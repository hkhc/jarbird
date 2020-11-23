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

import io.hkhc.gradle.JarbirdExtension
import io.hkhc.gradle.internal.bintray.ArtifactoryConfig
import io.hkhc.gradle.internal.bintray.BintrayConfig
import io.hkhc.gradle.internal.bintray.BintrayPublishPlan
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
 *  **** android sourcesSet and component creation
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
internal class BuildFlowBuilder(
    private val project: Project,
    private val extension: JarbirdExtension,
    private val pubs: List<JarbirdPubImpl>
) {

    @Suppress("unused")
    fun buildPhase1() {
        with(project) {

            val bintrayPublishPlan = BintrayPublishPlan(pubs)

            logger.debug("$LOG_PREFIX $PLUGIN_FRIENDLY_NAME Builder phase 1 of 4")

            // TODO 2 shall we add .configureEach after withType as suggested by
            // https://blog.gradle.org/preview-avoiding-task-configuration-time

            if (isMultiProjectRoot()) {
                logger.info("$LOG_PREFIX Configure root project '$name' for multi-project publishing")

                if (!rootProject.pluginManager.hasPlugin(PLUGIN_ID) && pubs.needBintray()) {
                    ArtifactoryConfig(this, extension, pubs).config()
                }
            } else {
                logger.info(
                    if (this == rootProject) {
                        "$LOG_PREFIX Configure project '$name' for single-project publishing bintray"
                    } else {
                        "$LOG_PREFIX Configure child project '$name' for multi-project publishing"
                    }
                )

                /* we support release gradle plugin or snapshot library, but not snapshot gradle plugin, to bintray */
                if (bintrayPublishPlan.bintray.isNotEmpty()) {
                    logger.info("config bintray")
                    BintrayConfig(this, extension, pubs).config()
                }
                if (bintrayPublishPlan.artifactory.isNotEmpty()) {
                    logger.info("config artifactory")
                    ArtifactoryConfig(this, extension, pubs).config()
                }
            }
        }
    }

    @Suppress("unused")
    fun buildPhase2() {
        project.logger.debug("$LOG_PREFIX $PLUGIN_FRIENDLY_NAME Builder phase 2 of 4")
        if (pubs.needGradlePlugin()) {
            PluginPublishingConfig(project, pubs).config()
        }
    }

    @Suppress("unused")
    fun buildPhase3() {
        project.logger.debug("$LOG_PREFIX $PLUGIN_FRIENDLY_NAME Builder phase 3 of 4")
//        if (pubs.needGradlePlugin()) {
//            PluginPublishingConfig(project, pubs).config2()
//        }
    }

    @Suppress("unused")
    fun buildPhase4() {
        project.logger.debug("$LOG_PREFIX $PLUGIN_FRIENDLY_NAME Builder phase 4 of 4")
        if (!project.isMultiProjectRoot()) {
            PublishingConfig(project, pubs).config()
            if (pubs.any { it.signing }) {
                SigningConfig(project, pubs).config()
            }
        }
        TaskBuilder(project, pubs).build()
    }
}