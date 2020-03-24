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
import org.gradle.api.GradleException
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

/**
 *
 *
 * Build phases:
 *
 * phase 1: before all project evaluation listeners (after evaluate)
 *  - bintray extension
 * - setup ossArtifactory
 *
 *  **** bintray plugin project evaluation listener (after evaluate)
 *      - set task dependency
 *      - assign file spec to bintrayUpload tasks
 * phase 2: after all project evaluation listeners (after evaluate)
 *  - setup gradle plugin portal
 *  - setup version of  pluginMavenPublication task
 *
 * ------
 * Script execution
 * configure android library extension
 * ------
 *
 * phase 3: before all project.afterEvaluate
 *  **** android sourcesset and component creation
 *  **** configure android library variant
 *  **** configure simply publisher variant
 *  **** configure gradle plugin portal
 *
 * phase 4: after all project.afterEvaluate
 * - setup dokka task
 * - setup publishing extension
 *   - setup publications
 *      - setup dokkaJar task
 *      - setup sourcessetJar task
 *   - setup repository
 * - setup signing
 *
 * phase 5: project evaluated
 *  **** bintray plugin project evaluation listener (project evaluated)
 *  - Create facade tasks
 */
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
//    private val sourceSetName = extension.sourceSetName
    private val ext = (project as ExtensionAware).extensions

    companion object {

        private fun Project.isMultiProjectRoot() =
            rootProject == this && childProjects.isNotEmpty()

        private fun printHelpForMissingDokka(project: Project) {
            project.logger.warn("""
            We cannot find a dokka task in project '${project.name}'.
            By default, we will look for the declaration like this: 
                tasks {
                    dokka {
                        ...
                    }
                }
            If you defined a dokka with different name, you may specify it in $SP_EXT_NAME block:
                $SP_EXT_NAME {
                    ...
                    dokka = myDokkaTask
                    ...
                }
        """.trimIndent())
        }

        /*
            The gradle publish plugin hardcoded to use project name as publication artifact name.
            We further customize that publication here and replace it with pom.name
            The default value of pom.name is still project.name so we are not violating the Gradle convention.
            "publishPluginMavenPublication*" task is created by gradle plugin publish plugin.
         */
        private fun updatePluginPublication(project: Project, artifactId: String) {

            project.extensions.configure(PublishingExtension::class.java) {
                publications.find { it.name.startsWith("publishPluginMavenPublication") }?.let {
                    if (it is MavenPublication) {
                        it.artifactId = artifactId
                    }
                }
            }
        }
    }

    @Suppress("unused")
    fun buildPhase1() {
        project.logger.debug("BimplePublisher Builder phase 1")
        project.setupPublisher1()
    }

    @Suppress("unused")
    fun buildPhase2() {
        project.logger.debug("BimplePublisher Builder phase 2")
        project.setupPublisher2()
    }

    @Suppress("unused")
    fun buildPhase3() {
        project.logger.debug("BimplePublisher Builder phase 3")
        project.setupPublisher3()
    }

    @Suppress("unused")
    fun buildPhase4() {
        project.logger.debug("BimplePublisher Builder phase 4")
        project.setupPublisher4()
    }

    @Suppress("unused")
    fun buildPhase5() {
        project.logger.debug("BimplePublisher Builder phase 5")
        project.setupPublisher5()
    }

    private fun Project.setupPublisher1() {

        // TODO Without this sign will fail. may be gradle 6.2 will fix this?
        // https://discuss.gradle.org/t/unable-to-publish-artifact-to-mavencentral/33727/3
        // TODO 2 shall we add .configureEach after withType as suggested by
        // https://blog.gradle.org/preview-avoiding-task-configuration-time
        tasks.withType<GenerateModuleMetadata> {
            enabled = false
        }

        if (isMultiProjectRoot()) {

            logger.debug("Configure root project '$name' for multi-project publishing")

            if (!rootProject.pluginManager.hasPlugin("io.hkhc.simplepublisher")) {
                if (extension.ossArtifactory) {
                    convention.getPluginByName<ArtifactoryPluginConvention>("artifactory").config()
                }
            }
        } else {

            if (this == rootProject) {
                logger.info("Configure project '$name' for single-project publishing")
            } else {
                logger.info("Configure child project '$name' for multi-project publishing")
            }

            if (extension.bintray) {
                ext.findByType(BintrayExtension::class.java)?.config()
                /*
                    Why does bintrayUpload not depends on _bintrayRecordingCopy by default?
                 */
                tasks.named("bintrayUpload").get().apply {
                    dependsOn("_bintrayRecordingCopy")
                }
            }

            if (extension.ossArtifactory) {
                convention.getPluginByName<ArtifactoryPluginConvention>("artifactory").config()
            }
        }
    }

    private fun Project.setupPublisher2() {

        if (extension.gradlePlugin) {
            ext.findByType(PluginBundleExtension::class.java)?.config()
            ext.findByType(GradlePluginDevelopmentExtension::class.java)?.config()
            updatePluginPublication(this, pom.name!!)
        }

        if (!isMultiProjectRoot()) {
            // do nothing intentionally as a placeholder for future enhancement
        }
    }

    private fun Project.setupPublisher3() {
        // nothing
    }

    private fun Project.setupPublisher4() {

        if (!isMultiProjectRoot()) {

            if (dokka == null) {
                try {
                    dokka = tasks.named("dokka")
                } catch (e: UnknownTaskException) {
                    printHelpForMissingDokka(this)
                    throw GradleException("Failed to found 'dokka' task")
                }
            }

            ext.findByType(PublishingExtension::class.java)?.config(pubComponent)

            if (extension.signing) {
                ext.findByType(SigningExtension::class.java)?.config(extension.useGpg)
            }
        }
    }

    private fun Project.setupPublisher5() {
        TaskBuilder(this, pom, extension, pubName).build()
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

    @Suppress("UnstableApiUsage")
    private fun setupDokkaJar(): TaskProvider<Jar>? {
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

            val path = extension.sourcesPath ?: project.sourceSets.getByName(extension.sourceSetName).allSource

            project.tasks.register(sourcesJarTaskName, Jar::class.java) {
                group = PUBLISH_GROUP
                description = desc
                archiveClassifier.set(CLASSIFIER_SOURCE)
                from(path)
            }
        }
    }

    private fun PublicationContainer.createPublication(pubComponent: String) {

        val dokkaJar = setupDokkaJar()
        val sourcesJar = setupSourcesJar()

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
