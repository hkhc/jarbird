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

package io.hkhc.gradle.pom

import com.jfrog.bintray.gradle.BintrayExtension
import io.hkhc.util.LOG_PREFIX
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPom
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import java.io.File
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class PomFactory {

    /**
     * read POM spec from a YAML file
     */
    fun Project.readPom(path: String): Pom {
        val file = File(path)
        return if (file.exists()) {
            logger.debug("$LOG_PREFIX File '${file.absolutePath}' found")
            val yaml = Yaml(Constructor(Pom::class.java))
            // it is possible that yaml.load return null even if file exists and
            // is a valid yaml file. For example, a YAML file could be fill of comment
            // and have no real tag.
            return yaml.load(file.readText()) ?: Pom()
        } else {
            logger.debug("$LOG_PREFIX File '${file.absolutePath}' does not exist")
            Pom()
        }
    }

    private fun pomPath(base: String): String {
        return "${base}${File.separatorChar}${Companion.POM_FILENAME}"
    }

    /**
     * resolve POM spec via a series of possible location and accumulate the details
     */
    fun resolvePom(project: Project): Pom {

        val pom = Pom()

        with(project) {
            System.getProperty("gradle.user.home")?.let {
                readPom(pomPath(it)).overlayTo(pom)
            }
            System.getenv("GRADLE_USER_HOME")?.let {
                readPom(pomPath(it)).overlayTo(pom)
            }
            File("${System.getProperty("user.home")}${File.separatorChar}.gradle").also {
                if (it.exists()) {
                    readPom(pomPath(it.absolutePath)).overlayTo(pom)
                }
            }
            readPom(pomPath(project.rootDir.absolutePath)).overlayTo(pom)
            readPom(pomPath(project.projectDir.absolutePath)).overlayTo(pom)
            System.getProperty("pomFile")?.let {
                readPom(it).overlayTo(pom)
            }
        }

        return pom
    }

    fun validatePom() {

//        with(pom) {
//
//        }
    }

    companion object {
        const val POM_FILENAME = "pom.yaml"
    }
}

fun Pom.fillTo(mavenPom: MavenPom) {

    mavenPom.name.set(name)
    mavenPom.description.set(description)
    mavenPom.inceptionYear.set(inceptionYear.toString())
    mavenPom.url.set(url)
    mavenPom.licenses {
        for (lic in licenses) {
            license {
                name.set(lic.name)
                url.set(lic.url)
                comments.set(lic.comments)
            }
        }
    }
    mavenPom.developers {
        for (dev in developers) {
            developer {
                id.set(dev.id)
                name.set(dev.name)
                email.set(dev.email)
                organization.set(dev.organization)
                organizationUrl.set(dev.organizationUrl)
                timezone.set(dev.timeZone)
                url.set(dev.url)
            }
        }
    }
    mavenPom.contributors {
        for (cont in contributors) {
            contributor {
                name.set(cont.name)
                email.set(cont.email)
                organization.set(cont.organization)
                organizationUrl.set(cont.organizationUrl)
                timezone.set(cont.timeZone)
                url.set(cont.url)
            }
        }
    }
    mavenPom.organization {
        name.set(organization.name)
        url.set(organization.url)
    }
    mavenPom.scm {
        connection.set(scm.connection)
        developerConnection.set(scm.developerConnection)
        url.set(scm.url)
        tag.set(scm.tag)
    }
    if (scm.issueType != null && scm.issueUrl != null) {
        mavenPom.issueManagement {
            system.set(scm.issueType)
            url.set(scm.issueUrl)
        }
    }
}

fun Pom.fill(pkg: BintrayExtension.PackageConfig) {

    val labelList = bintray.labels?.split(',')?.toTypedArray() ?: arrayOf()
    val licenseList = licenses.map { it.name }.toTypedArray()

    val pom = this
    @Suppress("SpreadOperator")
    pkg.apply {
        repo = bintray.repo ?: "maven"
        bintray.userOrg?.let { userOrg = it }
        name = pom.artifactId
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
            name = pom.version
            desc = pom.description
            released = currentZonedDateTime()
            vcsTag = pom.version
        }
        setLabels(*labelList)
    }
}

private fun currentZonedDateTime(): String =
    ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ"))
