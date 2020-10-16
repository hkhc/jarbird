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
import io.hkhc.gradle.BintrayPublishConfig
import io.hkhc.gradle.JarbirdExtension
import io.hkhc.gradle.isMultiProjectRoot
import io.hkhc.gradle.isSingleProject
import io.hkhc.gradle.utils.LOG_PREFIX
import org.gradle.api.Project
import org.gradle.kotlin.dsl.delegateClosureOf
import org.gradle.kotlin.dsl.getPluginByName
import org.jfrog.gradle.plugin.artifactory.dsl.ArtifactoryPluginConvention
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import org.jfrog.gradle.plugin.artifactory.dsl.ResolverConfig
import org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask

class ArtifactoryConfig(
    private val project: Project,
    private val extension: JarbirdExtension
) {

    private var repoUrl = "https://oss.jfrog.org"
    private val pubConfig = BintrayPublishConfig(project)
    private val pub = extension.pubItrn

    fun config() {

        repoUrl = pub.bintrayRepository?.snapshotUrl ?: repoUrl

        val convention = project.convention.getPluginByName<ArtifactoryPluginConvention>("artifactory")
        if (project.isSingleProject()) {
            convention.configSingle(false)
            configTask()
        } else if (project.isMultiProjectRoot()) {
//            convention.configSingle(true)
        } else {
            convention.configSingle(false)
            configTask()
        }
//        project.convention.getPluginByName<ArtifactoryPluginConvention>("artifactory").config()
    }

    private fun ArtifactoryPluginConvention.configSingle(isRootProject: Boolean) {

        project.logger.debug("$LOG_PREFIX configure Artifactory plugin for single project")

        setContextUrl(repoUrl)
        publish(
            delegateClosureOf<PublisherConfig> {
                repository(
                    delegateClosureOf<GroovyObject> {
                        setProperty("repoKey", "oss-snapshot-local")
                        setProperty("username", pubConfig.bintrayUsername)
                        setProperty("password", pubConfig.bintrayApiKey)
                        setProperty("maven", true)
                    }
                )
                defaults(
                    delegateClosureOf<GroovyObject> {
                        if (!isRootProject)
                            invokeMethod("publications", pub.pubNameWithVariant())
                        setProperty("publishArtifacts", true)
                        setProperty("publishPom", true)
                        setProperty("publishIvy", false)
                    }
                )
            }
        )

        resolve(
            delegateClosureOf<ResolverConfig> {
                setProperty("repoKey", "jcenter")
            }
        )
    }

    private fun ArtifactoryPluginConvention.configSub() {
//        publish(
//            delegateClosureOf<PublisherConfig> {
//                defaults(
//                    delegateClosureOf<GroovyObject> {
//                        invokeMethod("publications", pubName)
//                    }
//                )
//            }
    }

    private fun configTask() {

        project.tasks.named("artifactoryPublish", ArtifactoryTask::class.java) {
            publications(pub.pubNameWithVariant())
            if (project.isMultiProjectRoot()) {
                skip = true
            }
        }
    }

    private fun ArtifactoryPluginConvention.config() {

        project.logger.debug("$LOG_PREFIX configure Artifactory plugin")

        setContextUrl("https://oss.jfrog.org")
        publish(
            delegateClosureOf<PublisherConfig> {
                repository(
                    delegateClosureOf<GroovyObject> {
                        setProperty("repoKey", "oss-snapshot-local")
                        setProperty("username", pubConfig.bintrayUsername)
                        setProperty("password", pubConfig.bintrayApiKey)
                        setProperty("maven", true)
                    }
                )
                defaults(
                    delegateClosureOf<GroovyObject> {
                        // TODO Not support artifactory snapshot gradle plugin at the moment
//                        if (extension.gradlePlugin) {
//                            invokeMethod(
//                                "publications",
//                                arrayOf(
//                                    pubName as Any,
//                                    "${pubName}PluginMarkerMaven" as Any
//                                )
//                            )
//                        } else {
                        invokeMethod("publications", pub.pubNameWithVariant())
//                        }
                        setProperty("publishArtifacts", true)
                        setProperty("publishPom", true)
                        setProperty("publishIvy", false)
                    }
                )
            }
        )

        resolve(
            delegateClosureOf<ResolverConfig> {
                setProperty("repoKey", "jcenter")
            }
        )

        project.tasks.named("artifactoryPublish", ArtifactoryTask::class.java) {
            // TODO Not support artifactory snapshot gradle plugin at the moment
//            if (extension.gradlePlugin) {
//                publications(pubName, "${pubName}PluginMarkerMaven")
//            } else {
            publications(pub.pubNameWithVariant())
//            }
            if (project.isMultiProjectRoot()) {
                skip = true
            }
        }
    }
}
