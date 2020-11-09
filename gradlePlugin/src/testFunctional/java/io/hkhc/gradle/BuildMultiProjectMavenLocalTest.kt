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
import java.io.File

@Suppress("MagicNumber")
@Tags("Library", "MavenLocal", "Multi")
class BuildMultiProjectMavenLocalTest : StringSpec({

    lateinit var tempProjectDir: File
    lateinit var tester: GradleTaskTester
    lateinit var subProj1: File
    lateinit var subProj2: File

    lateinit var localRepoDir: File

    fun commonSetup(vararg coordinates: Coordinate) {

        subProj1 = File(tempProjectDir, "lib1")
        subProj2 = File(tempProjectDir, "lib2")
        File("functionalTestData/keystore").copyRecursively(tempProjectDir)
        File("functionalTestData/lib").copyRecursively(subProj1)
        File("functionalTestData/lib2").copyRecursively(subProj2)

        File("$tempProjectDir/settings.gradle").writeText(
            """
            include(":lib1", ":lib2")
            """.trimIndent()
        )
        File("$tempProjectDir/build.gradle").writeText(
            """
            plugins {
                id 'io.hkhc.jarbird'
                id 'com.dorongold.task-tree' version '1.5'
            }
            """.trimIndent()
        )
        File("$tempProjectDir/pom.yaml").writeText(simplePomRoot())
        PropertiesEditor("$tempProjectDir/gradle.properties") {
            setupKeyStore(tempProjectDir)
        }

        File("$subProj1/pom.yaml").writeText(simpleSubProj(coordinates[0]))
        File("$subProj1/build.gradle").writeText(
            """
            plugins {
                id 'java'
                id 'io.hkhc.jarbird'
                id 'com.dorongold.task-tree'
            }
            repositories {
                jcenter()
            }
            jarbird {
                pub {
                    maven = false
                }
            }
            """.trimIndent()
        )

        File("$subProj2/pom.yaml").writeText(simpleSubProj(coordinates[1]))
        File("$subProj2/build.gradle").writeText(
            """
            plugins {
                id 'java'
                id 'io.hkhc.jarbird'
                id 'com.dorongold.task-tree'
            }
            repositories {
                jcenter()
            }
            jarbird {
                pub {
                    maven = false
                }
            }
            """.trimIndent()
        )

        localRepoDir = File(tempProjectDir, "localRepo")
        localRepoDir.mkdirs()
        System.setProperty("maven.repo.local", localRepoDir.absolutePath)

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

    "Normal publish two sub-projects to Maven Local" {

        val coordinate1 = Coordinate("test.group", "test.artifact.one", "0.1")
        val coordinate2 = Coordinate("test.group", "test.artifact.two", "0.1")

        commonSetup(coordinate1, coordinate2)

        val targetTask = "jbPublishToMavenLocal"

        val result = tester.runTask(targetTask)
        result.tasks.forEach {
            System.out.println("\"${it}\",")
        }

        withClue("expected list of tasks executed with expected result") {
            result.tasks.map { it.toString() } shouldBe listOf(
                ":lib1:dokka=SUCCESS",
                ":lib1:dokkaJar=SUCCESS",
                ":lib1:compileJava=SUCCESS",
                ":lib1:processResources=NO_SOURCE",
                ":lib1:classes=SUCCESS",
                ":lib1:jar=SUCCESS",
                ":lib1:generateMetadataFileForLibPublication=SUCCESS",
                ":lib1:generatePomFileForLibPublication=SUCCESS",
                ":lib1:sourcesJar=SUCCESS",
                ":lib1:signLibPublication=SUCCESS",
                ":lib1:publishLibPublicationToMavenLocal=SUCCESS",
                ":lib1:jbPublishLibToMavenLocal=SUCCESS",
                ":lib1:jbPublishToMavenLocal=SUCCESS",
                ":lib2:dokka=SUCCESS",
                ":lib2:dokkaJar=SUCCESS",
                ":lib2:compileJava=SUCCESS",
                ":lib2:processResources=NO_SOURCE",
                ":lib2:classes=SUCCESS",
                ":lib2:jar=SUCCESS",
                ":lib2:generateMetadataFileForLibPublication=SUCCESS",
                ":lib2:generatePomFileForLibPublication=SUCCESS",
                ":lib2:sourcesJar=SUCCESS",
                ":lib2:signLibPublication=SUCCESS",
                ":lib2:publishLibPublicationToMavenLocal=SUCCESS",
                ":lib2:jbPublishLibToMavenLocal=SUCCESS",
                ":lib2:jbPublishToMavenLocal=SUCCESS",
                ":jbPublishToMavenLocal=SUCCESS"
            )
        }

        ArtifactChecker().verifyRepository(localRepoDir, coordinate1)
        ArtifactChecker().verifyRepository(localRepoDir, coordinate2)
    }
})

//val taskTree = treeStr(
//    StringNodeBuilder(":$targetTask").build {
//        +":lib1:jbPublishToMavenLocal" {
//            +":lib1:jbPublishLibToMavenLocal" {
//                +":lib1:publishLibPublicationToMavenLocal ..>"
//            }
//        }
//        +":lib2:jbPublishToMavenLocal" {
//            +":lib2:jbPublishLibToMavenLocal" {
//                +":lib2:publishLibPublicationToMavenLocal ..>"
//            }
//        }
//    }
//)
