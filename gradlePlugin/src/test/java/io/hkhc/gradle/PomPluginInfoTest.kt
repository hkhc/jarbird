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

import io.hkhc.gradle.pom.PluginInfo
import io.hkhc.utils.test.`Field perform overlay properly`
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class PomPluginInfoTest : StringSpec({

    "PluginInfo shall be a data class so that we may assume 'equals' logic is provided" {
        PluginInfo::class.isData.shouldBeTrue()
    }

    // Have one line per property in the class
    "PluginInfo shall overlay properly" {

        `Field perform overlay properly`(::PluginInfo, PluginInfo::id, "value")
        `Field perform overlay properly`(::PluginInfo, PluginInfo::displayName, "value")
        `Field perform overlay properly`(::PluginInfo, PluginInfo::description, "value")
        `Field perform overlay properly`(::PluginInfo, PluginInfo::implementationClass, "value")

        val p1 = PluginInfo(id = "1", tags = mutableListOf("tag1", "tag2"))
        val p2 = PluginInfo(id = "1", tags = mutableListOf("tag1", "tag3"))

        p1.overlayTo(p2)

        p2.id shouldBe "1"
        p2.tags shouldHaveSize 3
        p2.tags shouldBe mutableListOf("tag1", "tag3", "tag2")
    }

    "Merging list of tags and obtain new item" {

        // GIVEN list of two tags with different names
        val list1 = mutableListOf("tag1", "tag2")

        // AND a different list of licenses with different names
        val list2 = listOf("tag3", "tag2")

        // WHEN merging two lists together
        val list3 = PluginInfo.overlayToTags(list2, list1)

        // THEN the list becomes ...
        with(list3) {
            size shouldBe 3
            this shouldBe listOf("tag1", "tag2", "tag3")
        }
    }
})
