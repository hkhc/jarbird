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

import kotlinx.serialization.Serializable
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPom
import java.util.*

@Serializable
data class License(
    var name: String?,
    var url: String?,
    var dist: String?
) {
    fun merge(other: License) {
        name = name ?: other.name
        url = url ?: other.url
        dist = dist ?: other.dist
    }
}

@Serializable
data class People (
    var id: String?,
    var name: String?,
    var email: String?
) {
    fun merge(other: People) {
        id = id ?: other.id
        name = name ?: other.name
        email = email ?: other.email
    }
}

@Serializable
data class Web(
    var url: String? = null,
    var description: String? = null
) {
    fun merge(other: Web) {
        url = url ?: other.url
        description = description ?: other.description
    }
}

@Serializable
data class Scm(
    var url: String? = null,
    var connection: String? = null,
    var developerConnection: String? = null,
    var githubRepo: String? = null, // "user/repo": preferbly give this only and the system try to fill others in Scm
    var issueUrl: String? = null
) {
    fun merge(other: Scm) {
        url = url ?: other.url
        connection = connection ?: other.connection
        developerConnection = developerConnection ?: other.developerConnection
        githubRepo = githubRepo ?: other.githubRepo
        issueUrl = issueUrl ?: other.issueUrl
    }
}

// See https://maven.apache.org/pom.html for POM definitions

@Serializable
data class Pom(
    var group: String? = null,
    var name: String? = null,
    var version: String? = null,
    var year: Int = -1,
    var packaging: String? = null,
    var url: String? = null,
    var description: String? = null,
    var licenses: List<License> = mutableListOf(),
    var developers: List<People> = mutableListOf(),
    var contributors: List<People> = mutableListOf(),
    var web: Web = Web(),
    var scm: Scm = Scm()
) {

    var license: License?
        get() = if (licenses.isEmpty()) null else licenses[0]
        set(value) { licenses = if (value == null) mutableListOf() else mutableListOf(value) }

    var developer: People?
        get() = if (developers.isEmpty()) null else developers[0]
        set(value) { developers = if (value == null) mutableListOf() else mutableListOf(value) }

    var contributor: People?
        get() = if (contributors.isEmpty()) null else contributors[0]
        set(value) { contributors = if (value == null) mutableListOf() else mutableListOf(value) }

    private fun List<License>.mergeLicenses(others: List<License>): List<License> {
        others.forEach { otherLicense ->
            find { it.name == otherLicense.name }?.merge(otherLicense)
        }
        return this
    }

    private fun List<People>.mergePeople(others: List<People>): List<People> {
        others.forEach { otherPeople ->
            find { it.name == otherPeople.name }?.merge(otherPeople)
        }
        return this
    }

    fun merge(other: Pom) {
        group = group ?: other.group
        name = name ?: other.name
        version = version ?: other.version
        year = if (year != -1) year else other.year
        packaging = packaging ?: other.packaging
        url = url ?: other.url
        description = description ?: other.description

        licenses.mergeLicenses(other.licenses)
        developers.mergePeople(other.developers)
        contributors.mergePeople(other.contributors)

        web.merge(other.web)
        scm.merge(other.scm)
    }

    fun getFrom(project: Project) {

        group = group ?: project.group.toString()
        name = name ?: project.name
        version = name ?: project.version.toString()
        year = if (year != -1) year else GregorianCalendar.getInstance().get(Calendar.YEAR)
        packaging = packaging ?: "jar"
        description = description ?: project.description

        web.apply {
            url = url ?: scm.githubRepo?.let { "https://github.com/$it" }
            description = description ?: this@Pom.description
        }

        scm.apply {
            url = url ?: githubRepo?.let { "https://github.com/$it" }
            connection = connection ?: githubRepo?.let { "scm:git@github.com:$it.git" }
            developerConnection = developerConnection ?: githubRepo?.let { "scm:git@github.com:$it.git" }
            issueUrl = issueUrl ?: githubRepo?.let { "https://github.com/$githubRepo/issues" }
        }

        url = url ?: scm.url
    }

    fun fillTo(mavenPom: MavenPom) {

        mavenPom.name.set(name)
        mavenPom.description.set(description)
        mavenPom.url.set(url)
        mavenPom.licenses {
            for (lic in licenses) {
                license {
                    name.set(lic.name)
                    url.set(lic.url)
                }
            }
        }
        mavenPom.developers {
            for (dev in developers) {
                developer {
                    id.set(dev.id)
                    name.set(dev.name)
                    email.set(dev.email)
                }
            }
        }
        mavenPom.contributors {
            for (cont in contributors) {
                contributor {
                    name.set(cont.name)
                    email.set(cont.email)
                }
            }
        }
        mavenPom.scm {
            connection.set(scm.connection)
            developerConnection.set(scm.developerConnection)
            url.set(scm.url)
        }
    }
}
