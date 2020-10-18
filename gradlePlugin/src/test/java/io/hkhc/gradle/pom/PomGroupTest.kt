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

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.maps.shouldContainExactly

class PomGroupTest : StringSpec({

    "Create a POM group" {
        PomGroup(listOf()).getMap() shouldContainExactly mapOf()
    }

    "Create a POM group with one default Pom" {
        PomGroup(
            listOf(
                Pom(group = "test.group1")
            )
        ).getMap() shouldContainExactly mapOf(
            Pom.DEFAULT_VARIANT to Pom(variant = Pom.DEFAULT_VARIANT, group = "test.group1")
        )
    }

    "Create a POM group with list of Pom" {
        PomGroup(
            listOf(
                Pom(variant = "v1", group = "test.group1"),
                Pom(variant = "v2", group = "test.group2")
            )
        ).getMap() shouldContainExactly mapOf(
            "v1" to Pom(variant = "v1", group = "test.group1"),
            "v2" to Pom(variant = "v2", group = "test.group2")
        )
    }

    "Group members are Overlaid with default Pom" {

        PomGroup(
            listOf(
                Pom(description = "common description"),
                Pom(variant = "v1", group = "test.group1"),
                Pom(variant = "v2", group = "test.group2", description = "group2 description")
            )
        ).getMap() shouldContainExactly mapOf(
            Pom.DEFAULT_VARIANT to Pom(description = "common description"),
            "v1" to Pom(variant = "v1", group = "test.group1", description = "common description"),
            "v2" to Pom(variant = "v2", group = "test.group2", description = "group2 description")
        )
    }

    "POM Group overlay another" {

        val pomGroup = PomGroup(
            listOf(
                Pom(name = "package name")
            )
        ).overlayTo(
            PomGroup(
                listOf(
                    Pom(description = "common description"),
                    Pom(variant = "v1", group = "test.group1"),
                    Pom(variant = "v2", group = "test.group2", description = "group2 description")
                )
            )
        ) as PomGroup

        pomGroup.getMap() shouldContainExactly mapOf(
            Pom.DEFAULT_VARIANT to Pom(description = "common description", name = "package name"),
            "v1" to Pom(
                variant = "v1",
                group = "test.group1",
                name = "package name",
                description = "common description"
            ),
            "v2" to Pom(
                variant = "v2",
                group = "test.group2",
                name = "package name",
                description = "group2 description"
            )
        )
    }
})
