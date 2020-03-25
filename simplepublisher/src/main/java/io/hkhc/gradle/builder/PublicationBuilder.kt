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

import io.hkhc.gradle.SimplePublisherExtension
import io.hkhc.gradle.isMultiProjectRoot
import io.hkhc.gradle.pom.Pom
import org.gradle.api.Project

/**
 *
 *
 * Build phases:
 *
 * phase 1: before all project evaluation listeners (after evaluate)
 *  - bintray extension
 * - setup ossArtifactory
 *
 *  **** bintray plugin project evaluation listener (after evaluate)
 *      - set task dependency
 *      - assign file spec to bintrayUpload tasks
 * phase 2: after all project evaluation listeners (after evaluate)
 *  - setup gradle plugin portal
 *  - setup version of  pluginMavenPublication task
 *
 * ------
 * Script execution
 * configure android library extension
 * ------
 *
 * phase 3: before all project.afterEvaluate
 *  **** android sourcesset and component creation
 *  **** configure android library variant
 *  **** configure simply publisher variant
 *  **** configure gradle plugin portal
 *
 * phase 4: after all project.afterEvaluate
 * - setup dokka task
 * - setup publishing extension
 *   - setup publications
 *      - setup dokkaJar task
 *      - setup sourcessetJar task
 *   - setup repository
 * - setup signing
 *
 * phase 5: project evaluated
 *  **** bintray plugin project evaluation listener (project evaluated)
 *  - Create facade tasks
 */
class PublicationBuilder(
    private val extension: SimplePublisherExtension,
    private val project: Project,
    private val pom: Pom
) {

    private val variantCap = extension.variant.capitalize()
    private val pubName = "${extension.pubName}$variantCap"

    @Suppress("unused")
    fun buildPhase1() {
        with(project) {

            logger.debug("SimplePublisher Builder phase 1")

            // TODO 2 shall we add .configureEach after withType as suggested by
            // https://blog.gradle.org/preview-avoiding-task-configuration-time

            if (isMultiProjectRoot()) {

                logger.info("Configure root project '$name' for multi-project publishing")

                if (!rootProject.pluginManager.hasPlugin("io.hkhc.simplepublisher")) {
                    if (extension.ossArtifactory) {
                        ArtifactoryConfig(this, extension).config()
                    }
                }
            } else {

                if (this == rootProject) {
                    logger.info("Configure project '$name' for single-project publishing")
                } else {
                    logger.info("Configure child project '$name' for multi-project publishing")
                }

                if (extension.bintray) {
                    BintrayConfig(this, extension, pom).config()
                }

                if (extension.ossArtifactory) {
                    ArtifactoryConfig(this, extension).config()
                }
            }
        }
    }

    @Suppress("unused")
    fun buildPhase2() {
        project.logger.debug("SimplePublisher Builder phase 2")
        if (extension.gradlePlugin) {
            PluginPublishingConfig(project, extension, pom).config()
        }
    }

    @Suppress("unused")
    fun buildPhase3() {
        project.logger.debug("SimplePublisher Builder phase 3")
    }

    @Suppress("unused")
    fun buildPhase4() {
        project.logger.debug("SimplePublisher Builder phase 4")
        if (!project.isMultiProjectRoot()) {
            PublishingConfig(project, extension, pom).config()
            if (extension.signing) {
                SigningConfig(project, extension, pom).config()
            }
        }
    }

    @Suppress("unused")
    fun buildPhase5() {
        project.logger.debug("SimplePublisher Builder phase 5")
        TaskBuilder(project, pom, extension, pubName).build()
    }
}
