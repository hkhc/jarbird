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

import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.RecordingCopyTask
import io.hkhc.gradle.JarbirdExtension
import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.utils.LOG_PREFIX
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.kotlin.dsl.closureOf
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class BintrayConfig(
    private val project: Project,
    private val extension: JarbirdExtension,
    private val pubs: List<JarbirdPub>
) {

    fun config() {
        project.logger.debug("$LOG_PREFIX configure Bintray plugin")
        val bintrayExt = project.findByType(BintrayExtension::class.java)
            ?: throw GradleException("Bintray extension is not found, may be Bintray plugin is not applied?")
        bintrayExt.config()

        /*
            Why does bintrayUpload not depends on _bintrayRecordingCopy by default?
         */
        project.tasks.named("bintrayUpload").get().apply {
            // Bintray Gradle Plugin expect RecordingCopyTask dependency in task directly rather than name in String
            dependsOn(project.tasks.named("_bintrayRecordingCopy").get())
        }

        /*
            Bintray does not do signing implicitly, we add our own dependency to make sure publication is signed.
         */
        project.tasks.named("_bintrayRecordingCopy").get().apply {
            pubs.bintrayPubList().forEach {
                dependsOn("sign${it.capitalize()}Publication")
            }
        }
    }

    // Bintray can perform component signing on behalf of us. However it requires our private key in order to sign
    // archives for us. I don't want to share the key and hence specify the signature files manually and upload
    // them.
    @Suppress("SpreadOperator")
    private fun BintrayExtension.includeSignatureFiles() {

        val includeFileList = pubs
            .filter { it.bintray }
            .filter { !(it.pom.isGradlePlugin() && it.pom.isSnapshot()) }
            .fold(mutableListOf<String>()) { acc, pub ->
                if (pub.pom.group == null) {
                    throw GradleException(
                        "Bintray: group name is not available, " +
                            "failed to configure Bintray extension."
                    )
                }
                acc.apply {
                    add("${pub.pom.artifactId}-${pub.variantVersion()}*.asc")
                }
            }
            .toTypedArray()

        filesSpec(
            closureOf<RecordingCopyTask> {

                pubs
                    .filter { it.bintray }
                    .filter { !(it.pom.isGradlePlugin() && it.pom.isSnapshot()) }
                    .forEach {
                        val groupDir = it.pom.group?.replace('.', '/')
                        val filenamePrefix = "${it.pom.artifactId}-${it.variantVersion()}"
                        from("${project.buildDir}/outputs/aar") {
                            include("*.aar.asc")
                            rename { _ -> "${it.pom.artifactId}-${it.variantVersion()}.aar.asc" }
                        }
                        from("${project.buildDir}/publications/${it.pubNameWithVariant()}") {
                            include("pom-default.xml.asc")
                            rename("pom-default.xml.asc", "$filenamePrefix.pom.asc")
                        }
                        into("$groupDir/${it.pom.artifactId}/${it.variantVersion()}")
                    }
                from("${project.buildDir}/libs") {
                    include(*includeFileList)
                }
            }
        )
    }

    @Suppress("SpreadOperator")
    private fun BintrayExtension.config() {

        val firstBintrayPom = pubs.firstOrNull { it.bintray }
        if (firstBintrayPom == null) return

        extension.bintrayRepository?.let { endpoint ->
            if (endpoint.releaseUrl != "") apiUrl = endpoint.releaseUrl
            if (endpoint.username != "") user = endpoint.username
            if (endpoint.apikey != "") key = endpoint.apikey

            override = true
            dryRun = false
            publish = true
        }

        // TODO if we are publishing gradle plugin, we shall use the publication from plugin development plugin directly
        val publicationsList = pubs.bintrayPubList()
        val gradlePublicationsList = publicationsList + pubs.bintrayGradlePluginPubList()

        setPublications(*(gradlePublicationsList.toTypedArray()))

        pkg.fill(firstBintrayPom)

        includeSignatureFiles()
    }

    fun BintrayExtension.PackageConfig.fill(pub: JarbirdPub) {

        val pom = pub.pom

        val labelList = pom.bintray.labels?.split(',')?.toTypedArray() ?: arrayOf()
        val licenseList = pom.licenses.map { it.name }.toTypedArray()

        repo = pom.bintray.repo ?: "maven"
        pom.bintray.userOrg?.let { userOrg = it }
        name = pom.artifactId
        desc = pom.description
        @Suppress("SpreadOperator")
        setLicenses(*licenseList)
        websiteUrl = pom.web.url ?: ""
        vcsUrl = pom.scm.url ?: ""
        if (pom.scm.repoType == "github.com") {
            githubRepo = pom.scm.repoName ?: ""
            pom.scm.githubReleaseNoteFile?.let { githubReleaseNotesFile = it }
        }
        issueTrackerUrl = pom.scm.issueUrl ?: ""
        version.apply {
            name = pub.variantVersion()
            desc = pom.description
            released = currentZonedDateTime()
            vcsTag = pub.variantVersion()
        }
        @Suppress("SpreadOperator")
        setLabels(*labelList)
    }

    private fun currentZonedDateTime(): String =
        ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ"))
}

internal fun List<JarbirdPub>.bintrayPubList() =
    filter { it.bintray && !(it.pom.isGradlePlugin() && it.pom.isSnapshot()) }
        .map { it.pubNameWithVariant() }
internal fun List<JarbirdPub>.bintrayGradlePluginPubList() =
    filter { it.bintray && it.pom.isGradlePlugin() && !it.pom.isSnapshot() }
        .map { it.pubNameWithVariant() + "PluginMarkerMaven" }
