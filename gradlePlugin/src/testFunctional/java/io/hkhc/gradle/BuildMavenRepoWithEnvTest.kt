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

import io.hkhc.gradle.test.Coordinate
import io.hkhc.gradle.test.MavenPublishingChecker
import io.hkhc.gradle.test.MockMavenRepositoryServer
import io.hkhc.utils.PropertiesEditor
import io.hkhc.utils.StringNodeBuilder
import io.hkhc.utils.TextCutter
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class BuildMavenRepoWithEnvTest {

    @TempDir
    lateinit var tempProjectDir: File
    lateinit var mockRepositoryServer: MockMavenRepositoryServer

    @BeforeEach
    fun setUp() {
        mockRepositoryServer = MockMavenRepositoryServer()
    }

    @AfterEach
    fun teardown() {
        mockRepositoryServer.teardown()
    }

    fun commonSetup(coordinate: Coordinate) {
        mockRepositoryServer.setUp(coordinate, "/base")

        File("$tempProjectDir/pom.yaml")
            .writeText(simplePom(coordinate))
        File("$tempProjectDir/build.gradle.kts")
            .writeText(buildGradle())

        File("functionalTestData/keystore").copyRecursively(tempProjectDir)
        File("functionalTestData/lib").copyRecursively(tempProjectDir)
    }

    @Test
    fun `Normal publish to Maven Repository to release repository`() {

        val coordinate = Coordinate("test.group", "test.artifact", "0.1")
        commonSetup(coordinate)

        PropertiesEditor("$tempProjectDir/gradle.properties") {
            setupKeyStore(tempProjectDir)
            "repository.maven.mock.release" to mockRepositoryServer.getServerUrl()
            "repository.maven.mock.snapshot" to "fake-url-that-is-not-going-to-work"
            "repository.maven.mock.username" to "username"
            "repository.maven.mock.password" to "password"
        }

        val targetTask = "jbPublishToMavenRepository"

        val taskTree = treeStr(
            StringNodeBuilder(":$targetTask").build {
                +":jbPublishLibToMavenRepository" {
                    +":jbPublishLibToMavenmock" {
                        +":publishLibPublicationToMavenLibRepository" {
                            +":dokkaJar ..>"
                            +":generateMetadataFileForLibPublication ..>"
                            +":generatePomFileForLibPublication"
                            +":jar ..>"
                            +":signLibPublication ..>"
                            +":sourcesJar"
                        }
                    }
                }
            }
        )

        val output = runTaskWithOutput(arrayOf(targetTask, "taskTree", "--task-depth", "4"), tempProjectDir)
        Assertions.assertEquals(
            taskTree,
            TextCutter(output.stdout).cut(":$targetTask", ""), "task tree"
        )

        val result = runTask(targetTask, tempProjectDir)

        Assertions.assertEquals(TaskOutcome.SUCCESS, result.task(":$targetTask")?.outcome)
        MavenPublishingChecker(coordinate).assertReleaseArtifacts(mockRepositoryServer.collectRequests())
    }
}
