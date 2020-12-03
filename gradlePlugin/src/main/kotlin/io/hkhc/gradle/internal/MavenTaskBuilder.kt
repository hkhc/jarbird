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
import org.gradle.api.Project
import org.gradle.api.tasks.TaskContainer

class MavenTaskBuilder(val project: Project, val pubs: List<JarbirdPub>) {

    private val localTaskInfo = JbPublishToMavenLocalTaskInfo()
    private val repoTaskInfo = JbPublishToMavenRepoTaskInfo()

    fun getLocalTaskInfo(): TaskInfo = localTaskInfo
    fun getRepoTaskInfo(): TaskInfo = repoTaskInfo

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

            val parentTask = localTaskInfo.register(container) {
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
            project.registerRootProjectTasks(repoTaskInfo)
            // TODO we shall check POM group that pubName is not duplicated among variants.
        } else if (pubs.any { it.maven }) {

            /*
            JbPublishToMavenRepoTaskInfo
                + JbPublishPubToMavenRepoTaskInfo
                    + JbPublishPubToCustomMavenRepoTaskInfo
                        + pub.publishPubToCustomMavenRepoTask
                        + pub.publishPluginMarkerPubToCustomMavenRepoTask
             */

            pubs.filter { it.maven }.forEach { pub ->

                (pub as JarbirdPubImpl).mavenRepo.also {
                    // TODO shall repo.name be capitalized?

                    val customRepoTaskInfo = JbPublishPubToCustomMavenRepoTaskInfo(pub)

                    customRepoTaskInfo.register(container) {
                        dependsOn(pub.publishPubToCustomMavenRepoTask)
                        if (pub.pom.isGradlePlugin()) {
                            dependsOn(pub.publishPluginMarkerPubToCustomMavenRepoTask)
                        }
                    }

                    val pubTaskInfo = JbPublishPubToMavenRepoTaskInfo(pub)
                    pubTaskInfo.register(container) {
                        dependsOn(customRepoTaskInfo.name)
                    }

                    repoTaskInfo.register(container) {
                        dependsOn(pubTaskInfo.name)
                    }
                }
            }
        }
    }
}
