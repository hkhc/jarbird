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

import com.gradle.publish.PluginBundleExtension
import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.internal.utils.findByType
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension

class PluginPublishingConfig(
    private val project: Project,
    private val pubs: List<JarbirdPub>
) {

    /*
        The following plugins shall be declared as dependencies in build.gradle.kts.
        The exact dependency identifier can be find by accessing the plugin POM file at
        https://plugins.gradle.org/m2/[path-by-id]/[plugin-id].gradle.plugin/[version]/
            [plugin-id].gradle.plugin-[version].pom

        e.g. for plugin com.gradle.plugin-publish, check the dependency section of POM at
        https://plugins.gradle.org/m2/com/gradle/plugin-publish/
            com.gradle.plugin-publish.gradle.plugin/0.10.1/com.gradle.plugin-publish.gradle.plugin-0.10.1.pom
     */

    fun config() {
        (
            // TODO rename findByType to findExtension
            project.findByType(PluginBundleExtension::class.java)
                ?: throw GradleException(
                    "\"pluginBundle\" extension is not found, may be " +
                        "\"com.gradle.plugin-publish\" plugin is not applied?"
                )
            ).config()

        (
            project.findByType(GradlePluginDevelopmentExtension::class.java)
                ?: throw GradleException(
                    "\"gradlePlugin\" extension is not found, may be " +
                        "\"plugin org.gradle.java-gradle-plugin\" is not applied?"
                )
            ).config()

        presetupPluginMarkerPublication()
    }

    /*
        The gradle publish plugin hardcoded to use project name as publication artifact name.
        We further customize that publication here and replace it with pom.artifactId
        The default value of pom.name is still project.name so we are not violating the Gradle convention.
        "publishPluginMavenPublication*" task is created by gradle plugin publish plugin.
     */
    private fun presetupPluginMarkerPublication() {

        // TODO we can have one pluginMaven publication per sub-project only

        pubs.forEach { pub ->
            // We create the marker pubkication here so that the MavenPluginPublishPlugin may reuse it
            // and we can do some customization here.
            if (pub.pom.isGradlePlugin()) {
                val publishing = project.findByType(PublishingExtension::class.java)
                publishing?.publications {
                    val markerPublication = maybeCreate(
                        "pluginMaven",
                        MavenPublication::class.java
                    )
                    markerPublication.groupId = pub.pom.group
                    markerPublication.artifactId = pub.pom.artifactId
                    markerPublication.version = pub.pom.version
                }
            }
        }
    }

//        pubs.forEach {
//            if (it.pom.isGradlePlugin()) {
//                it.variantArtifactId()?.let { artifactId ->
//                    updatePluginPublication(project, artifactId)
//                }
//            }
//        }
//    }

    private fun PluginBundleExtension.config() {

        // TODO we can have one set of metadata for gradle plugins in one project.

        val pom = pubs.map { it.pom }.first { it.isGradlePlugin() }

        website = pom.web.url
        vcsUrl = pom.scm.url ?: website
        description = pom.plugin?.description ?: pom.description
        tags = pom.plugin?.tags ?: listOf()
    }

    private fun GradlePluginDevelopmentExtension.config() {

        project.logger.debug("$LOG_PREFIX configure Gradle plugin development plugin")

        plugins {
            pubs.filter { it.pom.isGradlePlugin() }.forEach {
                create(it.pubNameWithVariant()) {
                    it.pom.plugin?.let { plugin ->
                        id = plugin.id
                        displayName = plugin.displayName
                        description = plugin.description
                        implementationClass = plugin.implementationClass
                    }
                }
            }
        }
    }

//    private fun updatePluginPublication(project: Project, artifactId: String) {
//
//        println("updatePluginPublication ${artifactId}")
//
//        project.extensions.configure(PublishingExtension::class.java) {
//            publications.find { it.name.startsWith("pluginMaven") }?.let {
//                if (it is MavenPublication) {
//                    println("updatePluginPublication ready to update ${artifactId} vs ${it.artifactId}")
//                    it.artifactId = artifactId
//                }
//            }
//        }
//    }
}
