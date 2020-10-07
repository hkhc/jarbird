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

import io.hkhc.gradle.test.MockRepositoryServer
import io.hkhc.test.utils.FileTree
import io.hkhc.test.utils.PropertiesEditor
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * snapshot / release
 * - MavenLocal
 * - MavenRepository
 * - Bintray
 * - artifactory
 * - Android AAR
 * - multivariant Android AAR
 *
 * Multi-project
 *
 * snapshot / release
 * plugin gradle plugin portal
 * plugin mavenLocal
 * plugin mavenrepository
 * plugin bintray
 * plugin artifactory
 *
 * all - mavenrepository
 * all - bintray
 *
 * gradle versions
 * signing v1 signing v2
 * groovy/kts script
 * alternate project name
 *
 *
 */


class BuildMavenRepoTest {

    // https://www.baeldung.com/junit-5-temporary-directory
    @TempDir
    lateinit var tempProjectDir: File
    lateinit var mockRepositoryServer: MockRepositoryServer

    @BeforeEach
    fun setUp() {
        mockRepositoryServer = MockRepositoryServer()
    }

    @AfterEach
    fun teardown() {
        mockRepositoryServer.teardown()
    }

    fun commonSetup(group: String, artifactId: String, version: String, basePath: String) {
        mockRepositoryServer.setUp(group, artifactId, version, basePath)

        File("$tempProjectDir/pom.yaml")
            .writeText(simplePom(group, artifactId, version))
        File("$tempProjectDir/build.gradle.kts")
            .writeText(buildGradle())

        File("functionalTestData/keystore").copyRecursively(tempProjectDir)
        File("functionalTestData/src").copyRecursively(tempProjectDir)
    }

    @Test
    fun `Normal publish to Maven Repository to release repository`() {

        commonSetup("test.group", "test.artifact", "0.1", "/release")

        PropertiesEditor("$tempProjectDir/gradle.properties") {
            setupKeyStore()
            "repository.mock.release" to mockRepositoryServer.getServerUrl()
            "repository.mock.snapshot" to "fake-url-that-is-not-going-to-work"
            "repository.mock.username" to "username"
            "repository.mock.password" to "password"
        }

        val task = "jbPublishToMavenRepository"

        val result = GradleRunner.create()
            .withProjectDir(tempProjectDir)
            .withArguments(task)
            .withPluginClasspath()
            .withDebug(true)
            .build()

        FileTree().dump(tempProjectDir, System.out::println)

        assertEquals(TaskOutcome.SUCCESS, result.task(":$task")?.outcome)
        mockRepositoryServer.assertReleaseArtifacts()
    }

    @Test
    fun `Normal publish to Maven Repository to snapshot repository`() {

        commonSetup("test.group", "test.artifact", "0.1-SNAPSHOT", "/snapshot")

        PropertiesEditor("$tempProjectDir/gradle.properties") {
            setupKeyStore()
            "repository.mock.release" to "fake-url-that-is-not-going-to-work"
            "repository.mock.snapshot" to mockRepositoryServer.getServerUrl()
            "repository.mock.username" to "username"
            "repository.mock.password" to "password"
        }

        val task = "jbPublishToMavenRepository"

        val result = GradleRunner.create()
            .withProjectDir(tempProjectDir)
            .withArguments(task)
            .withPluginClasspath()
            .withDebug(true)
            .build()

        FileTree().dump(tempProjectDir, System.out::println)

        assertEquals(TaskOutcome.SUCCESS, result.task(":$task")?.outcome)
        mockRepositoryServer.assertSnapshotArtifacts()
    }
}
