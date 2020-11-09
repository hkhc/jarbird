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
import io.hkhc.utils.FileTree
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
class BuildAndroidBintrayTest {

    // https://www.baeldung.com/junit-5-temporary-directory
    @TempDir
    private lateinit var tempProjectDir: File
    private lateinit var mockRepositoryServer: MockBintrayRepositoryServer
    private lateinit var libProj: File

    private lateinit var localRepoDir: File
    private lateinit var envs: MutableMap<String, String>

    @BeforeEach
    fun setUp() {

        mockRepositoryServer = MockBintrayRepositoryServer()

        envs = defaultEnvs(tempProjectDir).apply {
            val pair = getTestAndroidSdkHomePair()
            put(pair.first, pair.second)
        }

        libProj = File(tempProjectDir, "lib")
        File("functionalTestData/keystore").copyRecursively(tempProjectDir)
        File("functionalTestData/libaar").copyRecursively(libProj)
        localRepoDir = File(tempProjectDir, "localRepo")
        localRepoDir.mkdirs()
        System.setProperty("maven.repo.local", localRepoDir.absolutePath)
    }

    @AfterEach
    fun teardown() {
        mockRepositoryServer.teardown()
    }

    fun taskTree(taskName: String) = treeStr(
        StringNodeBuilder(":$taskName").build {
            +":lib:bintrayUpload" {
                +":lib:_bintrayRecordingCopy" {
                    +":lib:signLibReleasePublication ..>"
                }
                +":lib:publishLibReleasePublicationToMavenLocal" {
                    +":lib:bundleReleaseAar ..>"
                    +":lib:dokkaJarRelease ..>"
                    +":lib:generateMetadataFileForLibReleasePublication ..>"
                    +":lib:generatePomFileForLibReleasePublication"
                    +":lib:signLibReleasePublication ..>"
                    +":lib:sourcesJarRelease"
                }
            }
        }
    )

    @Test
    fun `Normal publish Android AAR to Bintray with version variant`() {

        val coordinate = Coordinate(
            "test.group",
            "test.artifact",
            "0.1",
            versionWithVariant = "0.1-release"
        )
        mockRepositoryServer.setUp(coordinate, "/base")

        File("$tempProjectDir/settings.gradle").writeText(commonSetting())
        File("$tempProjectDir/build.gradle").writeText(commonAndroidRootGradle())
        File("$libProj/build.gradle").writeText(commonAndroidGradle(variantMode = "variantWithVersion()"))
        File("$libProj/pom.yaml")
            .writeText("variant: release\n" + simplePom(coordinate, "release", "aar"))

        val username = "username"
        val repo = "maven"

        PropertiesEditor("$tempProjectDir/gradle.properties") {
            setupKeyStore(tempProjectDir)
            "repository.bintray.release" to mockRepositoryServer.getServerUrl()
            "repository.bintray.username" to username
            "repository.bintray.apikey" to "password"
            setupAndroidProperties()
        }

        val targetTask = "lib:jbPublishToBintray"

        assertTaskTree(targetTask, taskTree(targetTask), 3, tempProjectDir, envs)

        val result = runTask(targetTask, tempProjectDir, envs)

        FileTree().dump(tempProjectDir, ::println)

        Assertions.assertEquals(TaskOutcome.SUCCESS, result.task(":$targetTask")?.outcome)
        BintrayPublishingChecker(coordinate, "aar").assertReleaseArtifacts(
            mockRepositoryServer.collectRequests(),
            username,
            repo
        )
    }

    @Test
    fun `Normal publish Android AAR to Bintray with artifactId variant`() {

        val coordinate = Coordinate(
            "test.group",
            "test.artifact",
            "0.1",
            artifactIdWithVariant = "test.artifact-release"
        )
        mockRepositoryServer.setUp(coordinate, "/base")

        File("$tempProjectDir/settings.gradle").writeText(commonSetting())
        File("$tempProjectDir/build.gradle").writeText(commonAndroidRootGradle())
        File("$libProj/build.gradle").writeText(commonAndroidGradle(variantMode = "variantWithArtifactId()"))
        File("$libProj/pom.yaml")
            .writeText(simplePom(coordinate, "release", "aar"))

        val username = "username"
        val repo = "maven"

        PropertiesEditor("$tempProjectDir/gradle.properties") {
            setupKeyStore(tempProjectDir)
            "repository.bintray.release" to mockRepositoryServer.getServerUrl()
            "repository.bintray.username" to username
            "repository.bintray.apikey" to "password"
            setupAndroidProperties()
        }

        val targetTask = "lib:jbPublishToBintray"

        assertTaskTree(targetTask, taskTree(targetTask), 3, tempProjectDir, envs)

        val result = runTask(targetTask, tempProjectDir, envs)

        FileTree().dump(tempProjectDir, ::println)

        Assertions.assertEquals(TaskOutcome.SUCCESS, result.task(":$targetTask")?.outcome)
        BintrayPublishingChecker(coordinate, "aar").assertReleaseArtifacts(
            mockRepositoryServer.collectRequests(),
            username,
            repo
        )
    }
}
