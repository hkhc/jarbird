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

package io.hkhc.gradle.internal.bintray

import groovy.lang.GroovyObject
import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.internal.JarbirdExtensionImpl
import io.hkhc.gradle.internal.LOG_PREFIX
import io.hkhc.gradle.internal.isMultiProjectRoot
import io.hkhc.gradle.internal.isSingleProject
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.kotlin.dsl.delegateClosureOf
import org.gradle.kotlin.dsl.getPluginByName
import org.jfrog.gradle.plugin.artifactory.dsl.ArtifactoryPluginConvention
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import org.jfrog.gradle.plugin.artifactory.dsl.ResolverConfig

@Suppress("SpreadOperator")
class ArtifactoryConfig(
    private val project: Project,
    private val extension: JarbirdExtensionImpl,
    private val pubs: List<JarbirdPub>
) {

//    private val publishPlan = BintrayPublishPlan(pubs)


    fun config() {

        val convention = project.convention.getPluginByName<ArtifactoryPluginConvention>("artifactory")

        // at this point we can assume that
        // - all ArtifactoryRepoSpec in all pubs are the same.
        // - all coordinate are either all release or all snapshot

        if (pubs.isEmpty())
            throw GradleException("No pubs to config")

        val model = ArtifactoryConfigModel(pubs)

        if (model.needsArtifactory()) {
            when {
                project.isSingleProject() -> {
                    convention.configSingle(model)
                }
                project.isMultiProjectRoot() -> {
                }
                else -> {
                    convention.configSingle(model)
                }
            }
        }
    }

    private fun ArtifactoryPluginConvention.configSingle(
        model: ArtifactoryConfigModel
    ) {

        project.logger.debug("$LOG_PREFIX configure Artifactory plugin for single project")

        setContextUrl(model.contextUrl)

        model.repoSpec?.let { repoSpec ->
            publish(
                delegateClosureOf<PublisherConfig> {
                    repository(
                        delegateClosureOf<GroovyObject> {
                            setProperty("repoKey", repoSpec.repoKey)
                            setProperty("username", repoSpec.username)
                            setProperty("password", repoSpec.password)
                            setProperty("maven", true)
                        }
                    )
                    defaults(
                        delegateClosureOf<GroovyObject> {
                            invokeMethod("publications", model.publications.toTypedArray())
                            setProperty("publishArtifacts", true)
                            setProperty("publishPom", true)
                            setProperty("publishIvy", false)
                        }
                    )
                }
            )

            resolve(
                delegateClosureOf<ResolverConfig> {
                    setProperty("repoKey", repoSpec.repoKey)
                }
            )
        }
    }

}
