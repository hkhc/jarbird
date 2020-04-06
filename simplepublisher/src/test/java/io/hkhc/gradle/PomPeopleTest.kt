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

import io.hkhc.gradle.pom.People
import io.hkhc.gradle.pom.Pom
import io.hkhc.utils.test.`Field perform overlay properly`
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PomPeopleTest : StringSpec({

    // Have one line per property in the class
    "People shall overlay properly" {

        `Field perform overlay properly`(::People, People::id, "value")
        `Field perform overlay properly`(::People, People::name, "value")
        `Field perform overlay properly`(::People, People::email, "value")
        `Field perform overlay properly`(::People, People::organization, "value")
        `Field perform overlay properly`(::People, People::organizationUrl, "value")
        `Field perform overlay properly`(::People, People::timeZone, "value")
        `Field perform overlay properly`(::People, People::url, "value")
    }

    "Merging list of people and obtain new item" {

        // GIVEN list of two licenses with different names
        val list1 = mutableListOf(
            People(id = "people_id_1", name = "people_name_1", email = "people_email_1"),
            People(id = "people_id_2", name = "people_name_2", email = "people_email_2")
        )

        // AND a different list of licenses with different names
        val list2 = listOf(
            People(id = "people_id_3", name = "people_name_3", email = "people_email_3")
        )

        // WHEN merging two lists together
        val list3 = Pom.overlayToPeople(list2, list1)

        // THEN the list becomes ...
        with(list3) {
            size shouldBe 3
            this shouldBe listOf(
                People(id = "people_id_1", name = "people_name_1", email = "people_email_1"),
                People(id = "people_id_2", name = "people_name_2", email = "people_email_2"),
                People(id = "people_id_3", name = "people_name_3", email = "people_email_3")
            )
        }
    }

    "Merging list of people and update existing item" {

        // GIVEN list of two licenses with different names
        val list1 = mutableListOf(
            People(id = "people_id_1", name = "people_name_1", email = "people_email_1"),
            People(id = "people_id_2", name = "people_name_2", email = "people_email_2")
        )

        // AND a different list of licenses with some items' name in first list
        val list2 = listOf(
            People(id = "people_id_2", name = "people_name_2", email = "people_email_2_changed"),
            People(id = "people_id_3", name = "people_name_3", email = "people_email_3")
        )

        // WHEN merging two lists together
        val list3 = Pom.overlayToPeople(list2, list1)

        // THEN the items in the second list overlay the items in the first list
        with(list3) {
            size shouldBe 3
            this shouldBe listOf(
                People(id = "people_id_1", name = "people_name_1", email = "people_email_1"),
                People(id = "people_id_2", name = "people_name_2", email = "people_email_2_changed"),
                People(id = "people_id_3", name = "people_name_3", email = "people_email_3")
            )
        }
    }
})
