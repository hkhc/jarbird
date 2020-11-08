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

import io.hkhc.gradle.test.ArtifactChecker
import io.hkhc.gradle.test.Coordinate
import io.hkhc.utils.FileTree
import io.hkhc.utils.PropertiesEditor
import io.hkhc.utils.StringNodeBuilder
import io.hkhc.utils.TextCutter
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class BuildAndroidMavenLocalTest {

    // https://www.baeldung.com/junit-5-temporary-directory
    @TempDir
    lateinit var tempProjectDir: File
    lateinit var libProj: File

    lateinit var localRepoDir: File
    lateinit var envs: MutableMap<String, String>

    @BeforeEach
    fun setUp() {

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

    @Test
    fun `Normal publish Android AAR to Maven Local`() {

        val coordinate = Coordinate("test.group", "test.artifact", "0.1", versionWithVariant = "0.1-release")

        File("$tempProjectDir/settings.gradle").writeText(commonSetting())
        File("$tempProjectDir/build.gradle").writeText(commonAndroidRootGradle())
        File("$libProj/build.gradle").writeText(commonAndroidGradle())
        File("$libProj/pom.yaml")
            .writeText("variant: release\n" + simpleAndroidPom(coordinate))

        PropertiesEditor("$tempProjectDir/gradle.properties") {
            setupKeyStore(tempProjectDir)
            setupAndroidProeprties()
        }

        val targetTask = "lib:jbPublishToMavenLocal"

        val taskTree = treeStr(
            StringNodeBuilder(":$targetTask").build {
                +":lib:jbPublishLibReleaseToMavenLocal" {
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

        assertTaskTree(targetTask, taskTree, 3, tempProjectDir, envs)

        val result = runTask(targetTask, tempProjectDir, envs)

        FileTree().dump(tempProjectDir, ::println)

        Assertions.assertEquals(TaskOutcome.SUCCESS, result.task(":$targetTask")?.outcome)
        ArtifactChecker()
            .verifyRepository(localRepoDir, coordinate, "aar")
    }
}
