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

package io.hkhc.gradle.builder

import io.hkhc.gradle.pom.Pom
import io.hkhc.gradle.SP_GROUP
import io.hkhc.gradle.SimplePublisherExtension
import org.gradle.api.Project
import org.gradle.api.tasks.TaskContainer

class TaskBuilder(
    private val project: Project,
    private val pom: Pom,
    private val extension: SimplePublisherExtension,
    private val pubName: String
) {

    private val pubNameCap = pubName.capitalize()
    private val pubId = "${pubNameCap}Publication"
    private val markerPubId = "${pubNameCap}PluginMarkerMavenPublication"
    private val mavenRepo = "Maven${pubNameCap}Repository"
    private val mavenLocal = "MavenLocal"

    private fun TaskContainer.registerMavenLocalTask() {

        register("spPublishTo$mavenLocal") {
            group = SP_GROUP

            description = if (extension.gradlePlugin) {
                "Publish Maven publication '$pubName' " +
                        "and plugin '${pom.plugin?.id}' to the local Maven Repository"
            } else {
                "Publish Maven publication '$pubName' to the local Maven Repository"
            }

            dependsOn("publish${pubId}To$mavenLocal")

            if (extension.gradlePlugin) {
                dependsOn("publish${markerPubId}To$mavenLocal")
            }
        }
    }

    private fun TaskContainer.registerMavenRepositoryTask() {

        register("spPublishToMavenRepository") {
            group = SP_GROUP

            // I don't know why the maven repository name in the task name is not capitalized

            description = if (extension.gradlePlugin) {
                "Publish Maven publication '$pubName' " +
                        "and plugin '${pom.plugin?.id}' to the 'Maven$pubNameCap' Repository"
            } else {
                "Publish Maven publication '$pubName' to the 'Maven$pubNameCap' Repository"
            }

            dependsOn("publish${pubId}To$mavenRepo")

//            if (extension.gradlePlugin) {
//                dependsOn("publish${markerPubId}To$mavenRepo")
//            }
        }
    }

    private fun TaskContainer.registerBintrayTask() {
        register("spPublishToBintray") {
            group = SP_GROUP

            val target = if (pom.isSnapshot()) "OSS JFrog" else "Bintray"

            description = if (extension.gradlePlugin) {
                "Publish Maven publication '$pubName' " +
                        "and plugin '${pom.plugin?.id}' to $target"
            } else {
                "Publish Maven publication '$pubName' to $target"
            }

            /*
                bintray repository does not allow publishing SNAPSHOT artifacts, it has to be published
                to the OSS JFrog repository
             */
            if (pom.isSnapshot()) {
                if (extension.ossArtifactory) {
                    dependsOn("artifactory${pubNameCap}Publish")
                }
            } else {
                dependsOn("bintrayUpload")
            }
        }
    }

    private fun TaskContainer.registerGradlePortalTask() {
        register("spPublishToGradlePortal") {
            group = SP_GROUP
            description = "Publish plugin '${pom.plugin?.id}' to the Gradle plugin portal"
            dependsOn("publishPlugins")
        }
    }

    private fun TaskContainer.registerPublishTask() {
        register("spPublish") {
            group = SP_GROUP

            // assemble a list of repositories
            val repoList = mutableListOf<String>()
            repoList.add("Maven Local")
            repoList.add("'Maven$pubName' Repository")
            if (extension.bintray) {
                repoList.add("Bintray")
            }
            if (extension.gradlePlugin) {
                repoList.add("Gradle Plugin Portal")
            }
            val repoListStr = repoList.joinToString()

            if (extension.gradlePlugin) {
                description = "Publish Maven publication '$pubNameCap' " +
                        "and plugin '${pom.plugin?.id}' to $repoListStr"
            } else {
                description = "Publish Maven publication '$pubNameCap' to $repoListStr"
            }

            dependsOn("spPublishTo$mavenLocal")
            dependsOn("spPublishToMavenRepository")
            if (extension.bintray) {
                dependsOn("spPublishToBintray")
            }
            if (extension.gradlePlugin) {
                dependsOn("spPublishToGradlePortal")
            }
        }
    }

    fun build() {
        with(project.tasks) {
            registerMavenLocalTask()
            registerMavenRepositoryTask()
            if (extension.bintray) {
                registerBintrayTask()
            }
            if (extension.gradlePlugin) {
                registerGradlePortalTask()
            }
            registerPublishTask()
        }
    }
}
