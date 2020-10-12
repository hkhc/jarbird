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
import io.hkhc.gradle.pom.Pom
import io.hkhc.gradle.utils.LOG_PREFIX
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.kotlin.dsl.closureOf
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class BintrayConfig(
    private val project: Project,
    private val extension: JarbirdExtension,
    private val pom: Pom
) {

    private val pubName = extension.pubNameWithVariant()

    fun config() {
        project.logger.debug("$LOG_PREFIX configure Bintray plugin")
        val bintrayExt = project.findByType(BintrayExtension::class.java)
            ?: throw GradleException("Bintray extension is not found, may be Bintray plugin is not applied?")
        bintrayExt.config()

        /*
            Why does bintrayUpload not depends on _bintrayRecordingCopy by default?
         */
        project.tasks.named("bintrayUpload").get().apply {
            dependsOn("_bintrayRecordingCopy")
        }

        /*
            Make sure the build has finished before performing the copying task.
         */
        project.tasks.named("_bintrayRecordingCopy").get().apply {
            dependsOn("publish${pubName.capitalize()}PublicationToMavenLocal")
        }
    }

    // Bintray can perform component signing on behalf of us. However it requires our private key in order to sign
    // archives for us. I don't want to share the key and hence specify the signature files manually and upload
    // them.
    private fun BintrayExtension.includeSignatureFiles() {

        if (pom.group == null) {
            throw GradleException("Bintray: group name is not available, failed to configure Bintray extension.")
        }

        val groupDir = pom.group?.replace('.', '/')
        val filenamePrefix = "${pom.artifactId}-${pom.version}"

        filesSpec(
            closureOf<RecordingCopyTask> {
                from("${project.buildDir}/libs") {
                    include(
                        "$filenamePrefix*.aar.asc",
                        "$filenamePrefix*.jar.asc"
                    )
                }
                from("${project.buildDir}/publications/$pubName") {
                    include("pom-default.xml.asc")
                    rename("pom-default.xml.asc", "$filenamePrefix.pom.asc")
                }
                into("$groupDir/${pom.artifactId}/${pom.version}")
            }
        )
    }

    private fun BintrayExtension.config() {

        val endpoint = extension.bintrayRepository!!

        if (endpoint.releaseUrl != "") {
            apiUrl = endpoint.releaseUrl
        }

        override = true
        dryRun = false
        publish = true

        if (endpoint.username != "") user = endpoint.username
        if (endpoint.apikey != "") key = endpoint.apikey

        if (pom.isGradlePlugin()) {
            setPublications(pubName, "${pubName}PluginMarkerMaven")
        } else {
            setPublications(pubName)
        }

        pkg.fill(pom)

        includeSignatureFiles()
    }

    fun BintrayExtension.PackageConfig.fill(pom: Pom) {

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
            name = pom.version
            desc = pom.description
            released = currentZonedDateTime()
            vcsTag = pom.version
        }
        @Suppress("SpreadOperator")
        setLabels(*labelList)
    }

    private fun currentZonedDateTime(): String =
        ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ"))
}
