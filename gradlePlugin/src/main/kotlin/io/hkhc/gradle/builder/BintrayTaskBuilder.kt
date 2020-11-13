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

import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.internal.SP_GROUP
import io.hkhc.gradle.internal.LOG_PREFIX
import org.gradle.api.Project
import org.gradle.api.tasks.TaskContainer

class BintrayTaskBuilder(
    private val project: Project,
    pubs: List<JarbirdPub>
) {

    private val publishPlan = BintrayPublishPlan(pubs)

    private fun bintrayTaskDescription(): String {

        val bintrayLibs =
            publishPlan.bintrayLibs.joinToString(", ") { "'${it.getGAV()}'" }
        val bintrayPlgins =
            publishPlan.bintrayPlugins.joinToString(", ") { "'${it.pom.plugin?.id}:${it.variantVersion()}'" }
        val artifactoryLibs =
            publishPlan.artifactoryLibs.joinToString(", ") { "'${it.getGAV()}'" }

        val bintrayPublicationStr = "publication${if (publishPlan.bintrayLibs.size > 1) "s" else ""}"
        val bintrayPluginStr = "plugin${if (publishPlan.bintrayPlugins.size > 1) "s" else ""}"
        val artifactoryPublicationStr = "publication${if (publishPlan.artifactoryLibs.size > 1) "s" else ""}"

        val bintrayDesc = when {
            !bintrayLibs.isBlank() && !bintrayPlgins.isBlank() ->
                "Publish $bintrayPublicationStr $bintrayLibs and $bintrayPluginStr $bintrayPlgins to Bintray."
            !bintrayLibs.isBlank() && bintrayPlgins.isBlank() ->
                "Publish $bintrayPublicationStr $bintrayLibs to Bintray."
            bintrayLibs.isBlank() && !bintrayPlgins.isBlank() ->
                "Publish $bintrayPluginStr $bintrayPlgins to Bintray."
            else -> ""
        }

        val artifactoryDesc = when {
            !artifactoryLibs.isBlank() -> """
                        Publish $artifactoryPublicationStr $artifactoryLibs to OSS Jfrog.
            """.trimIndent()
            else -> ""
        }

        return "$bintrayDesc $artifactoryDesc".trim()
    }

    fun registerBintrayTask(taskContainer: TaskContainer) {

        if (publishPlan.invalidPlugins.isNotEmpty()) {
            project.logger.warn(
                "WARNING: $LOG_PREFIX Publish snapshot Gradle Plugin to Bintray/OSSArtifactory is not supported."
            )
        }

        if (publishPlan.bintray.isNotEmpty() || publishPlan.artifactory.isNotEmpty()) {

            with(taskContainer) {
                register(JB_PUBLISH_TO_BINTRAY_TASK) {
                    group = SP_GROUP
                    description = bintrayTaskDescription()
                    /*
                        bintray repository does not allow publishing SNAPSHOT artifacts, it has to be published
                        to the OSS JFrog repository
                     */

                    if (publishPlan.artifactory.isNotEmpty()) {
                        dependsOn(ARTIFACTORY_PUBLISH_TASK)
                    }

                    if (publishPlan.bintray.isNotEmpty()) {
                        dependsOn(BINTRAY_UPLOAD_TASK)
                    }
                }
            }
        }
    }
}
