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

import io.hkhc.utils.test.`Array Fields merged properly when overlaying`
import io.hkhc.utils.test.`Field perform overlay properly`
import io.hkhc.utils.test.`Fields overlay properly`
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.plugins.Convention
import org.gradle.api.plugins.internal.DefaultBasePluginConvention
import java.util.Calendar
import java.util.GregorianCalendar

// @ExtendWith(MockKExtension::class)
class PomTest : StringSpec({

    lateinit var project: DefaultProject

    beforeTest {
        project = mockk(relaxed = true)
        val convention: Convention = mockk(relaxed = true)
        every { convention.plugins } returns mutableMapOf<String, Any>("base" to DefaultBasePluginConvention(project))
        every { project.convention } returns convention
    }

    "Pom shall be a data class so that we may assume 'equals' logic is provided" {
        Pom::class.isData.shouldBeTrue()
    }

    // Have one line per property in the class
    "Pom shall overlay properly" {

        val nonStringFields = arrayOf(
            `Field perform overlay properly`(::Pom, Pom::bintray, Bintray("names", "repo", "org")),
            `Field perform overlay properly`(::Pom, Pom::organization, Organization("name", "url_orgn")),
            `Field perform overlay properly`(::Pom, Pom::web, Web("url", "description")),
            `Field perform overlay properly`(::Pom, Pom::scm, Scm(url = "url", connection = "connection")),
            `Field perform overlay properly`(::Pom, Pom::plugin, PluginInfo(id = "123", displayName = "name")),
            `Array Fields merged properly when overlaying`(
                ::Pom,
                Pom::licenses,
                listOf(License(name = "A"), License(name = "B")),
                listOf(License(name = "C"))
            ),
            `Array Fields merged properly when overlaying`(
                ::Pom,
                Pom::developers,
                listOf(Person(name = "A"), Person(name = "B")),
                listOf(Person(name = "C"))
            ),
            `Array Fields merged properly when overlaying`(
                ::Pom,
                Pom::contributors,
                listOf(Person(name = "A"), Person(name = "B")),
                listOf(Person(name = "C"))
            )
        )

        `Fields overlay properly`(Pom::class, ::Pom, nonStringFields)
    }

    "Pom shall determine if it is snapshot by plugin info version" {

        // GIVEN
        val p1 = Pom(version = "1.0")
        // WHEN
        var isSnapshot = p1.isSnapshot()
        // THEN
        isSnapshot.shouldBeFalse()

        // GIVEN capital letter "snapshot"
        p1.version = "1.0-SNAPSHOT"
        // WHEN
        isSnapshot = p1.isSnapshot()
        // THEN
        isSnapshot.shouldBeTrue()

        // GIVEN small letter "snapshot"
        p1.version = "1.0-snapshot"
        // WHEN
        isSnapshot = p1.isSnapshot()
        // THEN
        isSnapshot.shouldBeFalse()
    }

    "Pom shall expand license details based on license name" {

        // GIVEN non-existence license name
        var p1 = Pom(licenses = mutableListOf(License(name = "XXX")))
        // WHEN
        p1.lookupLicenseLink(p1.licenses)
        // THEN
        p1.licenses[0].url.shouldBeNull()

        // GIVEN a known license name
        p1 = Pom(licenses = mutableListOf(License(name = "Apache-2.0")))
        // WHEN
        p1.lookupLicenseLink(p1.licenses)
        // THEN
        p1.licenses[0].url shouldBe "http://www.apache.org/licenses/LICENSE-2.0.txt"
    }

    "Pom shall resolve git details by the repo ID" {

        // GIVEN non-existence license name
        var p1 = Pom(scm = Scm(repoType = "github.com", repoName = "hkhc/abc"))
        // WHEN
        p1.expandScmGit(p1.scm)
        // THEN
        with(p1.scm) {
            url shouldBe "https://github.com/hkhc/abc"
            connection shouldBe "scm:git@github.com:hkhc/abc"
            developerConnection shouldBe "scm:git@github.com:hkhc/abc.git"
            issueType shouldBe "github.com"
            issueUrl shouldBe "https://github.com/hkhc/abc/issues"
        }
    }

    "Pom shall be sync with project object" {

        // GIVEN
        Pom.setDateHandler { GregorianCalendar.getInstance().apply { set(Calendar.YEAR, 1999) } }
        every { project.group } returns "io.hkhc"
        every { project.name } returns "mylib"
        every { project.version } returns "1.0"
        every { project.description } returns "desc"

        val pom = Pom()
        pom.licenses.add(License("Apache-2.0"))
        pom.scm.repoType = "github.com"
        pom.scm.repoName = "hkhc/mylib"

        // WHEN
        pom.syncWith(project)

        // THEN
        pom.group shouldBe "io.hkhc"
        pom.name shouldBe "mylib"
        pom.version shouldBe "1.0"
        pom.description shouldBe "desc"

        pom.inceptionYear shouldBe 1999
        pom.packaging shouldBe "jar"
        pom.licenses[0].url shouldBe "http://www.apache.org/licenses/LICENSE-2.0.txt"

        pom.scm.url shouldBe "https://github.com/hkhc/mylib"
        pom.url shouldBe "https://github.com/hkhc/mylib"
        pom.web.url shouldBe "https://github.com/hkhc/mylib"
    }

    "Pom shall be sync with project object and update project object" {

        // GIVEN
        Pom.setDateHandler { GregorianCalendar.getInstance().apply { set(Calendar.YEAR, 1999) } }
        every { project.name } returns "mylib"
        val pom = Pom(group = "io.hkhc", version = "1.0")

        // WHEN
        pom.syncWith(project)

        // THEN

        verify {
            project.group = "io.hkhc"
            project.name
        }
    }
})
