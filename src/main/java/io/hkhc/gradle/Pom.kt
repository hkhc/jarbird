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

import org.gradle.api.Project
import java.util.*

data class License(
    var name: String? = null,
    var url: String? = null,
    var dist: String? = null,
    var comments: String? = null
) {
    fun overlayTo(other: License) {
        name?.let { other.name = name }
        url?.let { other.url = url }
        dist?.let { other.dist = dist }
        comments?.let { other.comments = comments }
    }
}

// TODO missed Properties, Roles
// see https://docs.gradle.org/current/javadoc/org/gradle/api/publish/maven/MavenPomDeveloper.html
data class People(
    var id: String? = null,
    var name: String? = null,
    var email: String? = null,
    var organization: String? = null,
    var organizationUrl: String? = null,
    var timeZone: String? = null,
    var url: String? = null
) {
    fun overlayTo(other: People) {
        id?.let { other.id = id }
        name?.let { other.name = name }
        email?.let { other.email = email }
        organization?.let { other.organization = organization }
        organizationUrl?.let { other.organizationUrl = organizationUrl }
        timeZone?.let { other.timeZone = timeZone }
        url?.let { other.url = url }
    }
}

data class Organization(
    var name: String? = null,
    var url: String? = null
) {
    fun overlayTo(other: Organization) {
        name?.let { other.name = name }
        url?.let { other.url = url }
    }
}

data class Web(
    var url: String? = null,
    var description: String? = null
) {
    fun overlayTo(other: Web) {
        url?.let { other.url = url }
        description?.let { other.description = description }
    }
}

data class Scm(
    var url: String? = null,
    var connection: String? = null,
    var developerConnection: String? = null,
    var repoType: String? = null, // "github.com" / "gitlab.com" / "bitbucket.org"
    var repoName: String? = null, // "user/repo": preferbly give this only and the system try to fill others in Scm
    var issueType: String? = null, // may be same as repoType or others
    var issueUrl: String? = null,
    var tag: String? = null
) {
    @Suppress("DuplicatedCode")
    fun overlayTo(other: Scm) {
        url?.let { other.url = url }
        connection?.let { other.connection = connection }
        developerConnection?.let { other.developerConnection = developerConnection }
        repoType?.let { other.repoType = repoType }
        repoName?.let { other.repoName = repoName }
        issueType?.let { other.issueType = issueType }
        issueUrl?.let { other.issueUrl = issueUrl }
        tag?.let { other.tag = tag }
    }
}

// See https://maven.apache.org/pom.html for POM definitions

data class Pom(
    var group: String? = null,
    var name: String? = null,
    var version: String? = null,
    var inceptionYear: Int = -1,
    var packaging: String? = null,
    var url: String? = null,
    var description: String? = null,
    // Thw following 3 fields are use for YAML to setting it in serializable way.
    var licenses: MutableList<License> = mutableListOf(),
    var developers: MutableList<People> = mutableListOf(),
    var contributors: MutableList<People> = mutableListOf(),
    var organization: Organization = Organization(),
    var web: Web = Web(),
    var scm: Scm = Scm(),
    var bintrayLabels: String? = null
) {

    companion object {
        fun overlayToLicenses(me: List<License>, other: MutableList<License>): MutableList<License> {
            me.forEach { meItem ->
                val found = false
                other.find { otherItem -> meItem.name == otherItem.name }?.apply {
                    meItem.overlayTo(this)
                }
                if (!found) other.add(meItem)
            }
            return other
        }
        fun overlayToPeople(me: List<People>, other: MutableList<People>): MutableList<People> {
            me.forEach { meItem ->
                val found = false
                other.find { otherItem -> meItem.name == otherItem.name }?.apply {
                    meItem.overlayTo(this)
                }
                if (!found) other.add(meItem)
            }
            return other
        }
    }

    @Suppress("DuplicatedCode")
    fun overlayTo(other: Pom) {

        group?.let { other.group = group }
        name?.let { other.name = name }
        version?.let { other.version = version }
        if (inceptionYear != -1) { other.inceptionYear = inceptionYear }
        packaging?.let { other.packaging = packaging }
        url?.let { other.url = url }
        description?.let { other.description = description }

        overlayToLicenses(licenses, other.licenses)
        overlayToPeople(developers, other.developers)
        overlayToPeople(contributors, other.contributors)

        organization.overlayTo(other.organization)
        web.overlayTo(other.web)
        scm.overlayTo(other.scm)

        bintrayLabels?.let { other.bintrayLabels = bintrayLabels }
    }

    private fun lookupLicenseLink(licenses: List<License>) {
        for (lic in licenses) {
            lic.url = lic.url ?: LICENSE_MAP[lic.name!!]
        }
    }

    @Suppress("DuplicatedCode")
    private fun expandScmGit(scm: Scm) {
        if (scm.repoType != null && scm.repoName != null) {
            with(scm) {
                url = url ?: "https://$repoType/$repoName"
                connection = connection ?: "scm:git@$repoType:$repoName.git"
                developerConnection = developerConnection ?: "scm:git@@repoType:$repoName.git"
                tag = tag ?: scm.tag
                issueType = issueType ?: repoType
                issueUrl = issueUrl ?: "https://$repoType/$repoName/issues"
            }
        }
    }

    fun syncWith(project: Project) {

        group?.let { project.group = it }
        group = group ?: project.group.toString()

        // we are not going to change the project name here.
        name = name ?: project.name

        version?.let { project.version = it }
        version = version ?: project.version.toString()

        inceptionYear = if (inceptionYear != -1) inceptionYear else GregorianCalendar.getInstance().get(Calendar.YEAR)
        packaging = packaging ?: "jar"
        description = description ?: project.description

        lookupLicenseLink(licenses)

        // TODO handle SVN/HG/etc
        expandScmGit(scm)

        web.apply {
            url = url ?: scm.url
            description = description ?: this@Pom.description
        }

        url = url ?: scm.url
    }
}
