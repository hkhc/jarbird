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
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class FuncTestOne {

    fun dumpFileTree(file: File, level: Int) {
        for(i in 0..level) {
            System.out.print("  ")
        }
        System.out.println(file.name)
        if (file.isDirectory()) {
            file.listFiles().forEach {
                dumpFileTree(it, level+1)
            }
        }
    }

    fun dumpFileTree(file: File) = dumpFileTree(file, 0)

    @get:Rule
    var tempProjectDir: TemporaryFolder = TemporaryFolder()

    lateinit var localRepoDir: File

    @Before
    fun setUp() {
        File("functionalTestData/testOne").copyRecursively(tempProjectDir.root)
        File("functionalTestData/common").copyRecursively(tempProjectDir.root)
        localRepoDir = File(tempProjectDir.root, "localRepo")
        localRepoDir.mkdirs()
        System.setProperty("maven.repo.local", localRepoDir.absolutePath)
    }

    @Test
    fun `Normal publish to Maven Local`() {
        val result = GradleRunner.create()
            .withProjectDir(tempProjectDir.root)
            .withArguments("publishLibPublicationToMavenLocal")
            .withPluginClasspath()
            .withDebug(true)
            .build()

        Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":publishLibPublicationToMavenLocal")?.outcome)
        ArtifactChecker().verifyRepostory(localRepoDir, "test.group", "test.artifact", "0.1", "jar")

    }
}
