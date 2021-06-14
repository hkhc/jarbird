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

package io.hkhc.gradle.internal.maven

import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.internal.JbPublish
import io.hkhc.gradle.internal.Publish
import io.hkhc.gradle.internal.TaskInfo
import io.hkhc.gradle.internal.needReposWithType
import io.hkhc.gradle.internal.needsReposWithType
import io.hkhc.gradle.internal.repo.MavenLocalRepoSpec
import io.hkhc.gradle.internal.repo.MavenRepoSpec
import io.hkhc.gradle.internal.reposWithType
import org.gradle.api.Project
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.withType

class MavenTaskBuilder(val project: Project, val pubs: List<JarbirdPub>) {

    private val localTaskInfo = JbPublish.to.mavenLocal.taskInfo
    private val repoTaskInfos = mutableListOf<TaskInfo>()

    class RepoTasks {
        // we want to use linkedMap to keep order of the map, to help functional tests.
        val repoTaskInfoMap = linkedMapOf<MavenRepoSpec, MutableList<String>>()

        fun add(repo: MavenRepoSpec, taskName: String) {
            val taskList = repoTaskInfoMap[repo] ?: mutableListOf()
            taskList.add(taskName)
            repoTaskInfoMap[repo] = taskList
        }
    }

    fun getLocalTaskInfo(): TaskInfo = localTaskInfo
//    fun getRepoTaskInfos(): List<TaskInfo> = repoTaskInfos

    fun registerMavenLocalTask(container: TaskContainer) {

        /*
            JB_PUBLISH_TO_MAVEN_LOCAL_TASK
                + pub.jbPublishPubToMavenLocalTask ...
                    + pub.publishPubToMavenLocalTask
                    + pub.publishPluginMarkerPubToMavenLocalTask
             */

        if (pubs.needReposWithType<MavenLocalRepoSpec>()) {
            localTaskInfo.register(container) {
                pubs.forEach { pub ->
                    val pubTaskInfo = JbPublish.pub(pub).to.mavenLocal.taskInfo
                    dependsOn(pubTaskInfo.name)
                }
            }
        }

        pubs.forEach { pub ->

            if (pub.needsReposWithType<MavenLocalRepoSpec>()) {
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

//        if (project.isMultiProjectRoot()) {
//            if (pubs.flatMap { it.getRepos() }.any { it is MavenRepoSpec }) {
//                project.registerRootProjectTasks(JbPublish.to.mavenRepo.taskInfo)
//            }
//            // TODO we shall check POM group that pubName is not duplicated among variants.
//        } else if (pubs.needsNonLocalMaven()) {

        if (pubs.needReposWithType<MavenRepoSpec>()) {

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
            val jbPublishPubToCustomMavenRepoTasks = pubs
                .map { pub -> pub to pub.reposWithType<MavenRepoSpec>() }
                .filter { it.second.isNotEmpty() }
                .map { (pub, repos) -> pub

                val customRepoTaskInfos = repos.map { repoSpec ->
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

                        taskMap.add(repoSpec, it.name)
                    }
                }

                JbPublish.pub(pub).to.mavenRepo.taskInfo.register(container) {
                    customRepoTaskInfos.forEach {
                        dependsOn(it.name)
                    }
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

            if (jbPublishPubToCustomMavenRepoTasks.isNotEmpty()) {
                JbPublish.to.mavenRepo.taskInfo.register(container) {
                    pubs.reposWithType<MavenRepoSpec>().forEach {
                        dependsOn(JbPublish.to.mavenRepo(it).taskInfo.name)
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

        val filter = MavenPublicationFilter()

        // Filter unwanted combinations of publications and repositories
        // Retain the repository only if there is some JarbirdPubs that need it
        project.tasks.withType<PublishToMavenRepository>().configureEach {
            onlyIf {
                filter.filter(pubs, repository, publication)
            }
        }
    }

}
