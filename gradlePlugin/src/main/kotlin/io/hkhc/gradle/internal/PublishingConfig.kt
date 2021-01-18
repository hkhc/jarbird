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

import io.hkhc.gradle.JarbirdExtension
import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.internal.dokka.DokkaConfig
import io.hkhc.gradle.internal.maven.MavenPomAdapter
import io.hkhc.gradle.internal.repo.MavenRepoSpec
import io.hkhc.gradle.internal.utils.Version
import io.hkhc.gradle.internal.utils.detailMessageWarning
import io.hkhc.gradle.internal.utils.findByType
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named

internal class PublishingConfig(
    private val project: Project,
    private val extension: JarbirdExtension,
    private val pubs: List<JarbirdPub>
) {
    private val extensions = (project as ExtensionAware).extensions
    // TODO we shall have one separate dokka task per pub
//    private var dokka = pubs.map { it.dokka }.find { it != null }
    // private var dokka = project.tasks.named("dokkaHtml").get()

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

//        if (dokka == null) {
//            try {
//                dokka = project.tasks.named("dokka")
//            } catch (e: UnknownTaskException) {
//                printHelpForMissingDokka(project)
//            }
//        }

        (
            project.findByType(PublishingExtension::class.java)
                ?: throw GradleException(
                    "\"publishing\" extension is not found. " +
                        "Maybe \"org.gradle.maven-publish\" is not applied?"
                )
            ).config()
    }

//    fun createDokkaSourceRoots(pub: JarbirdPubImpl): Iterable<File> {
//
//        return if (pub.sourceSets == null) {
//            sourceSets.getByName(pub.sourceSetName).allSource.srcDirs
//        } else {
//            pub.sourceSets ?: listOf()
//        }
//    }

    private fun PublishingExtension.config() {

        publications {
            createPublication()
        }

        if (pubs.needsNonLocalMaven()) {
            repositories {
                createRepository()
            }
        }
    }

    private fun PublishArtifact.getString() =
        "PublishArtifact(name=$name,file=$file,classifier=$classifier,date=$date,extension=$extension,type=$type)"

//    private fun checkComponent(pub: JarbirdPub) {
//        try {
//            project.components[pub.pubComponent]
//        } catch (e: UnknownDomainObjectException) {
//            project.logger.error(
//                """
//                    The component '${pub.pubComponent}' is not found.
//                    Available component(s) are : ${project.components.joinToString(", ") { it.name }}
//                    set it with pub block, e.g.
//                        pub {
//                            pubComponent = "${project.components.firstOrNull()?.name ?: "your-component"}"
//                        }
//                """.trimIndent()
//            )
//            throw GradleException("Component '${pub.pubComponent}' is not found.")
//        }
//    }

    private fun registerSourceSetCompileTask(pub: JarbirdPubImpl): TaskProvider<Jar>? {
        return pub.sourceSet?.let { sourceSet ->

            if (sourceSet.name != "main") {
                ClassesJarTaskInfo(pub).register(project.tasks, Jar::class.java) {
                    archiveBaseName.set(pub.variantArtifactId())
                    archiveVersion.set(pub.variantVersion())
                    from(project.configurations["${sourceSet.name}Compile"])
                    dependsOn(
                        project.tasks.named("${sourceSet.name}Classes")
                    )
                }
            } else {
                project.tasks.named<Jar>("jar").apply {
                    configure {
                        archiveBaseName.set(pub.variantArtifactId())
                        archiveVersion.set(pub.variantVersion())
                    }
                }
            }
        }
    }

    private fun PublicationContainer.createPublication() {

        pubs.forEach { pub0 ->

            val pub = pub0 as JarbirdPubImpl
            val pom = pub.pom

//            checkComponent(pub)

//            if (!project.isMultiProjectRoot()) {
//
//                if (project.configurations.findByName("helloConfiguration") == null) {
//                    project.configurations.create("helloConfiguration") {
//                        outgoing {
//                            artifact(project.tasks.named("jar"))
//                        }
//                    }
//                    val adhoc = project.components["java"] as AdhocComponentWithVariants
//                    adhoc.addVariantsFromConfiguration(project.configurations["helloConfiguration"]) {
//                        this.mapToOptional()
//                    }
//                }
//
//                project.components.forEach {
//                    println("after new configuration components ${it.name}")
//                }
//            }

            val publishJarTask = registerSourceSetCompileTask(pub)

            register(pub.pubNameWithVariant(), MavenPublication::class.java) {

                groupId = pom.group

                artifactId = pub.variantArtifactId()

                // version is gotten from an external plugin
                //            version = project.versioning.info.display
                version = pub.variantVersion()

                if (publishJarTask == null) {
                    val effectiveComponent = pub.component ?: project.components["java"]
                    from(effectiveComponent)
                } else {
                    artifactCompat(publishJarTask)
                }
                // We are adding documentation artifact
                if (!project.isMultiProjectRoot()) {

                    /*
                     https://github.com/gradle/gradle/pull/13505
                     artifact supports TaskProvider from Gradle 6.6
                     */

                    project.afterEvaluate {
                        artifactCompat(DokkaConfig(project, extension).setupDokkaJar(pub))
                        artifactCompat(SourceConfig(project).configSourceJarTask(pub, pub.sourceSet ?: pub.docSourceSets))
                    }
                }
                pom { MavenPomAdapter().fill(this, pom) }
            }
        }
    }

    private fun MavenPublication.artifactCompat(source: Any) {
        if (Version(project.gradle.gradleVersion) >= Version("6.6")) {
            artifact(source)
        } else {
            if (source is TaskProvider<*>) {
                artifact(source.get())
            } else {
                artifact(source)
            }
        }
    }

    private fun RepositoryHandler.createRepository() {

        val releaseEndpoints = pubs
            .filter { !it.pom.isSnapshot() }
            .flatMap { it.getRepos() }
            .filterIsInstance<MavenRepoSpec>()
            .toSet()
        val snapshotEndpoint = pubs
            .filter { it.pom.isSnapshot() }
            .flatMap { it.getRepos() }
            .filterIsInstance<MavenRepoSpec>()
            .toSet()

        releaseEndpoints.forEach {

            maven {
                with(it) {
                    val ep = this
                    name = ep.id
                    url = project.uri(ep.releaseUrl)
                    credentials {
                        username = ep.username
                        password = ep.password
                    }
                }
            }
        }

        snapshotEndpoint.forEach {

            maven {
                with(it) {
                    val ep = this
                    name = ep.id
                    url = project.uri(ep.snapshotUrl)
                    credentials {
                        username = ep.username
                        password = ep.password
                    }
                }
            }
        }
    }
}
