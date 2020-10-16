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

import io.hkhc.gradle.CLASSIFIER_JAVADOC
import io.hkhc.gradle.CLASSIFIER_SOURCE
import io.hkhc.gradle.JarbirdExtension
import io.hkhc.gradle.PUBLISH_GROUP
import io.hkhc.gradle.SP_EXT_NAME
import io.hkhc.gradle.maven.MavenPomAdapter
import io.hkhc.gradle.maven.mavenCentral
import io.hkhc.gradle.pom.Pom
import io.hkhc.gradle.utils.LOG_PREFIX
import io.hkhc.gradle.utils.detailMessageWarning
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.get

class PublishingConfig(
    private val project: Project,
    private val exte: JarbirdExtension,
    private val pom: Pom
) {
    private val extensions = (project as ExtensionAware).extensions
    private val pub = exte.pubItrn
    private var dokka = pub.dokka

    companion object {
        private fun printHelpForMissingDokka(project: Project) {
            detailMessageWarning(
                project.logger,
                "No dokka task is found in project '${project.name}'. Maven publishing cannot be done.",
                """
                    We cannot find a dokka task in project '${project.name}'.
                    You may need to apply the dokka plugin like this
                        id("org.jetbrains.dokka") version "0.10.1"

                    It create a default dokka task that jarbird recognize 
                    If you defined a dokka with different name, you may specify it in $SP_EXT_NAME block:
                        $SP_EXT_NAME {
                            ...
                            dokka = myDokkaTask
                            ...
                        }
                """.trimIndent()
            )
        }
    }

    fun config() {

        project.logger.debug("$LOG_PREFIX configure Publishing extension")

        if (dokka == null) {
            try {
                dokka = project.tasks.named("dokka")
            } catch (e: UnknownTaskException) {
                printHelpForMissingDokka(project)
            }
        }

        (
            project.findByType(PublishingExtension::class.java)
                ?: throw GradleException(
                    "\"publishing\" extension is not found. " +
                        "Maybe \"org.gradle.maven-publish\" is not applied?"
                )
            ).config()
    }

    private fun PublishingExtension.config() {

        publications {
            createPublication()
        }

        if (pub.maven) {
            repositories {
                createRepository()
            }
        }
    }

    private fun PublishArtifact.getString() =
        "PublishArtifact(name=$name,file=$file,classifier=$classifier,date=$date,extension=$extension,type=$type)"

    private fun PublicationContainer.createPublication() {

        val dokkaJar = setupDokkaJar()
        val sourcesJar = setupSourcesJar()

        val pomSpec = pom

        val pubComponent = pub.pubComponent
        register(pub.pubNameWithVariant(), MavenPublication::class.java) {

            groupId = pomSpec.group

            artifactId = pomSpec.artifactId

            // version is gotten from an external plugin
            //            version = project.versioning.info.display
            version = pomSpec.version

            // This is the main artifact
            from(project.components[pubComponent])
            // We are adding documentation artifact
            project.afterEvaluate {
                dokkaJar?.let { artifact(it.get()) }
                // And sources
                sourcesJar?.let { artifact(it.get()) }
            }

            pom { MavenPomAdapter().fill(this, pomSpec) }
        }
    }

    private fun RepositoryHandler.createRepository() {

        // even if we don't publish to maven repository, we still need to set it up as bintray needs it.
        val endpoint = pub.mavenRepository ?: project.mavenCentral()

        maven {
            name = "Maven${pub.pubNameWithVariant().capitalize()}"
            val endpointUrl =
                if (pom.isSnapshot()) {
                    endpoint.snapshotUrl
                } else {
                    endpoint.releaseUrl
                }
            url = project.uri(endpointUrl)
            credentials {
                username = endpoint.username
                password = endpoint.password
            }
        }
    }

    @Suppress("UnstableApiUsage")
    private fun setupDokkaJar(): TaskProvider<Jar>? {
        if (dokka == null) return null
        val dokkaJarTaskName = pub.pubNameWithVariant("dokkaJar")
        return try {
            project.tasks.named(dokkaJarTaskName, Jar::class.java) {
                group = PUBLISH_GROUP
                archiveClassifier.set(CLASSIFIER_JAVADOC)
            }
        } catch (e: UnknownTaskException) {
            // TODO add error message here if dokka is null
            project.tasks.register(dokkaJarTaskName, Jar::class.java) {
                group = PUBLISH_GROUP
                description = "Assembles Kotlin docs with Dokka to Jar"
                archiveClassifier.set(CLASSIFIER_JAVADOC)
                from(dokka)
                dependsOn(dokka)
            }
        }
    }

    @Suppress("UnstableApiUsage")
    private fun setupSourcesJar(): TaskProvider<Jar>? {

        val sourcesJarTaskName = pub.pubNameWithVariant("sourcesJar")
        return try {
            project.tasks.named(sourcesJarTaskName, Jar::class.java) {
                archiveClassifier.set(CLASSIFIER_SOURCE)
            }
        } catch (e: UnknownTaskException) {
            val desc = if (pub.variant == "") {
                "Create archive of source code for the binary"
            } else {
                "Create archive of source code for the binary of variant '${pub.variant}' "
            }

            val ss = pub.sourceSets?.let { it } ?: sourceSets.getByName(pub.sourceSetName).allSource

            project.tasks.register(sourcesJarTaskName, Jar::class.java) {
                group = PUBLISH_GROUP
                description = desc
                archiveClassifier.set(CLASSIFIER_SOURCE)
                from(ss)
            }
        }
    }

    private val sourceSets: SourceSetContainer
        get() =
            extensions.getByName("sourceSets") as SourceSetContainer
}
