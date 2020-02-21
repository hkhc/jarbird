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

import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.closureOf
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.gradle.plugins.signing.SigningExtension
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun PublicationContainer.createPublication(
    project: Project,
    pubName: String,
    pubComponent: String,
    variant: String = ""
    ) {

    val variantCap = variant.capitalize()
    val pubConfig = PublishConfig(project)
    val dokkaJar = project.tasks.named("dokkaJar$variantCap", Jar::class.java) {
        archiveClassifier.set("javadoc")
    }
    val sourcesJar = project.tasks.named("sourcesJar$variantCap", Jar::class.java) {
        archiveClassifier.set("sources")
    }


    create<MavenPublication>("$pubName$variantCap") {

        with(pubConfig) {

            groupId = artifactGroup

            // The default artifactId is project.name
            // artifactId = artifactEyeD
            // version is gotten from an external plugin
            //            version = project.versioning.info.display
            version = artifactVersion
            // This is the main artifact
            from(project.components[pubComponent])
            // We are adding documentation artifact
            artifact(dokkaJar.get())
            // And sources
            artifact(sourcesJar.get())


            // See https://maven.apache.org/pom.html for POM definitions

            pom {
                name.set(artifactEyeD)
                description.set(pomDescription)
                url.set(pubConfig.pomUrl)
                licenses {
                    license {
                        name.set(licenseName)
                        url.set(licenseUrl)
                    }
                }
                developers {
                    developer {
                        id.set(developerId)
                        name.set(developerName)
                        email.set(developerEmail)
                    }
                }
                scm {
                    connection.set(scmConnection)
                    developerConnection.set(scmConnection)
                    url.set(scmUrl)
                }
            }

            // TODO dependency versionMapping

        }



    }

}

fun RepositoryHandler.createRepository(project: Project) {

    val pubConfig = PublishConfig(project)

    maven {

        with(pubConfig) {

            url = project.uri(
                if (project.version.toString().endsWith("SNAPSHOT"))
                    nexusSnapshotRepositoryUrl!!
                else
                    nexusReleaseRepositoryUrl!!
            )
            credentials {
                username = nexusUsername!!
                password = nexusPassword!!
            }

        }

    }

}

/**
 * @param pubName Form the maven publication name in publishing extension together with variant
 * @param variant To suffix the name of taaks and publication, so that multiple publications
 * can co-exist
 * @param pubComponent The component name to publish, it is usually "java" for ordinary jar
 * archives, and "android" for Android AAR
 */
fun Project.publishingConfig(
    pubName: String,
    variant: String = "",
    pubComponent: String = "java"
) {

    val pubConfig = PublishConfig(project)
    val variantCap = variant.capitalize()
    val ext = (this as ExtensionAware).extensions

    ext.findByType(PublishingExtension::class.java)?.config(this, pubName, variantCap, pubComponent)
    if (!pubConfig.artifactVersion.endsWith("-SNAPSHOT")?:false) {
        ext.findByType(SigningExtension::class.java)?.config(this, pubName, variantCap)
    }
    ext.findByType(BintrayExtension::class.java)?.config(this, pubName, variantCap)

}

fun PublishingExtension.config(
    project: Project,
    pubName: String,
    variant: String = "",
    pubComponent: String = "java"
) {

    val variantCap = variant.capitalize()

    publications {
        createPublication(
            project = project,
            pubName = pubName,
            pubComponent = pubComponent,
            variant = variantCap)
    }
    repositories {
        createRepository(project)
    }

}

fun currentZonedDateTime() =
    ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ"))

fun BintrayExtension.config(
    project: Project,
    pubName: String,
    variant: String = ""
    ) {

    val variantCap = variant.capitalize()
    val pubConfig = PublishConfig(project)
    val labelList = pubConfig.bintrayLabels?.split(',') ?: emptyList()
    val labelArray = Array(labelList.size) { labelList[it] }

    override = true
    dryRun = false
    publish = true

    user = pubConfig.bintrayUser
    key = pubConfig.bintrayApiKey
    setPublications("$pubName$variantCap")

    pkg.apply {
        repo = "maven"
        name = pubConfig.artifactEyeD
        desc = pubConfig.pomDescription!!
        setLicenses(pubConfig.licenseName!!)
        websiteUrl = pubConfig.scmUrl!!
        vcsUrl = pubConfig.scmUrl!!
        githubRepo = pubConfig.scmGithubRepo!!
        issueTrackerUrl = pubConfig.issuesUrl!!
        version.apply {
            name = pubConfig.artifactVersion!!
            desc = pubConfig.pomDescription!!
            released = currentZonedDateTime()
            vcsTag = pubConfig.artifactVersion!!
        }
        setLabels(*labelArray)
    }

    // Bintray requires our private key in order to sign archives for us. I don't want to share
    // the key and hence specify the signature files manually and upload them.
    filesSpec(closureOf<com.jfrog.bintray.gradle.tasks.RecordingCopyTask> {
        from("${project.buildDir}/libs").apply {
            include("*.aar.asc")
            include("*.jar.asc")
        }
        from("${project.buildDir}/publications/$pubName$variantCap").apply {
            include("pom-default.xml.asc")
            rename("pom-default.xml.asc",
                "${pubConfig.artifactEyeD}-${pubConfig.artifactVersion}.pom.asc")
        }
        into("${(pubConfig.artifactGroup as String)
            .replace('.', '/')}/${pubConfig.artifactEyeD}/${pubConfig.artifactVersion}")
    })

}

fun SigningExtension.config(
    project: Project,
    pubName: String,
    variant: String = ""
) {

    val variantCap = variant.capitalize()
    val ext = (project as ExtensionAware).extensions
    val publishingExtension = ext.findByType(PublishingExtension::class.java)

    publishingExtension?.let { sign(it.publications["$pubName$variantCap"]) }

}
