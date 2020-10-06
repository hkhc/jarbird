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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class BuildMavenLocalTest {

    // https://www.baeldung.com/junit-5-temporary-directory
    @TempDir
    lateinit var tempProjectDir: File

    lateinit var localRepoDir: File

    @BeforeEach
    fun setUp() {
        File("functionalTestData/testBuildMavenLocal").copyRecursively(tempProjectDir)
        File("functionalTestData/common").copyRecursively(tempProjectDir)
        localRepoDir = File(tempProjectDir, "localRepo")
        localRepoDir.mkdirs()
        System.setProperty("maven.repo.local", localRepoDir.absolutePath)
    }

    @Test
    fun `Normal publish to Maven Local`() {
        val result = GradleRunner.create()
            .withProjectDir(tempProjectDir)
            .withArguments("jbPublishToMavenLocal")
            .withPluginClasspath()
            .withDebug(true)
            .build()

        FileTree().dump(tempProjectDir, System.out::println)

        assertEquals(TaskOutcome.SUCCESS, result.task(":publishLibPublicationToMavenLocal")?.outcome)
        ArtifactChecker().verifyRepostory(localRepoDir, "test.group", "test.artifact", "0.1", "jar")
    }

//    @Test
//    fun `Normal publish to Maven Repository`() {
//        val result = GradleRunner.create()
//            .withProjectDir(tempProjectDir.root)
//            .withArguments("publishLibPublicationToMavenRepository")
//            .withPluginClasspath()
//            .withDebug(true)
//            .build()
//
//        FileTree().dump(tempProjectDir.root, System.out::println)
//
//        Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":publishLibPublicationToMavenLocal")?.outcome)
//        ArtifactChecker().verifyRepostory(localRepoDir, "test.group", "test.artifact", "0.1", "jar")
//    }
}
