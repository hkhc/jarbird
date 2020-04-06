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

import io.hkhc.gradle.pom.License
import io.hkhc.gradle.pom.Organization
import io.hkhc.gradle.pom.People
import io.hkhc.gradle.pom.Pom
import io.hkhc.gradle.pom.Scm
import io.hkhc.gradle.pom.Web
import io.hkhc.utils.test.`Int field is overlay properly`
import io.hkhc.utils.test.`Field perform overlay properly`
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

class PomTest : StringSpec({

    "Pom shall be a data class so that we may assume 'equals' logic is provided" {
        Pom::class.isData.shouldBeTrue()
    }

    // Have one line per property in the class
    "Pom shall overlay properly" {

        `Field perform overlay properly`(::Pom, Pom::group, "value")
        `Field perform overlay properly`(::Pom, Pom::name, "value")
        `Field perform overlay properly`(::Pom, Pom::version, "value")
        `Int field is overlay properly`(::Pom, Pom::inceptionYear)
        `Field perform overlay properly`(::Pom, Pom::packaging, "value")
        `Field perform overlay properly`(::Pom, Pom::url, "value")
        `Field perform overlay properly`(::Pom, Pom::description, "value")
        `Field perform overlay properly`(::Pom, Pom::bintrayLabels, "value")

        `Field perform overlay properly`(::Pom, Pom::organization, Organization("name", "url_orgn"))
        `Field perform overlay properly`(::Pom, Pom::web, Web("url", "description"))
        `Field perform overlay properly`(::Pom, Pom::scm, Scm(url = "url", connection = "connection"))
        `Field perform overlay properly`(::Pom, Pom::plugin, null)
    }

    "Pom shall merge licenses properly" {

        // GIVEN
        val p1 = Pom(licenses = mutableListOf(
            License(name = "A"),
            License(name = "B")
        ))
        val p2 = Pom(licenses = mutableListOf(
            License(name = "C")
        ))

        // WHEN
        p1.overlayTo(p2)

        // THEN
        p2.licenses shouldBe mutableListOf(
            License(name = "C"),
            License(name = "A"),
            License(name = "B")
        )
    }

    "Pom shall merge developers properly" {

        // GIVEN
        val p1 = Pom(developers = mutableListOf(
            People(name = "A"),
            People(name = "B")
        ))
        val p2 = Pom(developers = mutableListOf(
            People(name = "C")
        ))

        // WHEN
        p1.overlayTo(p2)

        // THEN
        p2.developers shouldBe mutableListOf(
            People(name = "C"),
            People(name = "A"),
            People(name = "B")
        )
    }

    "Pom shall merge contributors properly" {

        // GIVEN
        val p1 = Pom(contributors = mutableListOf(
            People(name = "A"),
            People(name = "B")
        ))
        val p2 = Pom(contributors = mutableListOf(
            People(name = "C")
        ))

        // WHEN
        p1.overlayTo(p2)

        // THEN
        p2.contributors shouldBe mutableListOf(
            People(name = "C"),
            People(name = "A"),
            People(name = "B")
        )
    }

})
