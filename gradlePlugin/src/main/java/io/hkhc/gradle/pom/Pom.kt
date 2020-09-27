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

import io.hkhc.gradle.LICENSE_MAP
import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginConvention
import java.util.Calendar
import java.util.GregorianCalendar

/**
 * Overlayable interface provide a common protocol for the class that can combine together
 * by providing "overlayTo" method.
 *
 * The method that implements the interface shall merge each field of the class such that
 * - The field in receiver shall overwrite the ine in parameter, if former is not null
 *
 * For collection fields, the item in former field and not in later field shall be appended to the later collection
 *
 *
 */
interface Overlayable {
    fun overlayTo(other: Overlayable)
}

data class License(
    var name: String? = null,
    var url: String? = null,
    var dist: String? = null,
    var comments: String? = null
) : Overlayable {
    override fun overlayTo(other: Overlayable) {
        if (other is License) {
            name?.let { other.name = it }
            url?.let { other.url = it }
            dist?.let { other.dist = it }
            comments?.let { other.comments = it }
        }
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
) : Overlayable {
    override fun overlayTo(other: Overlayable) {
        if (other is People) {
            id?.let { other.id = it }
            name?.let { other.name = it }
            email?.let { other.email = it }
            organization?.let { other.organization = it }
            organizationUrl?.let { other.organizationUrl = it }
            timeZone?.let { other.timeZone = it }
            url?.let { other.url = url }
        }
    }
}

data class Organization(
    var name: String? = null,
    var url: String? = null
) : Overlayable {
    override fun overlayTo(other: Overlayable) {
        if (other is Organization) {
            name?.let { other.name = it }
            url?.let { other.url = it }
        }
    }
}

data class Web(
    var url: String? = null,
    var description: String? = null
) : Overlayable {
    override fun overlayTo(other: Overlayable) {
        if (other is Web) {
            url?.let { other.url = it }
            description?.let { other.description = it }
        }
    }
}

data class Scm(
    var url: String? = null,
    var connection: String? = null,
    var developerConnection: String? = null,
    var repoType: String? = null, // "github.com" / "gitlab.com" / "bitbucket.org"
    var repoName: String? = null, // "user/repo": preferably give this only and the system try to fill others in Scm
    var issueType: String? = null, // may be same as repoType or others
    var issueUrl: String? = null,
    var tag: String? = null,
    var githubReleaseNoteFile: String? = null
) : Overlayable {
    @Suppress("DuplicatedCode")
    override fun overlayTo(other: Overlayable) {
        if (other is Scm) {
            url?.let { other.url = it }
            connection?.let { other.connection = it }
            developerConnection?.let { other.developerConnection = it }
            repoType?.let { other.repoType = it }
            repoName?.let { other.repoName = it }
            issueType?.let { other.issueType = it }
            issueUrl?.let { other.issueUrl = it }
            tag?.let { other.tag = it }
            githubReleaseNoteFile?.let { other.githubReleaseNoteFile = it }
        }
    }
}

data class Bintray(
    var labels: String? = null,
    var repo: String? = null,
    var userOrg: String? = null
) : Overlayable {

    override fun overlayTo(other: Overlayable) {
        if (other is Bintray) {
            labels?.let { other.labels = it }
            repo?.let { other.repo = it }
            userOrg?.let { other.userOrg = it }
        }
    }
}

data class PluginInfo(
    var id: String? = null,
    var displayName: String? = null,
    var description: String? = null,
    var implementationClass: String? = null,
    var tags: MutableList<String> = mutableListOf()
) : Overlayable {

    companion object {
        fun overlayToTags(me: List<String>, other: MutableList<String>): MutableList<String> {
            me.forEach { meItem ->
                other.find { otherItem -> meItem == otherItem } ?: other.add(meItem)
            }
            return other
        }
    }

    override fun overlayTo(other: Overlayable) {
        if (other is PluginInfo) {
            id?.let { other.id = it }
            displayName?.let { other.displayName = it }
            description?.let { other.description = it }
            implementationClass?.let { other.implementationClass = it }
            overlayToTags(tags, other.tags)
        }
    }
}

// See https://maven.apache.org/pom.html for POM definitions

data class Pom(
    var group: String? = null,
    var artifactId: String? = null,
    var version: String? = null,
    var inceptionYear: Int = -1,
    var packaging: String? = null,
    var name: String? = null,
    var url: String? = null,
    var description: String? = null,
    // Thw following 3 fields are use for YAML to setting it in serializable way.
    var licenses: MutableList<License> = mutableListOf(),
    var developers: MutableList<People> = mutableListOf(),
    var contributors: MutableList<People> = mutableListOf(),
    var organization: Organization = Organization(),
    var web: Web = Web(),
    var scm: Scm = Scm(),
    var bintray: Bintray = Bintray(),

    var plugin: PluginInfo? = null

) : Overlayable {

    companion object {
        fun overlayToLicenses(me: List<License>, other: MutableList<License>): MutableList<License> {
            me.forEach { meItem ->
                var found = false
                other.find { otherItem -> meItem.name == otherItem.name }?.apply {
                    meItem.overlayTo(this)
                    found = true
                }
                if (!found) other.add(meItem)
            }
            return other
        }
        fun overlayToPeople(me: List<People>, other: MutableList<People>): MutableList<People> {
            me.forEach { meItem ->
                var found = false
                other.find { otherItem -> meItem.name == otherItem.name }?.apply {
                    meItem.overlayTo(this)
                    found = true
                }
                if (!found) other.add(meItem)
            }
            return other
        }

        var dataHandler: () -> Calendar = { GregorianCalendar.getInstance() }

        fun setDateHandler(block: () -> Calendar) {
            dataHandler = block
        }
    }

    @Suppress("DuplicatedCode")
    override fun overlayTo(other: Overlayable) {
        if (other is Pom) {
            group?.let { other.group = it }
            artifactId?.let { other.artifactId = it }
            version?.let { other.version = it }
            if (inceptionYear != -1) { other.inceptionYear = inceptionYear }
            packaging?.let { other.packaging = it }
            name?.let { other.name = it }
            url?.let { other.url = it }
            description?.let { other.description = it }

            overlayToLicenses(licenses, other.licenses)
            overlayToPeople(developers, other.developers)
            overlayToPeople(contributors, other.contributors)

            organization.overlayTo(other.organization)
            web.overlayTo(other.web)
            scm.overlayTo(other.scm)

            bintray.overlayTo(other.bintray)

            plugin?.let { thisPlugin ->
                other.plugin = other.plugin?.also(thisPlugin::overlayTo) ?: thisPlugin
            }
        }
    }

    internal fun lookupLicenseLink(licenses: List<License>) {
        for (lic in licenses) {
            lic.name?.let {
                lic.url = lic.url ?: LICENSE_MAP[it]
            }
        }
    }

    @Suppress("DuplicatedCode")
    internal fun expandScmGit(scm: Scm) {
        if (scm.repoType != null && scm.repoName != null) {
            with(scm) {
                url = url ?: "https://$repoType/$repoName"
                connection = connection ?: "scm:git@$repoType:$repoName"
                developerConnection = developerConnection ?: "scm:git@$repoType:$repoName.git"
                issueType = issueType ?: repoType
                issueUrl = issueUrl ?: "https://$repoType/$repoName/issues"
            }
        }
    }

    fun isSnapshot() = version?.endsWith("-SNAPSHOT") ?: false

    /**
     * See https://central.sonatype.org/pages/requirements.html#sufficient-metadata
     * for the detail accounts of POM metadata needs to publish to Maven Central.
     * That should be more than enough for Bintray
     */
    @Suppress("ComplexMethod")
    fun syncWith(project: Project) {

        // two-way sync with project.group
        // so that the artifact can be build according the setting here.
        group?.let { project.group = it }
        group = group ?: project.group.toString()

        artifactId = artifactId ?: project.name

        // but we are not going to change the project name, because that may distrub the
        // execution of gradle script.
//        name = name ?: "$group:${project.name}"
        name = artifactId

        // TODO need a way to mock convention to make it testable
        val convention = project.convention.plugins["base"] as BasePluginConvention
        artifactId?.let { convention.archivesBaseName = it }

        // two-way sync with project.version
        version?.let { project.version = it }
        version = version ?: project.version.toString()

        // set default inception year if we don't provide one
        inceptionYear = if (inceptionYear != -1) inceptionYear else dataHandler().get(Calendar.YEAR)

        // default packaging is "jar"
        packaging = packaging ?: "jar"

        // we get the project description as artifact description,
        // but it seems useless to change he project description here, so we don't do that.
        description = description ?: project.description

        // resolve license link by the name.
        lookupLicenseLink(licenses)

        licenses.forEach {
            if (it.dist == null) it.dist = "repo"
        }

        // TODO handle SVN/HG/etc
        // resolve scm details by repoType and repoName
        expandScmGit(scm)

        // resolve web details if we don't provide it.
        web.apply {
            url = url ?: scm.url
            description = description ?: this@Pom.description
        }

        url = url ?: scm.url
    }
}
