/*
 * Copyright (c) 2020. Herman Cheung
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
import io.hkhc.gradle.internal.repo.MavenSpec
import org.gradle.api.Project
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.idea.util.string.joinWithEscape

class MavenTaskBuilder(val project: Project, val pubs: List<JarbirdPub>) {

    private val localTaskInfo = JbPublishToMavenLocalTaskInfo()
    private val repoTaskInfos = mutableListOf<TaskInfo>()

    class RepoTasks {
        // we want the iteration order of the map to be predictable to help functional tests.
        val repoTaskInfoMap = linkedMapOf<RepoSpec, MutableList<String>>()

        fun add(repo: RepoSpec, taskName: String) {
            println("taskMap add $taskName")
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
                    val pubTaskInfo = JbPublishPubToMavenLocalTaskInfo(pub)
                    dependsOn(pubTaskInfo.name)
                }
            }

            pubs.forEach { pub ->

                val pubTaskInfo = JbPublishPubToMavenLocalTaskInfo(pub)

                pubTaskInfo.register(container) {
                    dependsOn(pub.publishPubToMavenLocalTask)

                    if (pub.pom.isGradlePlugin()) {
                        dependsOn(pub.publishPluginMarkerPubToMavenLocalTask)
                    }
                }
            }
        }
    }

    fun registerMavenRepositoryTask(container: TaskContainer) {

        if (project.isMultiProjectRoot()) {

            pubs.flatMap { it.getRepos() }.filterIsInstance<MavenSpec>().forEach {
                project.registerRootProjectTasks(JbPublishToCustomMavenRepoTaskInfo(it))
            }
            // TODO we shall check POM group that pubName is not duplicated among variants.
        } else if (pubs.needsNonLocalMaven()) {

            /*
            JbPublishToMavenRepoTaskInfo
                + JbPublishPubToMavenRepoTaskInfo
                    + JbPublishPubToCustomMavenRepoTaskInfo
                        + pub.publishPubToCustomMavenRepoTask
                        + pub.publishPluginMarkerPubToCustomMavenRepoTask
             */

            // TODO val JbPublishToMavenRepoTaskInfo(it)

            val taskMap = RepoTasks()

            println("check point 0")
            val pubTaskInfos = pubs.map { pub ->

                val customRepoTaskInfos = pub.getRepos().filterIsInstance<MavenSpec>().map {
                    // TODO shall repo.name be capitalized?

                    println("pub repos ${it.getEndpoint().id}")

                    val customRepoTaskInfo = JbPublishPubToCustomMavenRepoTaskInfo(pub, it)

                    customRepoTaskInfo.register(container) {
                        println("registering JbPublishPubToCustomMavenRepoTaskInfo")
                        dependsOn(pub.publishPubToCustomMavenRepoTask(it))
                        if (pub.pom.isGradlePlugin()) {
                            dependsOn(pub.publishPluginMarkerPubToCustomMavenRepoTask(it))
                        }
                    }

                    taskMap.add(it, pub.publishPubToCustomMavenRepoTask(it))
                    if (pub.pom.isGradlePlugin()) {
                        taskMap.add(it, pub.publishPluginMarkerPubToCustomMavenRepoTask(it))
                    }

                    customRepoTaskInfo
                }

                val pubTaskInfo = JbPublishPubToMavenRepoTaskInfo(pub)
                println("register JbPublishPubToMavenRepoTaskInfo : ${pubTaskInfo.name} pub.variant ${pub.variant}")
                pubTaskInfo.register(container) {
                    customRepoTaskInfos.forEach {
                        dependsOn(it.name)
                    }
                }

                pubTaskInfo
            }

            println("check point 1")
            JbPublishToMavenRepoTaskInfo().register(container) {
                pubTaskInfos.forEach {
                    dependsOn(it.name)
                }
            }

            println("taskMap size ${taskMap.repoTaskInfoMap.size}")
            taskMap.repoTaskInfoMap.forEach { (repo, taskList) ->
                println("taskMap ${repo.getEndpoint().id} ${taskList.joinWithEscape(',')}")
                val taskInfo = JbPublishToCustomMavenRepoTaskInfo(repo)
                repoTaskInfos.add(taskInfo)
                println("register task ${taskInfo.name}")
                taskInfo.register(container) {
                    taskList.forEach { taskName ->
                        dependsOn(taskName)
                    }
                }
            }

            project.tasks.withType<PublishToMavenRepository>().configureEach {
                onlyIf {
                    println("onlyIf repoasitory ${repository.name} publication ${publication.name}")

                    pubs.any { pub ->
                        pub.getRepos().filterIsInstance<MavenSpec>().any { repo ->
                            println("repo.repoName ${repo.repoName}")
                            println("pub.pubNameWithVariant ${pub.pubNameWithVariant()}")
                            println("pub.pub.markerPubNameCap ${pub.markerPubName}")
                            if (repo.repoName == repository.name) {
                                if (pub.pom.isGradlePlugin()) {
                                    pub.pubNameWithVariant() == publication.name || pub.markerPubName == publication.name
                                } else {
                                    pub.pubNameWithVariant() == publication.name
                                }
                            } else {
                                false
                            }
                        }
                    }
                }
            }
        }
    }
}
