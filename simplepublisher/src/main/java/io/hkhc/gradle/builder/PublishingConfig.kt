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
import io.hkhc.gradle.PUBLISH_GROUP
import io.hkhc.gradle.PublishConfig
import io.hkhc.gradle.SP_EXT_NAME
import io.hkhc.gradle.SimplePublisherExtension
import io.hkhc.gradle.mavenCentral
import io.hkhc.gradle.pom.Pom
import io.hkhc.gradle.pom.fillTo
import io.hkhc.util.LOG_PREFIX
import io.hkhc.util.detailMessageWarning
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
    private val extension: SimplePublisherExtension,
    private val pom: Pom
) {

    private val pubConfig = PublishConfig(project)
    private val variantCap = extension.variant.capitalize()
    private val pubName = "${extension.pubName}$variantCap"
    private val ext = (project as ExtensionAware).extensions
    private var dokka = extension.dokka

    companion object {
        private fun printHelpForMissingDokka(project: Project) {
            detailMessageWarning(
                project.logger,
                "No dokka task is found in project '${project.name}'. Maven publishing cannot be done.",
                """
                    We cannot find a dokka task in project '${project.name}'.
                    You may need to apply the dokka plugin like this
                        id("org.jetbrains.dokka") version "0.10.1"

                    It create a default dokka task that simplepublisher recognize 
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

        ext.findByType(PublishingExtension::class.java)?.config()
    }

    private fun PublishingExtension.config() {

        publications {
            createPublication()
        }

        repositories {
            createRepository()
        }
    }

    private fun PublishArtifact.getString() =
        "PublishArtifact(name=$name,file=$file,classifier=$classifier,date=$date,extension=$extension,type=$type)"

    private fun PublicationContainer.createPublication() {

        val dokkaJar = setupDokkaJar()
        val sourcesJar = setupSourcesJar()

        val pomSpec = pom

        val pubComponent = extension.pubComponent

//        project.components[pubComponent].also {
//            if (it is SoftwareComponentInternal) {
//                it.usages.forEach {usageContext ->
//                    project.logger.info("COMPONENT usages: ${usageContext}")
//                    project.logger.info("COMPONENT     name : ${usageContext.name}")
//                    usageContext.artifacts.forEach { artifact ->
//                        project.logger.info("COMPONENT     artifact: ${artifact.getString()}")
//                    }
//                    usageContext.attributes.also { attributes ->
//                        project.logger.info("COMPONENT     attributes: ${attributes}")
//                    }
//                    usageContext.capabilities.forEach { capability ->
//                        project.logger.info("COMPONENT     capability: ${capability}")
//                    }
//                    usageContext.dependencies.forEach {dependency ->
//                        project.logger.info("COMPONENT     dependency: ${dependency}")
//                    }
//                    usageContext.dependencyConstraints.forEach { constraint ->
//                        project.logger.info("COMPONENT     constraint: ${constraint}")
//                    }
//                }
//            }
//        }

        register(pubName, MavenPublication::class.java) {

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

            pom { pomSpec.fillTo(this) }
        }
    }

    private fun RepositoryHandler.createRepository() {

        val endpoint = extension.mavenRepository ?: project.mavenCentral()

        maven {
            name = "Maven${pubName.capitalize()}"
            with(pubConfig) {
                url = project.uri(
                    if (pom.isSnapshot()) {
                        endpoint.snapshotUrl
                    } else {
                        endpoint.releaseUrl
                    }
                )
                credentials {
                    username = endpoint.username
                    password = endpoint.password
                }
            }
        }
    }

    @Suppress("UnstableApiUsage")
    private fun setupDokkaJar(): TaskProvider<Jar>? {
        if (dokka == null) return null
        val dokkaJarTaskName = "dokkaJar$variantCap"
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

        val sourcesJarTaskName = "sourcesJar$variantCap"
        return try {
            project.tasks.named(sourcesJarTaskName, Jar::class.java) {
                archiveClassifier.set(CLASSIFIER_SOURCE)
            }
        } catch (e: UnknownTaskException) {
            val desc = if (extension.variant == "") {
                "Create archive of source code for the binary"
            } else {
                "Create archive of source code for the binary of variant '${extension.variant}' "
            }

            val path = extension.sourcesPath ?: sourceSets.getByName(extension.sourceSetName).allSource

            project.tasks.register(sourcesJarTaskName, Jar::class.java) {
                group = PUBLISH_GROUP
                description = desc
                archiveClassifier.set(CLASSIFIER_SOURCE)
                from(path)
            }
        }
    }

    private val sourceSets: SourceSetContainer
        get() =
            ext.getByName("sourceSets") as SourceSetContainer
}
