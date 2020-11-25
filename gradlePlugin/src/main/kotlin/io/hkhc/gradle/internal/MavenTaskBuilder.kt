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

    fun registerMavenLocalTask(container: TaskContainer) {

        if (project.isMultiProjectRoot()) {
            project.registerRootProjectTasks(JbPublishToMavenLocalTaskInfo().name)
        } else {

            /*
            JB_PUBLISH_TO_MAVEN_LOCAL_TASK
                + pub.jbPublishPubToMavenLocalTask ...
                    + pub.publishPubToMavenLocalTask
                    + pub.publishPluginMarkerPubToMavenLocalTask
             */

            val parentTask = JbPublishToMavenLocalTaskInfo().register(container).get()

            pubs.forEach { pub ->

                JbPublishPubToMavenLocalTaskInfo(pub).register(container) {
                    dependsOn(pub.publishPubToMavenLocalTask)

                    if (pub.pom.isGradlePlugin()) {
                        dependsOn(pub.publishPluginMarkerPubToMavenLocalTask)
                    }
                    parentTask.dependsOn(this)
                }
            }
        }
    }

    fun registerMavenRepositoryTask(container: TaskContainer) {

        if (project.isMultiProjectRoot()) {
            // TODO we shall check POM group that pubName is not duplicated among variants.
            pubs.filter { it.maven }.forEach { pub ->
                project.registerRootProjectTasks(JbPublishPubToCustomMavenRepoTaskInfo(pub).name)
            }
        } else if (pubs.any { it.maven }) {

            /*
            JbPublishToMavenRepoTaskInfo
                + JbPublishPubToMavenRepoTaskInfo
                    + JbPublishPubToCustomMavenRepoTaskInfo
                        + pub.publishPubToCustomMavenRepoTask
                        + pub.publishPluginMarkerPubToCustomMavenRepoTask
             */

            val allMavenRepoTask = JbPublishToMavenRepoTaskInfo().register(container).get()

            pubs.filter { it.maven } .forEach { pub ->
                (pub as JarbirdPubImpl).mavenRepo.also {
                    // TODO shall repo.name be capitalized?

                    val customMavenRepoTask = JbPublishPubToCustomMavenRepoTaskInfo(pub).register(container) {
                        dependsOn(pub.publishPubToCustomMavenRepoTask)

                        if (pub.pom.isGradlePlugin()) {
                            dependsOn(pub.publishPluginMarkerPubToCustomMavenRepoTask)
                        }
                    }.get()

                    val mavenRepoTask = JbPublishPubToMavenRepoTaskInfo(pub).register(container) {
                        dependsOn(customMavenRepoTask)
                    }

                    allMavenRepoTask.dependsOn(mavenRepoTask)
                }
            }
        }
    }
}
