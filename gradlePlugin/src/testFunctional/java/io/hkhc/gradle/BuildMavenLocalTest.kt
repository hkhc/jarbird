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
import io.hkhc.gradle.test.GradleTaskTester
import io.hkhc.utils.FileTree
import io.hkhc.utils.PropertiesEditor
import io.hkhc.utils.test.tempDirectory
import io.kotest.assertions.withClue
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestStatus
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

@Suppress("MagicNumber")
@Tags("Library", "MavenLocal")
class BuildMavenLocalTest : StringSpec({

    lateinit var tempProjectDir: File
    lateinit var tester: GradleTaskTester

    lateinit var localRepoDir: File

//    lateinit var envs: Map<String, String>

    fun commonSetup(coordinate: Coordinate) {

        File("functionalTestData/keystore").copyRecursively(tempProjectDir)
        File("functionalTestData/lib").copyRecursively(tempProjectDir)
        localRepoDir = File(tempProjectDir, "localRepo")
        localRepoDir.mkdirs()
        System.setProperty("maven.repo.local", localRepoDir.absolutePath)

        File("$tempProjectDir/build.gradle").writeText(
            """
            plugins {
                id 'java'
                id 'io.hkhc.jarbird'
            }
            repositories {
                jcenter()
            }
            """.trimIndent()
        )

        File("$tempProjectDir/pom.yaml")
            .writeText(simplePom(coordinate))

        PropertiesEditor("$tempProjectDir/gradle.properties") {
            setupKeyStore(tempProjectDir)
        }

        tester = GradleTaskTester(
            tempProjectDir,
            mutableMapOf(
                getTestGradleHomePair(tempProjectDir)
            )
        )
    }

    beforeTest {
        tempProjectDir = tempDirectory()
    }

    afterTest {
        if (it.b.status == TestStatus.Error || it.b.status == TestStatus.Failure) {
            FileTree().dump(tempProjectDir, System.out::println)
        }
    }

    // TODO need a test for zero pub

    "Zero Gradle Test" {

        val coordinate = Coordinate("test.group", "test.artifact", "0.1")
        commonSetup(coordinate)

        val result = tester.runTask("tasks")
        result.task(":tasks")?.outcome shouldBe TaskOutcome.SUCCESS
    }

    "Normal publish to Maven Local" {

        val coordinate = Coordinate("test.group", "test.artifact", "0.1")
        commonSetup(coordinate)

        val targetTask = "jbPublishToMavenLocal"

        val result = tester.runTask(targetTask)

        withClue("expected list of tasks executed with expected result") {
            result.tasks.map { it.toString() } shouldBe listOf(
                ":dokka=SUCCESS",
                ":dokkaJar=SUCCESS",
                ":compileJava=SUCCESS",
                ":processResources=NO_SOURCE",
                ":classes=SUCCESS",
                ":jar=SUCCESS",
                ":generateMetadataFileForLibPublication=SUCCESS",
                ":generatePomFileForLibPublication=SUCCESS",
                ":sourcesJar=SUCCESS",
                ":signLibPublication=SUCCESS",
                ":publishLibPublicationToMavenLocal=SUCCESS",
                ":jbPublishLibToMavenLocal=SUCCESS",
                ":jbPublishToMavenLocal=SUCCESS"
            )
        }

//        result.tasks.forEach {
//            System.out.println("\"${it}\",")
//        }

        ArtifactChecker().verifyRepository(localRepoDir, coordinate)
    }
})

//        val taskTree = treeStr(
//            StringNodeBuilder(":$targetTask").build {
//                +":jbPublishLibToMavenLocal" {
//                    +":publishLibPublicationToMavenLocal" {
//                        +":dokkaJar ..>"
//                        +":generateMetadataFileForLibPublication ..>"
//                        +":generatePomFileForLibPublication"
//                        +":jar ..>"
//                        +":signLibPublication ..>"
//                        +":sourcesJar"
//                    }
//                }
//            }
//        )
//
//        assertTaskTree(targetTask, taskTree, 3, tempProjectDir)
