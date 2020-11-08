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

import io.hkhc.utils.test.`Fields overlay properly`
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class
PomLicenseTest : StringSpec({

    "License shall be a data class so that we may assume 'equals' logic is provided" {
        License::class.isData.shouldBeTrue()
    }

    // Have one line per property in the class
    "License shall overlay properly" {
        `Fields overlay properly`(License::class, ::License)
    }

    "Merging list of licenses and obtain new item" {

        // GIVEN list of two licenses with different names
        val list1 = mutableListOf(
            License(name = "lic_name_1", url = "lic_url_1", dist = "lic_dist_1"),
            License(name = "lic_name_2", url = "lic_url_2", dist = "lic_dist_2")
        )

        // AND a different list of licenses with different names
        val list2 = listOf(
            License(name = "lic_name_3", url = "lic_url_3", dist = "lic_dist_3")
        )

        // WHEN merging two lists together
        val list3 = Pom.overlayToList(list2, list1, License::match)

        // THEN the list becomes ...
        with(list3) {
            size shouldBe 3
            this shouldBe listOf(
                License("lic_name_1", "lic_url_1", "lic_dist_1"),
                License("lic_name_2", "lic_url_2", "lic_dist_2"),
                License("lic_name_3", "lic_url_3", "lic_dist_3")
            )
        }
    }

    "Merging list of licenses and update existing item" {

        // GIVEN list of two licenses with different names
        val list1 = mutableListOf(
            License("lic_name_1", "lic_url_1", "lic_dist_1"),
            License("lic_name_2")
        )

        // AND a different list of licenses with some items' name in first list
        val list2 = listOf(
            License("lic_name_2", "lic_url_2", "lic_dist_2"),
            License("lic_name_3", "lic_url_3", "lic_dist_3")
        )

        // WHEN merging two lists together
        val list3 = Pom.overlayToList(list2, list1, License::match)

        // THEN the items in the second list overlay the items in the first list
        with(list3) {
            size shouldBe 3
            this shouldBe listOf(
                License("lic_name_1", "lic_url_1", "lic_dist_1"),
                License("lic_name_2", "lic_url_2", "lic_dist_2"),
                License("lic_name_3", "lic_url_3", "lic_dist_3")
            )
        }
    }

    "License details based on license name" {

        // GIVEN non-existence license name
        val l1 = License(name = "XXX")
        l1.fillLicenseUrl()
        // THEN
        l1.url.shouldBeNull()

        // GIVEN a known license name
        val l2 = License(name = "Apache-2.0")
        l2.fillLicenseUrl()
        // THEN
        l2.url shouldBe "http://www.apache.org/licenses/LICENSE-2.0.txt"
    }
})
