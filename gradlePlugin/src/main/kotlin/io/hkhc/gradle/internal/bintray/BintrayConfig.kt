/*
 * Copyright (c) 2021. Herman Cheung
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

package io.hkhc.gradle.internal.bintray

import com.jfrog.bintray.gradle.BintrayExtension
import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.internal.JarbirdExtensionImpl
import io.hkhc.gradle.internal.LOG_PREFIX
import io.hkhc.gradle.internal.needsBintray
import io.hkhc.gradle.internal.repo.BintrayRepoSpec
import io.hkhc.gradle.internal.utils.findByType
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class BintrayConfig(
    private val project: Project,
    private val extension: JarbirdExtensionImpl,
    private val pubs: List<JarbirdPub>
) {

//    val publishPlan = BintrayPublishPlan(pubs)

    fun config() {
        project.logger.debug("$LOG_PREFIX configure Bintray plugin")
        val bintrayExt = project.findByType(BintrayExtension::class.java)
            ?: throw GradleException("Bintray extension is not found, may be Bintray plugin is not applied?")
        bintrayExt.config()

        /*
            Why does bintrayUpload not depends on _bintrayRecordingCopy by default?
         */
//        project.tasks.named("bintrayUpload") {
//            /*
//                *** Important
//                Bintray Gradle Plugin expect RecordingCopyTask dependency in task directly rather than name in String
//             */
//            dependsOn(project.tasks.named("_bintrayRecordingCopy").get())
//        }

        /*
            Bintray does not do signing implicitly, we add our own dependency to make sure publication is signed.
         */
//        project.tasks.named("_bintrayRecordingCopy") {
//            println("bintrayLibs count ${publishPlan.bintrayLibs.size}")
//            publishPlan.bintrayLibs.map { it.pubNameWithVariant() }.forEach {
//                println("bintrayLibs dependsOn $it sign${it.capitalize()}Publication")
//                dependsOn("sign${it.capitalize()}Publication")
//            }
//        }
    }

    @Suppress("SpreadOperator")
    private fun BintrayExtension.config() {

        val publishPlan = BintrayPublishPlan(pubs)

        val firstBintrayPom = pubs.firstOrNull { it.needsBintray() && !it.pom.isSnapshot() } ?: return

        pubs.flatMap { it.getRepos() }
            .filterIsInstance<BintrayRepoSpec>()
            .first()
            .apply {
                if (releaseUrl != "") apiUrl = releaseUrl
                if (username != "") user = username
                if (apikey != "") key = apikey

                override = true
                dryRun = false
                publish = true
            }

        setPublications(*(publishPlan.bintrayPublications().toTypedArray()))

        pkg.fill(firstBintrayPom)
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
