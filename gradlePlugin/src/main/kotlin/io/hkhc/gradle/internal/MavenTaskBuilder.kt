/*
 * Copyright (c) 2021. Herman Cheung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package io.hkhc.gradle.internal

import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.RepoSpec
import io.hkhc.gradle.internal.repo.MavenRepoSpec
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.withType

class MavenTaskBuilder(val project: Project, val pubs: List<JarbirdPub>) {

    private val localTaskInfo = JbPublish.to.mavenLocal.taskInfo
    private val repoTaskInfos = mutableListOf<TaskInfo>()

    class RepoTasks {
        // we want the iteration order of the map to be predictable to help functional tests.
        val repoTaskInfoMap = linkedMapOf<RepoSpec, MutableList<String>>()

        fun add(repo: RepoSpec, taskName: String) {
            val taskList = repoTaskInfoMap[repo] ?: mutableListOf()
            taskList.add(taskName)
            repoTaskInfoMap[repo] = taskList
        }
    }

    fun getLocalTaskInfo(): TaskInfo = localTaskInfo
    fun getRepoTaskInfos(): List<TaskInfo> = repoTaskInfos

    fun registerMavenLocalTask(container: TaskContainer) {

        if (project.isMultiProjectRoot()) {
            project.registerRootProjectTasks(localTaskInfo)
        } else {

            /*
            JB_PUBLISH_TO_MAVEN_LOCAL_TASK
                + pub.jbPublishPubToMavenLocalTask ...
                    + pub.publishPubToMavenLocalTask
                    + pub.publishPluginMarkerPubToMavenLocalTask
             */

            localTaskInfo.register(container) {
                pubs.forEach { pub ->
                    val pubTaskInfo = JbPublish.pub(pub).to.mavenLocal.taskInfo
                    dependsOn(pubTaskInfo.name)
                }
            }

            pubs.forEach { pub ->

                val pubTaskInfo = JbPublish.pub(pub).to.mavenLocal.taskInfo

                pubTaskInfo.register(container) {
                    dependsOn(Publish.pub(pub).to.mavenLocal.taskName)

                    if (pub.pom.isGradlePlugin()) {
                        dependsOn(Publish.pluginMarker(pub).to.mavenLocal.taskName)
                    }
                }
            }
        }
    }

    fun registerMavenRepositoryTask(container: TaskContainer) {

        if (project.isMultiProjectRoot()) {

//            if (pubs.flatMap { it.getRepos() }.any { it is MavenSpec }) {
//                project.registerRootProjectTasks(JbPublishToMavenRepoTaskInfo())
//            }
            // TODO we shall check POM group that pubName is not duplicated among variants.
        } else if (pubs.needsNonLocalMaven()) {

            // TODO val JbPublishToMavenRepoTaskInfo(it)

            /*

            jbPublish
                jbPublishToMavenRepo
                jbPublishToCustomMavenRepo ...
                    jbPublish[pub]ToMaven[Repo]
                        publish[pub]PublicationToMaven[Repo]Repository

             */

            val taskMap = RepoTasks()

            /*
                 ┌─────┐1      n┌──────────────────┐
                 │ pub ├───────►│ CustomMavenRepos │
                 └─────┘        └──────────────────┘
             */
            val jbPublishPubToCustomMavenRepoTasks = pubs.map { pub ->

                val customRepoTaskInfos = pub.getRepos().filterIsInstance<MavenRepoSpec>().map { repoSpec ->
                    // TODO shall repo.name be capitalized?

                    JbPublish.pub(pub).to.mavenRepo(repoSpec).taskInfo.also {

                        val taskName = Publish.pub(pub).to.mavenRepo(repoSpec).taskName
                        val markerTaskName = Publish.pluginMarker(pub).to.mavenRepo(repoSpec).taskName

                        it.register(container) {
                            dependsOn(taskName)
                            if (pub.pom.isGradlePlugin()) {
                                dependsOn(markerTaskName)
                            }
                        }

                        taskMap.add(repoSpec, taskName)
                        if (pub.pom.isGradlePlugin()) {
                            taskMap.add(repoSpec, markerTaskName)
                        }
                    }
                }

                JbPublish.pub(pub).to.mavenRepo.taskInfo.register(container) {
                    customRepoTaskInfos.forEach {
                        dependsOn(it.name)
                    }
                }

            }

            JbPublish.to.mavenRepo.taskInfo.register(container) {
                jbPublishPubToCustomMavenRepoTasks.forEach {
                    dependsOn(it.name)
                }
            }

            taskMap.repoTaskInfoMap.forEach { (repo, taskList) ->
                JbPublish.to.mavenRepo(repo).taskInfo.also {
                    repoTaskInfos.add(it)
                    it.register(container) {
                        taskList.forEach { taskName ->
                            dependsOn(taskName)
                        }
                    }
                }
            }

            filterUnwantedPublication()

        }
    }


    /**
     * Filter out the combination of repositories and publications that does not make sense.
     */
    private fun filterUnwantedPublication() {

        // Filter unwanted combinations of publications and repositories
        // Retain the repository only if there is some JarbirdPubs that need it
        project.tasks.withType<PublishToMavenRepository>().configureEach {
            onlyIf {
                pubs.any { pub ->
                    pub.getRepos()
                        .filterIsInstance<MavenRepoSpec>()
                        .any { repoSpec -> pub.needs(repoSpec, repository, publication) }
                }
            }
        }
    }

    /**
     * return true if the combination of repository and publication is needed by the RepoSpec
      */
    private fun JarbirdPub.needs(
        repoSpec: MavenRepoSpec,
        repository: MavenArtifactRepository,
        publication: MavenPublication
    ): Boolean {

        // return true if the repo name matches and the pubName matches
        return if (repoSpec.repoName == repository.name) {
            if (pom.isGradlePlugin()) {
                pubNameWithVariant() == publication.name || markerPubName == publication.name
            } else {
                pubNameWithVariant() == publication.name
            }
        } else {
            false
        }

    }

}
