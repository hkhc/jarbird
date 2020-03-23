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

package io.hkhc.gradle

import com.gradle.publish.PluginBundleExtension
import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.RecordingCopyTask
import groovy.lang.GroovyObject
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.closureOf
import org.gradle.kotlin.dsl.delegateClosureOf
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getPluginByName
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.gradle.plugins.signing.SigningExtension
import org.jfrog.gradle.plugin.artifactory.dsl.ArtifactoryPluginConvention
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import org.jfrog.gradle.plugin.artifactory.dsl.ResolverConfig
import org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask

class PublicationBuilder(
    private val extension: SimplePublisherExtension,
    private val project: Project,
    private val pom: Pom
) {

    private val pubConfig = PublishConfig(project)
    private val variantCap = extension.variant.capitalize()
    private val pubName = "${extension.pubName}$variantCap"
    private var dokka = extension.dokka
    private val pubComponent = extension.pubComponent
    private val sourceSetName = extension.sourceSetName
    private val ext = (project as ExtensionAware).extensions

    @Suppress("unused")
    fun build() {

        System.out.println("start PublicationBuilder.build")

        /*
            Create a publication tasks
                "publish${pubName}${variantCap}ToMaven${pubName}${variantCap}Repository"
                "publish${pubName}${variantCap}ToMavenLocal"
         */

        // TODO Without this sign will fail. may be gradle 6.2 will fix this?
        // https://discuss.gradle.org/t/unable-to-publish-artifact-to-mavencentral/33727/3
        project.tasks.withType<GenerateModuleMetadata> {
            enabled = false
        }

        if (project == project.rootProject && project.childProjects.isNotEmpty()) {

            project.logger.debug("Configure root project '${project.name}' for multi-project publishing")

            if (!project.rootProject.pluginManager.hasPlugin("io.hkhc.simplepublisher")) {
                if (extension.ossArtifactory) {
                    project.convention.getPluginByName<ArtifactoryPluginConvention>("artifactory").config()
                }
            }
        } else {

            if (project == project.rootProject) {
                project.logger.debug("Configure project '${project.name}' for single-project publishing")
            } else {
                project.logger.debug("Configure child project '${project.name}' for multi-project publishing")
            }

            if (dokka == null) {
                dokka = project.tasks.named("dokka")
            }

            ext.findByType(PublishingExtension::class.java)?.config(pubComponent)
            if (extension.signing) {
                ext.findByType(SigningExtension::class.java)?.config(extension.useGpg)
            }
            if (extension.bintray) {
                ext.findByType(BintrayExtension::class.java)?.config()
                project.tasks.named("bintrayUpload").get().apply {
                    dependsOn("_bintrayRecordingCopy")
                }
            }
            if (extension.ossArtifactory) {
                project.convention.getPluginByName<ArtifactoryPluginConvention>("artifactory").config()
            }

            if (extension.gradlePlugin) {
                ext.findByType(PluginBundleExtension::class.java)?.config()
                ext.findByType(GradlePluginDevelopmentExtension::class.java)?.config()
            }

            updatePluginPublication()

            TaskBuilder(project, pom, extension, pubName).build()

        }

        project.logger.warn("POM = $pom")

    }

    /**
        The gradle publish plugin hardcoded to use project name as publication artifact name.
        We further customize that publication here and replace it with pom.name
        The default value of pom.name is still project.name so we are not violating the Gradle convention.
     */
    private fun updatePluginPublication() {

        project.extensions.configure(PublishingExtension::class.java) {
            publications.find { it.name.startsWith("publishPluginMavenPublication") }?.let {
                if (it is MavenPublication) {
                    it.artifactId = pom.name
                }
            }
        }
    }

    private fun PublishingExtension.config(
        pubComponent: String = "java"
    ) {

        publications {
            createPublication(pubComponent = pubComponent)
        }

        repositories {
            createRepository()
        }
    }

    private fun SigningExtension.config(useGpg: Boolean) {

        if (useGpg) {
            useGpgCmd()
        }

        val publishingExtension = ext.findByType(PublishingExtension::class.java)

        if (pom.isSnapshot()) {
            project.logger.info("Not performing signing for SNAPSHOT artifact")
        }

        isRequired = !pom.isSnapshot()

        publishingExtension?.let { sign(it.publications[pubName]) }
    }

    @Suppress("unused")
    private fun BintrayExtension.config() {

        override = true
        dryRun = true
        publish = true

        user = pubConfig.bintrayUser
        key = pubConfig.bintrayApiKey

        if (extension.gradlePlugin) {
            setPublications(pubName, "${pubName}PluginMarkerMaven")
        } else {
            setPublications(pubName)
        }

        pom.fill(pkg)

        System.out.println("bintray filesSpec ${project.buildDir}/libs")

        // Bintray requires our private key in order to sign archives for us. I don't want to share
        // the key and hence specify the signature files manually and upload them.
        filesSpec(closureOf<RecordingCopyTask> {
            from("${project.buildDir}/libs").apply {
                include("*.aar.asc")
                include("*.jar.asc")
            }

            from("${project.buildDir}/publications/$pubName").apply {
                include("pom-default.xml.asc")
                rename("pom-default.xml.asc",
                    "${pom.name}-${pom.version}.pom.asc")
            }
            into("${pom.group!!.replace('.', '/')}/${pom.name}/${pom.version}")
        })
    }

    private fun ArtifactoryPluginConvention.config() {

        setContextUrl("https://oss.jfrog.org")
        publish(delegateClosureOf<PublisherConfig> {
            repository(delegateClosureOf<GroovyObject> {
                setProperty("repoKey", "oss-snapshot-local")
                setProperty("username", pubConfig.bintrayUser)
                setProperty("password", pubConfig.bintrayApiKey)
                setProperty("maven", true)
            })
            defaults(delegateClosureOf<GroovyObject> {
                if (extension.gradlePlugin) {
                    invokeMethod("publications", listOf(pubName, "${pubName}PluginMarkerMaven"))
                } else {
                    invokeMethod("publications", pubName)
                }
                setProperty("publishArtifacts", true)
                setProperty("publishPom", true)
            })
        })

        resolve(delegateClosureOf<ResolverConfig> {
            setProperty("repoKey", "jcenter")
        })

        project.tasks.register("artifactory${pubName.capitalize()}Publish", ArtifactoryTask::class) {
            publications(pubName)
            if (project.rootProject == project && project.childProjects.isNotEmpty()) {
                skip = true
            }
        }
    }

    @Suppress("UnstableApiUsage")
    private fun setupDokkaJar(): TaskProvider<Jar>? {
        val dokkaJarTaskName = "dokkaJar$variantCap"
        return try {
            project.tasks.named(dokkaJarTaskName, Jar::class.java) {
                group = "Publishing"
                archiveClassifier.set("javadoc")
            }
        } catch (e: UnknownTaskException) {
            // TODO add error message here if dokka is null
            project.tasks.register(dokkaJarTaskName, Jar::class.java) {
                group = "Publishing"
                description = "Assembles Kotlin docs with Dokka to Jar"
                archiveClassifier.set("javadoc")
                from(dokka)
                dependsOn(dokka)
            }
        }
    }

    @Suppress("UnstableApiUsage")
    private fun setupSourcesJar(sourceSetName: String): TaskProvider<Jar>? {

        val sourcesTaskName = "sourcesJar$variantCap"
        return try {
            project.tasks.named(sourcesTaskName, Jar::class.java) {
                archiveClassifier.set("sources")
            }
        } catch (e: UnknownTaskException) {
            project.tasks.register(sourcesTaskName, Jar::class.java) {
                group = "Publishing"
                description = "Create archive of source code for the binary"
                archiveClassifier.set("sources")
                from(project.sourceSets.getByName(sourceSetName).allSource)
            }
        }
    }

    private fun PublicationContainer.createPublication(pubComponent: String) {

        val dokkaJar = setupDokkaJar()
        val sourcesJar = setupSourcesJar(sourceSetName)

        val pomSpec = pom

        register(pubName, MavenPublication::class.java) {

            groupId = pomSpec.group

            artifactId = pomSpec.name
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
        maven {
            name = "Maven${pubName.capitalize()}"
            with(pubConfig) {
                url = project.uri(
                    if (pom.isSnapshot()) {
                        nexusSnapshotRepositoryUrl!!
                    } else {
                        nexusReleaseRepositoryUrl!!
                    }
                )

                credentials {
                    username = nexusUsername!!
                    password = nexusPassword!!
                }
            }
        }
    }

    private fun PluginBundleExtension.config() {
        website = pom.web.url
        vcsUrl = pom.scm.url ?: website
        description = pom.plugin?.description ?: pom.description
        tags = pom.plugin?.tags ?: listOf()
    }

    private fun GradlePluginDevelopmentExtension.config() {

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

    private val Project.sourceSets: SourceSetContainer get() =
        ext.getByName("sourceSets") as SourceSetContainer
}
