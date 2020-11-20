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

import io.hkhc.gradle.internal.getGradleUserHome
import io.hkhc.gradle.pom.internal.PomGroupFactory
import io.hkhc.utils.test.mkdir
import io.hkhc.utils.test.tempDirectory
import io.kotest.assertions.asClue
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.engine.spec.tempfile
import io.kotest.extensions.system.OverrideMode
import io.kotest.extensions.system.withEnvironment
import io.kotest.extensions.system.withSystemProperties
import io.kotest.extensions.system.withSystemProperty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import java.io.File

class PomFactoryTest : StringSpec({

    lateinit var project: Project

    beforeTest {
        project = mockk()
        every { project.logger } returns Logging.getLogger(Project::class.java)
        val rootDir = tempDirectory()
        every { project.rootDir } returns rootDir
        every { project.projectDir } returns rootDir.mkdir("app")
    }

    "Load a yaml file" {

        val file = tempfile()
        file.writeText(
            """
            |group: test-group
            |artifactId: test-id
            |version: 1.2.3.4
            |description: test-description
            |packaging: jar
            """.trimMargin()
        )
        val pomGroup = PomGroupFactory(project).readPom(file).getDefault()

        withClue("Parsed POM should reflect content in file") {
            pomGroup.asClue {
                it.group shouldBe "test-group"
                it.artifactId shouldBe "test-id"
                it.version shouldBe "1.2.3.4"
                it.description shouldBe "test-description"
                it.packaging shouldBe "jar"
            }
        }
    }

    "Load a yaml file with variant" {

        val file = tempfile()
        file.writeText(
            """
            |variant: release
            |group: test-group
            |artifactId: test-id
            |version: 1.2.3.4
            |description: test-description
            |packaging: jar
            """.trimMargin()
        )
        val pomGroup = PomGroupFactory(project).readPom(file)["release"]

        withClue("Parsed POM should reflect content in file") {
            pomGroup.asClue {
                it.group shouldBe "test-group"
                it.artifactId shouldBe "test-id"
                it.version shouldBe "1.2.3.4"
                it.description shouldBe "test-description"
                it.packaging shouldBe "jar"
                it.variant shouldBe "release"
            }
        }
    }

    "Load a yaml file with path" {

        val file = tempfile()
        file.writeText(
            """
            |group: test-group
            |artifactId: test-id
            |version: 1.2.3.4
            |description: test-description
            |packaging: jar
            """.trimMargin()
        )
        val pomGroup = PomGroupFactory(project).readPom(file.absolutePath).getDefault()

        withClue("Parsed POM should reflect content in file") {
            pomGroup.asClue {
                it.group shouldBe "test-group"
                it.artifactId shouldBe "test-id"
                it.version shouldBe "1.2.3.4"
                it.description shouldBe "test-description"
                it.packaging shouldBe "jar"
            }
        }
    }

    "Load with non-exist file" {

        val file = "non-exist-file.txt"
        val pom = PomGroupFactory(project).readPom(file).getDefault()

        pom shouldBe Pom()
    }

    "Load with invalid file" {

        val file = tempfile()
        val pom = PomGroupFactory(project).readPom(file).getDefault()
        // Even if some part of the file is valid yaml, when we've got invalid part, we
        // will only return empty Pom object.
        file.writeText(
            """
            |group: my-group
            |abcdefg
            """.trimMargin()
        )

        pom shouldBe Pom()
    }

    "Resolve Gradle user home directory" {

        withClue("The default gradle home") {
            withSystemProperties(mapOf("user.home" to "dir0"), OverrideMode.SetOrOverride) {
                getGradleUserHome() shouldBe "dir0/.gradle"
            }
        }

        withClue("The gradle home provided by gradle.user.home") {
            withSystemProperties(
                mapOf(
                    "user.home" to "dir0",
                    "gradle.user.home" to "dir1"
                ),
                OverrideMode.SetOrOverride
            ) {
                getGradleUserHome() shouldBe "dir1"
            }
        }

        withClue("The gradle home provided by env GRADLE_USER_HOME") {
            withEnvironment("GRADLE_USER_HOME", "dir2") {
                getGradleUserHome() shouldBe "dir2"
            }
        }

        withClue("The gradle home from system properties override env variable") {
            withSystemProperties(
                mapOf(
                    "user.home" to "dir0",
                    "gradle.user.home" to "dir1"
                ),
                OverrideMode.SetOrOverride
            ) {
                withEnvironment("GRADLE_USER_HOME", "dir2") {
                    getGradleUserHome() shouldBe "dir1"
                }
            }
        }
    }

    "List of files to resolve POM" {

        withSystemProperties(
            mapOf(
                "user.home" to "dir0",
                "pomFile" to "my-pom.yaml"
            ),
            OverrideMode.SetOrOverride
        ) {
            PomGroupFactory(project).getPomFileList().map { it.path } shouldBe listOf(
                "my-pom.yaml",
                "${project.projectDir}/pom.yaml",
                "${project.rootDir}/pom.yaml",
                "dir0/.gradle/pom.yaml"
            )
        }
    }

    "Resolve POM from list of files" {

        val pomGroupFactory = PomGroupFactory(project)

        withClue("Single item in list") {
            pomGroupFactory.resolvePomGroup(
                listOf(tempfile().apply { writeText("group: test-group-1") })
            ).getDefault().group shouldBe "test-group-1"
        }

        withClue("Two items in list") {
            pomGroupFactory.resolvePomGroup(
                listOf(
                    tempfile().apply { writeText("group: test-group-1") },
                    tempfile().apply { writeText("group: test-group-2") }
                )
            ).getDefault().group shouldBe "test-group-2"
        }

        withClue("Three items in list") {
            pomGroupFactory.resolvePomGroup(
                listOf(
                    tempfile().apply { writeText("group: test-group-1") },
                    tempfile().apply { writeText("group: test-group-2") },
                    tempfile().apply { writeText("group: test-group-3") }
                )
            ).getDefault().group shouldBe "test-group-3"
        }
    }

    "Resolve Pom from User's gradle home" {

        val pomFactory = PomGroupFactory(project)
        val baseDir = tempDirectory()
        val gradleUserHomeDir = baseDir.mkdir("gradle-user-home")
        val pomFile = File(gradleUserHomeDir, "pom.yaml")
        pomFile.writeText(
            """
            |group: test-group
            |artifactId: test-id
            |version: 1.2.3.4
            |description: test-description
            |packaging: jar
            """.trimMargin()
        )

        withSystemProperty("gradle.user.home", gradleUserHomeDir.absolutePath) {
            val pomGroup = pomFactory.resolvePomGroup().getDefault()
            pomGroup.asClue {
                it.group shouldBe "test-group"
                it.artifactId shouldBe "test-id"
                it.version shouldBe "1.2.3.4"
                it.description shouldBe "test-description"
                it.packaging shouldBe "jar"
            }
        }
    }
})
