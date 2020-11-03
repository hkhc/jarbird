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
import io.hkhc.gradle.test.BintrayPublishingChecker
import io.hkhc.gradle.test.Coordinate
import io.hkhc.gradle.test.MavenPublishingChecker
import io.hkhc.gradle.test.MockArtifactoryRepositoryServer
import io.hkhc.gradle.test.MockBintrayRepositoryServer
import io.hkhc.gradle.test.MockMavenRepositoryServer
import io.hkhc.utils.PropertiesEditor
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class BuildMavenBintrayRepoTest {

    // https://www.baeldung.com/junit-5-temporary-directory
    @TempDir
    lateinit var tempProjectDir: File
    lateinit var mockMavenRepositoryServer: MockMavenRepositoryServer
    lateinit var mockBintrayRepositoryServer: MockBintrayRepositoryServer
    lateinit var mockArtifactoryRepositoryServer: MockArtifactoryRepositoryServer

    @BeforeEach
    fun setUp() {
        mockMavenRepositoryServer = MockMavenRepositoryServer()
        mockBintrayRepositoryServer = MockBintrayRepositoryServer()
        mockArtifactoryRepositoryServer = MockArtifactoryRepositoryServer()
    }

    @AfterEach
    fun teardown() {
        mockMavenRepositoryServer.teardown()
        mockBintrayRepositoryServer.teardown()
        mockArtifactoryRepositoryServer.teardown()
    }

    fun commonSetup(coordinate: Coordinate) {
        mockMavenRepositoryServer.setUp(coordinate, "/base")
        mockBintrayRepositoryServer.setUp(coordinate, "/base")
        mockArtifactoryRepositoryServer.setUp(coordinate, "/base")

        File("$tempProjectDir/pom.yaml")
            .writeText(simplePom(coordinate))
        File("$tempProjectDir/build.gradle.kts")
            .writeText(buildGradle())

        File("functionalTestData/keystore").copyRecursively(tempProjectDir)
        File("functionalTestData/lib").copyRecursively(tempProjectDir)
    }

    @Test
    fun `Normal publish to Maven and Bintray Repository to release repository`() {

        val coordinate = Coordinate("test.group", "test.artifact", "0.1")
        commonSetup(coordinate)

        val username = "username"
        val repo = "maven"

        PropertiesEditor("$tempProjectDir/gradle.properties") {
            setupKeyStore(tempProjectDir)
            "repository.maven.mock.release" to mockMavenRepositoryServer.getServerUrl()
            "repository.maven.mock.snapshot" to "fake-url-that-is-not-going-to-work"
            "repository.maven.mock.username" to "username"
            "repository.maven.mock.password" to "password"
            "repository.bintray.release" to mockBintrayRepositoryServer.getServerUrl()
            "repository.bintray.username" to username
            "repository.bintray.apikey" to "password"
        }

        val task = "jbPublish"
        val result = runTask(task, tempProjectDir)

        assertEquals(TaskOutcome.SUCCESS, result.task(":$task")?.outcome)
        MavenPublishingChecker(coordinate).assertReleaseArtifacts(mockMavenRepositoryServer.collectRequests())
        BintrayPublishingChecker(coordinate).assertReleaseArtifacts(
            mockBintrayRepositoryServer.collectRequests(),
            username,
            repo
        )
    }

    @Test
    fun `Normal publish to Maven and Bintray Repository to snapshot repository`() {

        val coordinate = Coordinate("test.group", "test.artifact", "0.1-SNAPSHOT")
        commonSetup(coordinate)

        val username = "username"
        val repo = "maven"

        PropertiesEditor("$tempProjectDir/gradle.properties") {
            setupKeyStore(tempProjectDir)
            "repository.maven.mock.release" to "fake-url-that-is-not-going-to-work"
            "repository.maven.mock.snapshot" to mockMavenRepositoryServer.getServerUrl()
            "repository.maven.mock.username" to "username"
            "repository.maven.mock.password" to "password"
            "repository.bintray.snapshot" to mockArtifactoryRepositoryServer.getServerUrl()
            "repository.bintray.username" to username
            "repository.bintray.apikey" to "password"
        }

        val task = "jbPublish"
        val result = runTask(task, tempProjectDir)

        assertEquals(TaskOutcome.SUCCESS, result.task(":$task")?.outcome)
        MavenPublishingChecker(coordinate).assertSnapshotArtifacts(mockMavenRepositoryServer.collectRequests())
        ArtifactoryPublishingChecker(coordinate).assertReleaseArtifacts(
            mockArtifactoryRepositoryServer.collectRequests(),
            username,
            repo
        )
    }
}
