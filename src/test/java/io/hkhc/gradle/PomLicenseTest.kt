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

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PomLicenseTest : StringSpec( {

    "Empty license can merge from another" {
        val l1 = License("lic_name", "lic_url", "lic_dist")
        val l2 = License()
        l2.merge(l1)

        with(l2) {
            name shouldBe "lic_name"
            url shouldBe "lic_url"
            dist shouldBe "lic_dist"
        }
    }

    "Partially filled license merge to another and get missing value" {
        val l1 = License("lic_name", "lic_url", "lic_dist")
        val l2 = License("lic_name_2", "lic_url_2")
        l2.merge(l1)

        with(l2) {
            name shouldBe "lic_name_2"
            url shouldBe "lic_url_2"
            dist shouldBe "lic_dist"
        }
    }

    "Filled field in license is not updated during merging" {
        val l1 = License("lic_name", "lic_url", "lic_dist")
        val l2 = License("lic_name_2", "lic_url_2")
        l2.merge(l1)

        with(l2) {
            name shouldBe "lic_name_2"
            url shouldBe "lic_url"
            dist shouldBe "lic_dist"
        }
    }

    "Merging list of licenses and obtain new item" {
        val list1 = mutableListOf(
            License("lic_name_1", "lic_url_1", "lic_dist_1"),
            License("lic_name_2", "lic_url_2", "lic_dist_2")
        )

        val list2 = listOf(
            License("lic_name_3", "lic_url_3", "lic_dist_3")
        )

        with(Pom.mergeLicenses(list1, list2)) {
            size shouldBe 3
            this shouldBe listOf(
                License("lic_name_1", "lic_url_1", "lic_dist_1"),
                License("lic_name_2", "lic_url_2", "lic_dist_2"),
                License("lic_name_3", "lic_url_3", "lic_dist_3")
            )
        }
    }

    "Merging list of licenses and update existing item" {
        val list1 = mutableListOf(
            License("lic_name_1", "lic_url_1", "lic_dist_1"),
            License("lic_name_2")
        )

        val list2 = listOf(
            License("lic_name_2", "lic_url_2", "lic_dist_2"),
            License("lic_name_3", "lic_url_3", "lic_dist_3")
        )

        with(Pom.mergeLicenses(list1, list2)) {
            size shouldBe 3
            this shouldBe listOf(
                License("lic_name_1", "lic_url_1", "lic_dist_1"),
                License("lic_name_2", "lic_url_2", "lic_dist_2"),
                License("lic_name_3", "lic_url_3", "lic_dist_3")
            )
        }
    }


})
