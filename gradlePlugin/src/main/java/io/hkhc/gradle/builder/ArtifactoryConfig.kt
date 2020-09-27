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

import groovy.lang.GroovyObject
import io.hkhc.gradle.PublishConfig
import io.hkhc.gradle.JarbirdExtension
import io.hkhc.gradle.isMultiProjectRoot
import io.hkhc.util.LOG_PREFIX
import org.gradle.api.Project
import org.gradle.kotlin.dsl.delegateClosureOf
import org.gradle.kotlin.dsl.getPluginByName
import org.gradle.kotlin.dsl.register
import org.jfrog.gradle.plugin.artifactory.dsl.ArtifactoryPluginConvention
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import org.jfrog.gradle.plugin.artifactory.dsl.ResolverConfig
import org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask

class ArtifactoryConfig(
    private val project: Project,
    private val extension: JarbirdExtension
) {

    private val pubConfig = PublishConfig(project)
    private val pubName = extension.pubNameWithVariant()

    fun config() {
        project.convention.getPluginByName<ArtifactoryPluginConvention>("artifactory").config()
    }

    private fun ArtifactoryPluginConvention.config() {

        project.logger.debug("$LOG_PREFIX configure Artifactory plugin")

        setContextUrl("https://oss.jfrog.org")
        publish(
            delegateClosureOf<PublisherConfig> {
                repository(
                    delegateClosureOf<GroovyObject> {
                        setProperty("repoKey", "oss-snapshot-local")
                        setProperty("username", pubConfig.bintrayUser)
                        setProperty("password", pubConfig.bintrayApiKey)
                        setProperty("maven", true)
                    }
                )
                defaults(
                    delegateClosureOf<GroovyObject> {
                        if (extension.gradlePlugin) {
                            invokeMethod(
                                "publications",
                                arrayOf(
                                    pubName as Any,
                                    "${pubName}PluginMarkerMaven" as Any
                                )
                            )
                        } else {
                            invokeMethod("publications", pubName)
                        }
                        setProperty("publishArtifacts", true)
                        setProperty("publishPom", true)
                    }
                )
            }
        )

        resolve(
            delegateClosureOf<ResolverConfig> {
                setProperty("repoKey", "jcenter")
            }
        )

        project.tasks.register("artifactory${pubName.capitalize()}Publish", ArtifactoryTask::class) {
            if (extension.gradlePlugin) {
                publications(pubName, "${pubName}PluginMarkerMaven")
            } else {
                publications(pubName)
            }
            if (project.isMultiProjectRoot()) {
                skip = true
            }
        }
    }
}
