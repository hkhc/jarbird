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
import io.hkhc.gradle.internal.repo.ArtifactoryRepoSpec
import org.gradle.api.Project
import org.gradle.kotlin.dsl.delegateClosureOf
import org.gradle.kotlin.dsl.getPluginByName
import org.jfrog.gradle.plugin.artifactory.dsl.ArtifactoryPluginConvention
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import org.jfrog.gradle.plugin.artifactory.dsl.ResolverConfig
import org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask

@Suppress("SpreadOperator")
class ArtifactoryConfig(
    private val project: Project,
    private val extension: JarbirdExtensionImpl,
    private val pubs: List<JarbirdPub>
) {

    private var repoUrl = "https://oss.jfrog.org"
    private val publishPlan = BintrayPublishPlan(pubs)

    fun config() {

        // TODO show warning if there are more than one BintraySpec in list
        val repoSpecOrNull = pubs.flatMap { it.getRepos() }.filterIsInstance<ArtifactoryRepoSpec>().firstOrNull()
        repoSpecOrNull?.let { repoSpec ->
            val customRepoUrl = if (pubs[0].pom.isSnapshot()) repoSpec.snapshotUrl else repoSpec.releaseUrl
            if (customRepoUrl != "") repoUrl = customRepoUrl

            val convention = project.convention.getPluginByName<ArtifactoryPluginConvention>("artifactory")
            when {
                project.isSingleProject() -> {
                    convention.configSingle(false, repoSpec)
                    configTask()
                }
                project.isMultiProjectRoot() -> {
                    // convention.configSingle(true)
                }
                else -> {
                    convention.configSingle(false, repoSpec)
                    configTask()
                }
            }
        }
    }

    private fun ArtifactoryPluginConvention.configSingle(isRootProject: Boolean, repoSpec: ArtifactoryRepoSpec) {

        project.logger.debug("$LOG_PREFIX configure Artifactory plugin for single project")

        setContextUrl(repoUrl)
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
                        if (!isRootProject) {
                            invokeMethod("publications", publishPlan.artifactoryPublications().toTypedArray())
                        }
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
//                setProperty("repoKey", "jcenter")
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
            @Suppress("SpreadOperator")
            publications(*publishPlan.artifactoryPublications().toTypedArray())
            if (project.isMultiProjectRoot()) {
                skip = true
            }
        }
    }
}
