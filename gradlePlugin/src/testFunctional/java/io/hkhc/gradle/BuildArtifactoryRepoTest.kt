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

import io.hkhc.gradle.test.ArtifactoryPublishingChecker
import io.hkhc.gradle.test.Coordinate
import io.hkhc.gradle.test.MockArtifactoryRepositoryServer
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

class BuildArtifactoryRepoTest {

    @TempDir
    lateinit var tempProjectDir: File
    lateinit var mockRepositoryServer: MockArtifactoryRepositoryServer

    @BeforeEach
    fun setUp() {
        mockRepositoryServer = MockArtifactoryRepositoryServer()
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
            .writeText(buildGradleCustomArtifactrory())
        File("functionalTestData/keystore").copyRecursively(tempProjectDir)
        File("functionalTestData/lib").copyRecursively(tempProjectDir)
    }

    @Test
    fun `Normal publish snapshot to Bintray Repository`() {

        val coordinate = Coordinate("test.group", "test.artifact", "0.1-SNAPSHOT")
        commonSetup(coordinate)

        val username = "username"
        val repo = "maven"

        PropertiesEditor("$tempProjectDir/gradle.properties") {
            setupKeyStore(tempProjectDir)
            "repository.bintray.snapshot" to mockRepositoryServer.getServerUrl()
            "repository.bintray.username" to username
            "repository.bintray.apikey" to "password"
        }

        val targetTask = "jbPublishToBintray"

        val taskTree = treeStr(
            StringNodeBuilder(":$targetTask").build {
                +":artifactoryPublish" {
                    +":dokkaJar" {
                        +":dokka"
                    }
                    +":generateMetadataFileForLibPublication" {
                        +":jar ..>"
                    }
                    +":generatePomFileForLibPublication"
                    +":jar" {
                        +":classes ..>"
                        +":compileKotlin"
                        +":inspectClassesForKotlinIC ..>"
                    }
                    +":sourcesJar"
                }
            }
        )

        val output = runTaskWithOutput(arrayOf(targetTask, "taskTree", "--task-depth", "3"), tempProjectDir)
        Assertions.assertEquals(
            taskTree,
            TextCutter(output.stdout).cut(":$targetTask", ""), "task tree"
        )

        val result = runTask(targetTask, tempProjectDir)

        Assertions.assertEquals(TaskOutcome.SUCCESS, result.task(":$targetTask")?.outcome)
        ArtifactoryPublishingChecker(coordinate).assertReleaseArtifacts(
            mockRepositoryServer.collectRequests().apply {
                forEach {
                    println("recorded request ${it.method} ${it.path}")
                }
            },
            username,
            repo
        )
    }
}
