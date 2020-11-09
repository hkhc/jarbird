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

    val publishPlan = BintrayPublishPlan(pubs)

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
            publishPlan.bintrayLibs.map { it.pubNameWithVariant() }.forEach {
                dependsOn("sign${it.capitalize()}Publication")
            }
        }
    }

    fun filenameBase(pub: JarbirdPub) = "${pub.variantArtifactId()}-${pub.variantVersion()}"

    // Bintray can perform component signing on behalf of us. However it requires our private key in order to sign
    // archives for us. I don't want to share the key and hence specify the signature files manually and upload
    // them.
    @Suppress("SpreadOperator")
    private fun BintrayExtension.includeSignatureFiles() {

        val includeFileList = publishPlan.bintray.map { "${filenameBase(it)}*.asc" }
            .toTypedArray()

        filesSpec(
            closureOf<RecordingCopyTask> {

                publishPlan.bintray
                    .forEach {
                        val groupDir = it.pom.group?.replace('.', '/')
                        from("${project.buildDir}/outputs/aar") {
                            include("*.aar.asc")
                            rename { _ -> "${filenameBase(it)}.aar.asc" }
                        }
                        from("${project.buildDir}/publications/${it.pubNameWithVariant()}") {
                            include("pom-default.xml.asc")
                            rename("pom-default.xml.asc", "${filenameBase(it)}.pom.asc")
                        }
                        into("$groupDir/${it.variantArtifactId()}/${it.variantVersion()}")
                    }
                from("${project.buildDir}/libs") {
                    include(*includeFileList)
                }
            }
        )
    }

    @Suppress("SpreadOperator")
    private fun BintrayExtension.config() {

        val publishPlan = BintrayPublishPlan(pubs)
        val firstBintrayPom = pubs.firstOrNull { it.bintray } ?: return

        extension.bintrayRepository?.let { endpoint ->
            if (endpoint.releaseUrl != "") apiUrl = endpoint.releaseUrl
            if (endpoint.username != "") user = endpoint.username
            if (endpoint.apikey != "") key = endpoint.apikey

            override = true
            dryRun = false
            publish = true
        }

        setPublications(*(publishPlan.bintrayPublications().toTypedArray()))

        pkg.fill(firstBintrayPom)

        includeSignatureFiles()
    }

    @Suppress("SpreadOperator")
    fun BintrayExtension.PackageConfig.fill(pub: JarbirdPub) {

        val pom = pub.pom

        val labelList = pom.bintray.labels?.split(',')?.toTypedArray() ?: arrayOf()
        val licenseList = pom.licenses.map { it.name }.toTypedArray()

        repo = pom.bintray.repo ?: "maven"
        pom.bintray.userOrg?.let { userOrg = it }
        name = pub.variantArtifactId()
        desc = pom.description
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
        setLabels(*labelList)
    }

    private fun currentZonedDateTime(): String =
        ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ"))
}
