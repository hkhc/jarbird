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
import io.hkhc.utils.test.`Fields overlay properly`
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue

class PomPluginInfoTest : StringSpec({

    "PluginInfo shall be a data class so that we may assume 'equals' logic is provided" {
        PluginInfo::class.isData.shouldBeTrue()
    }

    // Have one line per property in the class
    "PluginInfo shall overlay properly" {

        val nonStringFields = arrayOf(
            `Array Fields merged properly when overlaying`(
                { PluginInfo() },
                PluginInfo::tags,
                listOf("tag1", "tag2"),
                listOf("tag3")
            )
        )

        `Fields overlay properly`(PluginInfo::class, { PluginInfo() }, nonStringFields)
    }
})
