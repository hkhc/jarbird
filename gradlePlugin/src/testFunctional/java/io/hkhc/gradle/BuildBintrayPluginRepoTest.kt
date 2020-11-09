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

import io.hkhc.gradle.test.BintrayPublishingChecker
import io.hkhc.gradle.test.Coordinate
import io.hkhc.gradle.test.MockBintrayRepositoryServer
import io.hkhc.utils.PropertiesEditor
import io.hkhc.utils.StringNodeBuilder
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

@Suppress("MagicNumber")
class BuildBintrayPluginRepoTest {

    @TempDir
    lateinit var tempProjectDir: File
    private lateinit var mockRepositoryServer: MockBintrayRepositoryServer

    @BeforeEach
    fun setUp() {
        mockRepositoryServer = MockBintrayRepositoryServer()
    }

    @AfterEach
    fun teardown() {
        mockRepositoryServer.teardown()
    }

    fun commonSetup(coordinate: Coordinate) {
        mockRepositoryServer.setUp(coordinate, "/base")

        File("$tempProjectDir/pom.yaml")
            .writeText(
                simplePom(coordinate) + '\n' +
                    pluginPom(coordinate.pluginId ?: "non-exist-plugin-id", "TestPluginClass")
            )
        File("$tempProjectDir/build.gradle.kts")
            .writeText(buildGradleCustomBintray())

        File("functionalTestData/keystore").copyRecursively(tempProjectDir)
        File("functionalTestData/lib").copyRecursively(tempProjectDir)
    }

    @Test
    fun `Normal release publish plugin to Bintray Repository`() {

        val coordinate = Coordinate("test.group", "test.artifact", "0.1", "test.plugin")
        commonSetup(coordinate)

        val username = "username"
        val repo = "maven"

        PropertiesEditor("$tempProjectDir/gradle.properties") {
            setupKeyStore(tempProjectDir)
            "repository.bintray.release" to mockRepositoryServer.getServerUrl()
            "repository.bintray.username" to username
            "repository.bintray.apikey" to "password"
        }

        val targetTask = "jbPublishToBintray"

        val taskTree = treeStr(
            StringNodeBuilder(":$targetTask").build {
                +":bintrayUpload" {
                    +":_bintrayRecordingCopy" {
                        +":signLibPublication ..>"
                    }
                    +":publishLibPluginMarkerMavenPublicationToMavenLocal" {
                        +":generatePomFileForLibPluginMarkerMavenPublication"
                    }
                    +":publishLibPublicationToMavenLocal" {
                        +":dokkaJar ..>"
                        +":generateMetadataFileForLibPublication ..>"
                        +":generatePomFileForLibPublication"
                        +":jar ..>"
                        +":signLibPublication ..>"
                        +":sourcesJar"
                    }
                }
            }
        )

        assertTaskTree(targetTask, taskTree, 3, tempProjectDir)

        val result = runTask(targetTask, tempProjectDir)

        Assertions.assertEquals(TaskOutcome.SUCCESS, result.task(":$targetTask")?.outcome)
        BintrayPublishingChecker(coordinate).assertReleaseArtifacts(
            mockRepositoryServer.collectRequests(),
            username,
            repo
        )
    }
}
