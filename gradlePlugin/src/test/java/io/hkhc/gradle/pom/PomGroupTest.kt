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

import io.hkhc.gradle.pom.internal.PomGroupFactory
import io.hkhc.utils.test.tempDirectory
import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.io.File

class PomGroupTest : StringSpec({

    "Create a POM group" {
        PomGroup(listOf()).getMap() shouldContainExactly mapOf()
    }

    "Create a POM group with one default Pom" {
        PomGroup(
            listOf(
                Pom(group = "test.group1")
            )
        ).apply {
            getMap() shouldContainExactly mapOf()
            getDefault() shouldBe Pom(variant = Pom.DEFAULT_VARIANT, group = "test.group1")
        }
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
        ).apply {
            getMap() shouldContainExactly mapOf(
                "v1" to Pom(variant = "v1", group = "test.group1", description = "common description"),
                "v2" to Pom(variant = "v2", group = "test.group2", description = "group2 description")
            )
            getDefault() shouldBe Pom(description = "common description")
        }
    }

    "Load POMGroup with variant" {

        val baseDir = tempDirectory()

        val pomFile = File(baseDir, "pom1.yaml").apply {
            writeText(
                """
                variant: release    
                group: mygroup
                artifactId: myArtifact
                version: 1.0
                """.trimIndent()
            )
        }

        val pomGroup = PomGroupFactory.loadYaml(pomFile)
        pomGroup.getMap() shouldHaveSize 1

        pomGroup["release"]?.let { pom ->
            pom.variant shouldBe "release"
            pom.group shouldBe "mygroup"
            pom.artifactId shouldBe "myArtifact"
            pom.version shouldBe "1.0"
        } ?: fail("Failed to get POM from POMGroup")
    }

    "Load POMGroup with variant and overlay to empty PomGroup" {

        val baseDir = tempDirectory()

        val pomFile = File(baseDir, "pom1.yaml").apply {
            writeText(
                """
                variant: release    
                group: mygroup
                artifactId: myArtifact
                version: 1.0
                """.trimIndent()
            )
        }

        val pomGroup = PomGroupFactory.loadYaml(pomFile)
        pomGroup.getMap() shouldHaveSize 1

        pomGroup["release"]?.let { pom ->
            pom.variant shouldBe "release"
            pom.group shouldBe "mygroup"
            pom.artifactId shouldBe "myArtifact"
            pom.version shouldBe "1.0"
        } ?: fail("Failed to get POM from POMGroup")

        val newPomGroup = PomGroup()
        pomGroup.overlayTo(newPomGroup)

        newPomGroup.getMap() shouldHaveSize 1

        newPomGroup["release"]?.let { newPom ->
            newPom.variant shouldBe "release"
            newPom.group shouldBe "mygroup"
            newPom.artifactId shouldBe "myArtifact"
            newPom.version shouldBe "1.0"
        } ?: fail("Failed to get POM from POMGroup")
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

        pomGroup.apply {
            getMap() shouldContainExactly mapOf(
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
            getDefault() shouldBe Pom(description = "common description", name = "package name")
        }
    }

    "Poms for root project and subproject" {
        val rootPomFile = tempfile().apply {
            writeText(
                """
            group: io.hkhc.log
            version: 0.5.3-SNAPSHOT
            description: Easy Kotlin Logging Library

            licenses:
              - name: Apache-2.0
                dist: repo

            developers:
              - id: hkhc
                name: Herman Cheung
                email: herman.kh.cheung+ihlog@gmail.com

            scm:
              repoType: github.com
              repoName: hkhc/ihlog

                """.trimIndent()
            )
        }

        val subPomFile = tempfile().apply {
            writeText(
                """
            artifactId: ihlog
            version: 0.5.3-SNAPSHOT
            packaging: jar
                """.trimIndent()
            )
        }

        val pomGroup = PomGroupFactory.resolvePomGroup(
            listOf(
                rootPomFile,
                subPomFile
            )
        )

        val defaultPom = pomGroup.getDefault()

        with(defaultPom) {
            group shouldBe "io.hkhc.log"
            artifactId shouldBe "ihlog"
            version shouldBe "0.5.3-SNAPSHOT"
        }
    }
})
