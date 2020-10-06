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
import io.hkhc.gradle.JarbirdExtension
import io.hkhc.gradle.pom.Pom
import util.LOG_PREFIX
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension

class PluginPublishingConfig(
    private val project: Project,
    extension: JarbirdExtension,
    private val pom: Pom
) {

    private val pubName = extension.pubNameWithVariant()

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
    }

    fun config2() {
        pom.name ?.let {
            updatePluginPublication(project, it)
        }
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

        project.extensions.configure(PublishingExtension::class.java) {
            publications.find { it.name.startsWith("pluginMaven") }?.let {
                if (it is MavenPublication) {
                    it.artifactId = artifactId
                }
            }
        }
    }
}
