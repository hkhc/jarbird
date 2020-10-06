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

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.Properties

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

    @Test
    fun `Normal publish to Maven Repository to release repository`() {

        mockRepositoryServer.setUp("test.group", "test.artifact", "0.1", "/release")

        File("functionalTestData/testBuildMavenRepo").copyRecursively(tempProjectDir)
        File("functionalTestData/common").copyRecursively(tempProjectDir)
        File("functionalTestData/src").copyRecursively(tempProjectDir)

        Properties().apply {
            load(FileReader("$tempProjectDir/gradle.properties"))
            setProperty("repository.mock.release", mockRepositoryServer.getServerUrl())
            setProperty("repository.mock.snapshot", "${mockRepositoryServer.getServerUrl()}-snapshot")
            setProperty("repository.mock.username", "username")
            setProperty("repository.mock.password", "password")
            store(FileWriter("$tempProjectDir/gradle.properties"), "")
        }

        val result = GradleRunner.create()
            .withProjectDir(tempProjectDir)
            .withArguments("jbPublishToMavenRepository")
            .withPluginClasspath()
            .withDebug(true)
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":jbPublishToMavenRepository")?.outcome)
        mockRepositoryServer.assertReleaseArtifacts()
    }

    @Test
    fun `Normal publish to Maven Repository to snapshot repository`() {

        mockRepositoryServer.setUp("test.group", "test.artifact", "0.1-SNAPSHOT", "/snapshot")

        File("functionalTestData/testBuildMavenRepoSnapshot").copyRecursively(tempProjectDir)
        File("functionalTestData/common").copyRecursively(tempProjectDir)
        File("functionalTestData/src").copyRecursively(tempProjectDir)

        Properties().apply {
            load(FileReader("$tempProjectDir/gradle.properties"))
            setProperty("repository.mock.release", "${mockRepositoryServer.getServerUrl()}-release")
            setProperty("repository.mock.snapshot", mockRepositoryServer.getServerUrl())
            setProperty("repository.mock.username", "username")
            setProperty("repository.mock.password", "password")
            store(FileWriter("$tempProjectDir/gradle.properties"), "")
        }

        val result = GradleRunner.create()
            .withProjectDir(tempProjectDir)
            .withArguments("jbPublishToMavenRepository")
            .withPluginClasspath()
            .withDebug(true)
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":jbPublishToMavenRepository")?.outcome)
        mockRepositoryServer.assertSnapshotArtifacts()
    }
}
