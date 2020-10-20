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

import io.hkhc.gradle.test.ArtifactChecker
import io.hkhc.gradle.test.Coordinate
import io.hkhc.utils.PropertiesEditor
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class BuildPluginMavenLocalTest {

    // https://www.baeldung.com/junit-5-temporary-directory
    @TempDir
    lateinit var tempProjectDir: File

    lateinit var localRepoDir: File

    @BeforeEach
    fun setUp() {
        File("functionalTestData/keystore").copyRecursively(tempProjectDir)
        File("functionalTestData/plugin/src").copyRecursively(tempProjectDir)
        localRepoDir = File(tempProjectDir, "localRepo")
        localRepoDir.mkdirs()
        System.setProperty("maven.repo.local", localRepoDir.absolutePath)
    }

    @Test
    fun `Normal publish plugin to Maven Local`() {

        val coordinate = Coordinate("test.group", "test.artifact", "0.1", "test.plugin")

        File("$tempProjectDir/pom.yaml")
            .writeText(
                simplePom(coordinate) + '\n' +
                    pluginPom(coordinate.pluginId ?: "non-exist-plugin-id", "TestPlugin")
            )

        File("$tempProjectDir/build.gradle").writeText(
            """
            plugins {
                id 'java'
                id 'io.hkhc.jarbird'
            }
            jarbird {
                pub {
                    maven = false
                }
            }
            """.trimIndent()
        )

        PropertiesEditor("$tempProjectDir/gradle.properties") {
            setupKeyStore(tempProjectDir)
        }

        val task = "jbPublishToMavenLocal"
        val result = runTask(task, tempProjectDir)

        assertEquals(TaskOutcome.SUCCESS, result.task(":$task")?.outcome)
        ArtifactChecker()
            .verifyRepository(localRepoDir, coordinate, "jar")
    }
}
