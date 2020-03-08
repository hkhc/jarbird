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
    fun readPom(path: String): Pom {
        val file = File(path)
        return if (file.exists()) {
            val yaml = Yaml(Constructor(Pom::class.java))
            return yaml.load(file.readText())
        } else {
            Pom()
        }
    }

    /**
     * resolve POM spec via a series of possible location and accumulate the details
     */
    fun resolvePom(project: Project): Pom {
        val pom = Pom()

        val pomFilename = "pom.yaml"

        val gradleUserHomePath = System.getenv("GRADLE_USER_HOME") ?: "~/.gradle"
        val homePomFile = "$gradleUserHomePath/pom.yaml"

        System.getProperty("pomFile")?.let {
            readPom(it).overlayTo(pom)
        }
        readPom("$homePomFile/$pomFilename").overlayTo(pom)
        readPom("${project.rootDir}/$pomFilename").overlayTo(pom)
        readPom("${project.buildDir}/$pomFilename").overlayTo(pom)

        pom.syncWith(project)

        project.logger.debug("Preceived POM")
        project.logger.debug(pom.toString())

//        val yaml = Yaml.default.stringify(Pom.serializer(), pom)
//        System.out.println("-----POM Start-----")
//        System.out.println(yaml)
//        System.out.println("-----POM End-----")

        return pom
    }

    fun validatePom() {

//        with(pom) {
//
//        }

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

    val labelList = bintrayLabels?.split(',')?.toTypedArray() ?: arrayOf()

    val pom = this
    pkg.apply {
        repo = "maven"
        name = pom.name
        desc = pom.description
        setLicenses(*pom.licenses.map { it.name }.toTypedArray())
        websiteUrl = pom.web.url ?: ""
        vcsUrl = pom.scm.url ?: ""
        if (pom.scm.repoType == "github.com") {
            githubRepo = pom.scm.repoName ?: ""
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
