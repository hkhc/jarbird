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

import LICENSE_MAP
import isSnapshot
import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginConvention
import java.util.Calendar
import java.util.GregorianCalendar

/**
 * Overlayable interface provide a common protocol for the class that can combine together
 * by providing "overlayTo" method.
 *
 * The method that implements the interface shall merge each field of the class such that
 * a new instance is created that contain values of object2 if field in object1 is null, and values of
 * object1 if field in object2 is null
 * - the receiver's value is set to the param object if it is null in param object*
 *
 * e.g.
 * object1 = { field1: "hello" }
 * object2 = { field1: "hi", field2: "world" }
 * then object1.overlayTo(object2) return { field1: "hello", field2: "world" }
 * and both object1 and object2 are remain unchanged
 *
 * @return the same instance of other
 *
 */
interface Overlayable {
    fun overlayTo(other: Overlayable): Overlayable
}

data class License(
    var name: String? = null,
    var url: String? = null,
    var dist: String? = null,
    var comments: String? = null
) : Overlayable {
    override fun overlayTo(other: Overlayable): Overlayable {
        if (other is License) {
            return License(
                name ?: other.name,
                url ?: other.url,
                dist ?: other.dist,
                comments ?: other.comments
            )
//                name?.let { other.name = it }
//                    url?.let { other.url = it }
//                    dist?.let { other.dist = it }
//                    comments?.let { other.comments = it }
        }
        return other
    }

    fun fillLicenseUrl() {
        name?.let {
            url = url ?: LICENSE_MAP[it]
        }
    }

    companion object {
        fun match(a: License, b: License) = a.name == b.name
    }
}

// TODO missed Properties, Roles
// see https://docs.gradle.org/current/javadoc/org/gradle/api/publish/maven/MavenPomDeveloper.html
data class Person(
    var id: String? = null,
    var name: String? = null,
    var email: String? = null,
    var organization: String? = null,
    var organizationUrl: String? = null,
    var timeZone: String? = null,
    var url: String? = null
) : Overlayable {
    override fun overlayTo(other: Overlayable): Overlayable {
        if (other is Person) {
            id?.let { other.id = it }
            name?.let { other.name = it }
            email?.let { other.email = it }
            organization?.let { other.organization = it }
            organizationUrl?.let { other.organizationUrl = it }
            timeZone?.let { other.timeZone = it }
            url?.let { other.url = url }
        }
        return other
    }
    companion object {
        fun match(a: Person, b: Person) = a.name == b.name
    }
}

data class Organization(
    var name: String? = null,
    var url: String? = null
) : Overlayable {
    override fun overlayTo(other: Overlayable): Overlayable {
        if (other is Organization) {
            name?.let { other.name = it }
            url?.let { other.url = it }
        }
        return other
    }
}

data class Web(
    var url: String? = null,
    var description: String? = null
) : Overlayable {
    override fun overlayTo(other: Overlayable): Overlayable {
        if (other is Web) {
            url?.let { other.url = it }
            description?.let { other.description = it }
        }
        return other
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
    override fun overlayTo(other: Overlayable): Overlayable {
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
        return other
    }
}

data class Bintray(
    var labels: String? = null,
    var repo: String? = null,
    var userOrg: String? = null
) : Overlayable {

    override fun overlayTo(other: Overlayable): Overlayable {
        if (other is Bintray) {
            labels?.let { other.labels = it }
            repo?.let { other.repo = it }
            userOrg?.let { other.userOrg = it }
        }
        return other
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

    override fun overlayTo(other: Overlayable): Overlayable {
        if (other is PluginInfo) {
            id?.let { other.id = it }
            displayName?.let { other.displayName = it }
            description?.let { other.description = it }
            implementationClass?.let { other.implementationClass = it }
            overlayToTags(tags, other.tags)
        }
        return other
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
    var developers: MutableList<Person> = mutableListOf(),
    var contributors: MutableList<Person> = mutableListOf(),
    var organization: Organization = Organization(),
    var web: Web = Web(),
    var scm: Scm = Scm(),

    /* "variant" is not part of POM, but an ID to facilitates multiple POM coexist in the same pom.yaml file */
    /* variant is fixed and not going to be overlaid */
    var variant: String = DEFAULT_VARIANT,

    /* "bintary" is not part of POM, but additional information needs to deploy to bintray repo */
    /* TODO it may be better to specify repo details in gradle.properties? */
    var bintray: Bintray = Bintray(),

    /* additional details for plugin deployment */
    var plugin: PluginInfo? = null

) : Overlayable {

    companion object {

        const val DEFAULT_VARIANT = "---DEFAULT-POM---"

        @Suppress("UNCHECKED_CAST")
        fun <T : Overlayable> overlayToList(me: List<T>, other: List<T>, matcher: (T, T) -> Boolean): MutableList<T> {
            return mutableListOf<T>().also { newList ->
                // add those
                newList.addAll(
                    other.map { otherItem ->
                        val matched = me.find { matcher.invoke(otherItem, it) }
                        if (matched == null) {
                            otherItem
                        } else {
                            matched.overlayTo(otherItem) as T
                        }
                    }
                )
                newList.addAll(
                    me.filter {
                        other.none { otherItem -> matcher.invoke(otherItem, it) }
                    }
                )
            }
        }

        var dataHandler: () -> Calendar = { GregorianCalendar.getInstance() }

        fun setDateHandler(block: () -> Calendar) {
            dataHandler = block
        }
    }

    @Suppress("DuplicatedCode")
    override fun overlayTo(other: Overlayable): Overlayable {
        if (other is Pom) {
            group?.let { other.group = it }
            artifactId?.let { other.artifactId = it }
            version?.let { other.version = it }
            if (inceptionYear != -1) { other.inceptionYear = inceptionYear }
            packaging?.let { other.packaging = it }
            name?.let { other.name = it }
            url?.let { other.url = it }
            description?.let { other.description = it }

            other.licenses = overlayToList(licenses, other.licenses, License::match)
            other.developers = overlayToList(developers, other.developers, Person::match)
            other.contributors = overlayToList(contributors, other.contributors, Person::match)

            organization.overlayTo(other.organization)
            web.overlayTo(other.web)
            scm.overlayTo(other.scm)

            bintray.overlayTo(other.bintray)

            plugin?.let { thisPlugin ->
                other.plugin = other.plugin?.also { it -> thisPlugin.overlayTo(it) } ?: thisPlugin
            }
        }
        return other
    }

    internal fun lookupLicenseLink(licenses: List<License>) {
        licenses.forEach { it.fillLicenseUrl() }
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

    fun isSnapshot() = version.isSnapshot()

    fun isGradlePlugin() = plugin != null

    /**
     * See https://central.sonatype.org/pages/requirements.html#sufficient-metadata
     * for the detail accounts of POM metadata needs to publish to Maven Central.
     * That should be more than enough for Bintray
     *
     * project.group, archiveBaseName of project convention may be updated
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
        val convention = project.convention.plugins["base"] as BasePluginConvention?
        convention?.let { c ->
            artifactId?.let { c.archivesBaseName = it }
        }

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
