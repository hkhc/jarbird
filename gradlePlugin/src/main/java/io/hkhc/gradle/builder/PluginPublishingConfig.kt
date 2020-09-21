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

import com.gradle.publish.PluginBundleExtension
import io.hkhc.gradle.pom.Pom
import io.hkhc.gradle.JarbirdExtension
import io.hkhc.util.LOG_PREFIX
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension

class PluginPublishingConfig(
    private val project: Project,
    extension: JarbirdExtension,
    private val pom: Pom
) {

    private val variantCap = extension.variant.capitalize()
    private val pubName = "${extension.pubName}$variantCap"
    private val ext = (project as ExtensionAware).extensions

    fun config() {
        ext.findByType(PluginBundleExtension::class.java)?.config()
        ext.findByType(GradlePluginDevelopmentExtension::class.java)?.config()
//        updatePluginPublication(project, pom.name!!)
//        config2()
    }

    fun config2() {
        updatePluginPublication(project, pom.name!!)
    }

    private fun PluginBundleExtension.config() {
        website = pom.web.url
        vcsUrl = pom.scm.url ?: website
        description = pom.plugin?.description ?: pom.description
        tags = pom.plugin?.tags ?: listOf()
    }

    private fun GradlePluginDevelopmentExtension.config() {

        project.logger.debug("$LOG_PREFIX configure Gradle plugin development plugin")

        plugins {
            create(pubName) {
                pom.plugin?.let { plugin ->
                    id = plugin.id
                    displayName = plugin.displayName
                    description = plugin.description
                    implementationClass = plugin.implementationClass
                }
            }
        }
    }

    /*
        The gradle publish plugin hardcoded to use project name as publication artifact name.
        We further customize that publication here and replace it with pom.artifactId
        The default value of pom.name is still project.name so we are not violating the Gradle convention.
        "publishPluginMavenPublication*" task is created by gradle plugin publish plugin.
     */
    private fun updatePluginPublication(project: Project, artifactId: String) {

        System.out.println("updatePluginPublication");

        project.extensions.configure(PublishingExtension::class.java) {
            publications.find { it.name.startsWith("pluginMaven") }?.let {
                System.out.println("updatePluginPublication found");
                if (it is MavenPublication) {
                    it.artifactId = artifactId
                }
            }
        }
    }
}
