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

package io.hkhc.gradle.internal

import io.hkhc.gradle.JarbirdPub
import org.gradle.api.GradleException
import org.gradle.api.Project

class PluginPublishingModel(project: Project, pubs: List<JarbirdPub>) {

    lateinit var publishingPub: JarbirdPub // the pub that selected to publish artifact to Gradle Plugin Gradle
    var gradlePluginPubs: List<JarbirdPub>

    var entries: List<PluginEntry>

    var website: String
    var vcsUrl: String
    var description: String
    var tags: List<String>

    init {

        assert(pubs.needGradlePlugin())

        gradlePluginPubs = filterGradlePluginPub(project, pubs)
        if (gradlePluginPubs.isNotEmpty())
            publishingPub = gradlePluginPubs[0]
        else
            throw GradleException("No pub with Gradle Plugin Portal declaration")

        entries = createPluginEntries(gradlePluginPubs)

        // TODO we can have one set of metadata for gradle plugins in one project.
        // TODO do a pre check

        publishingPub.pom.also { pom ->
            website = pom.web.url ?: ""
            vcsUrl = pom.scm.url ?: website
            description = pom.plugin?.description ?: pom.description ?: ""
            tags = pom.plugin?.tags ?: listOf()
        }

    }

    companion object {

        fun filterGradlePluginPub(project: Project, pubs: List<JarbirdPub>): List<JarbirdPub> {
            // TODO we can have one pluginMaven publication per sub-project only

            var pluginPub = pubs.filter { pub -> pub.pom.isGradlePlugin() }
            if (pluginPub.isEmpty()) return listOf()
            if (pluginPub.size > 1) {
                project.logger.warn("$LOG_PREFIX More than one pub are declared to perform gradlePluginPublishing. "+
                    pluginPub.joinToString { it.pubNameWithVariant() } +
                    " Only the first one will be published to Gradle Plugin Portal.")
            }

            pluginPub.forEach {
                it.pom.plugin?.let { plugin ->
                    if (plugin.id == null)
                        throw GradleException("Plugin ID is not specified for pub ${it.pubNameWithVariant()}")
                    if (plugin.implementationClass == null)
                        throw GradleException("Plugin implementation class is not specified for pub ${it.pubNameWithVariant()}")
                } ?:
                throw GradleException(
                    "$LOG_PREFIX Pub ${it.pubNameWithVariant()} has Gradle Plugin Portal declared, " +
                            "but has no plugin information declared in pom.yaml"
                )
            }

            return pluginPub

        }

        fun createPluginEntries(pluginPubs: List<JarbirdPub>): List<PluginEntry> {

            val entries = mutableListOf<PluginEntry>()

            pluginPubs.forEach {
                it.pom.plugin?.let { plugin -> // plugin sbould not be null here
                    with(it.pom) {
                        entries.add(
                            PluginEntry(
                                id = plugin.id!!,
                                pubName = it.pubNameWithVariant(),
                                implementationClass = plugin.implementationClass!!,
                                group = group!!,
                                artifactId = artifactId!!,
                                version = version!!,
                                displayName = plugin.displayName!!,
                                description = description!!
                            )
                        )
                    }
                }
            }

            return entries

        }

    }

    data class PluginEntry(
        val id: String,
        val pubName: String,
        val implementationClass: String,
        val group: String,
        val artifactId: String,
        val version: String,
        val displayName: String,
        val description: String
    )

}
