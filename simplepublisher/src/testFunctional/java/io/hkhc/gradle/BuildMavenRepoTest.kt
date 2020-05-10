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

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class BuildMavenRepoTest {

    // https://www.baeldung.com/junit-5-temporary-directory
    @TempDir
    lateinit var tempProjectDir: File

    // we still need local repo for building purpose
    lateinit var localRepoDir: File
    lateinit var server: MockWebServer

    @BeforeEach
    fun setUp() {
        File("functionalTestData/testBuildMavenRepo").copyRecursively(tempProjectDir)
        File("functionalTestData/common").copyRecursively(tempProjectDir)

        localRepoDir = File(tempProjectDir, "localRepo")
        localRepoDir.mkdirs()
        System.setProperty("maven.repo.local", localRepoDir.absolutePath)

        server = MockWebServer()

        server.enqueue(MockResponse().setResponseCode(200))
        server.enqueue(MockResponse().setResponseCode(200))

        server.start()

        System.out.println("setUp")

        System.setProperty("repository.mock.release", server.url("/base").toString())
        System.setProperty("repository.mock.snapshot", server.url("/base").toString())
        System.setProperty("repository.mock.username", "username")
        System.setProperty("repository.mock.password", "password")
    }

    @Test
    fun `Normal publish to Maven Repository`() {
        val result = GradleRunner.create()
            .withProjectDir(tempProjectDir)
            .withArguments("spPublishToMavenRepository")
            .withPluginClasspath()
            .withDebug(true)
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":spPublishToMavenRepository")?.outcome)
        assertEquals(10, server.requestCount)
    }
}
