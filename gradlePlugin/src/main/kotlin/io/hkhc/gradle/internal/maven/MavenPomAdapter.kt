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

package io.hkhc.gradle.internal.maven

import io.hkhc.gradle.pom.License
import io.hkhc.gradle.pom.Organization
import io.hkhc.gradle.pom.Person
import io.hkhc.gradle.pom.Pom
import io.hkhc.gradle.pom.Scm
import org.gradle.api.publish.maven.MavenPom

internal class MavenPomAdapter {

    private fun MavenPom.fillLicenses(pomLicenses: List<License>) {
        licenses {
            pomLicenses.forEach { lic ->
                license {
                    name.set(lic.name)
                    url.set(lic.url)
                    comments.set(lic.comments)
                }
            }
        }
    }

    private fun MavenPom.fillDevelopers(pomPeople: List<Person>) {
        developers {
            pomPeople.forEach { dev ->
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
    }

    private fun MavenPom.fillContributors(pomPeople: List<Person>) {
        contributors {
            pomPeople.forEach { dev ->
                contributor {
                    name.set(dev.name)
                    email.set(dev.email)
                    organization.set(dev.organization)
                    organizationUrl.set(dev.organizationUrl)
                    timezone.set(dev.timeZone)
                    url.set(dev.url)
                }
            }
        }
    }

    private fun MavenPom.fillOrganization(pomOrgn: Organization) {
        organization {
            pomOrgn.also { ogn ->
                name.set(ogn.name)
                url.set(ogn.url)
            }
        }
    }

    private fun MavenPom.fillScm(pomScm: Scm) {
        scm {
            pomScm.also { scm ->
                connection.set(scm.connection)
                developerConnection.set(scm.developerConnection)
                url.set(scm.url)
                tag.set(scm.tag)
            }
        }
    }

    private fun MavenPom.fillIssueManagement(pomScm: Scm) {
        issueManagement {
            system.set(pomScm.issueType)
            url.set(pomScm.issueUrl)
        }
    }

    fun fill(mavenPom: MavenPom, pom: Pom) {
        with(mavenPom) {
            name.set(pom.name)
            description.set(pom.description)
            inceptionYear.set(pom.inceptionYear.toString())
            url.set(pom.url)
            fillLicenses(pom.licenses)
            fillDevelopers(pom.developers)
            fillContributors(pom.contributors)
            fillOrganization(pom.organization)
            fillScm(pom.scm)
            if (pom.scm.issueType != null && pom.scm.issueUrl != null) {
                fillIssueManagement(pom.scm)
            }
        }
    }
}
