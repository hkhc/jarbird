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
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * snapshot / release
 * +- MavenLocal
 * +- MavenRepository
 * + Bintray
 * + artifactory
 * Android AAR
 * multivariant Android AAR
 *
 * Multi-project
 *
 * snapshot / release
 * plugin gradle plugin portal
 * + plugin mavenLocal
 * + plugin mavenrepository
 * + plugin bintray
 * - plugin artifactory
 *
 * all - mavenrepository
 * all - bintray
 *
 * gradle versions
 * signing v1 signing v2
 * groovy/kts script
 * alternate project name
 * credential with env variable
 * handle publishing rejection
 *
 */
class BuildMavenRepoTest {

    // https://www.baeldung.com/junit-5-temporary-directory
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
        File("functionalTestData/lib/src").copyRecursively(tempProjectDir)
    }

    @Test
    fun `Normal publish to Maven Repository to release repository`() {

        val coordinate = Coordinate("test.group", "test.artifact", "0.1")
        commonSetup(coordinate)

        PropertiesEditor("$tempProjectDir/gradle.properties") {
            setupKeyStore()
            "repository.maven.mock.release" to mockRepositoryServer.getServerUrl()
            "repository.maven.mock.snapshot" to "fake-url-that-is-not-going-to-work"
            "repository.maven.mock.username" to "username"
            "repository.maven.mock.password" to "password"
        }

        val task = "jbPublishToMavenRepository"
        val result = runTask(task, tempProjectDir)

        assertEquals(TaskOutcome.SUCCESS, result.task(":$task")?.outcome)
        MavenPublishingChecker(coordinate).assertReleaseArtifacts(mockRepositoryServer.collectRequests())
    }

    @Test
    fun `Normal publish to Maven Repository to snapshot repository`() {

        val coordinate = Coordinate("test.group", "test.artifact", "0.1-SNAPSHOT")
        commonSetup(coordinate)

        PropertiesEditor("$tempProjectDir/gradle.properties") {
            setupKeyStore()
            "repository.maven.mock.release" to "fake-url-that-is-not-going-to-work"
            "repository.maven.mock.snapshot" to mockRepositoryServer.getServerUrl()
            "repository.maven.mock.username" to "username"
            "repository.maven.mock.password" to "password"
        }

        val task = "jbPublishToMavenRepository"
        val result = runTask(task, tempProjectDir)

        assertEquals(TaskOutcome.SUCCESS, result.task(":$task")?.outcome)
        MavenPublishingChecker(coordinate).assertSnapshotArtifacts(mockRepositoryServer.collectRequests())
    }
}
