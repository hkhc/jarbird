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
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class BuildMultiProjectMavenLocalTest {

    // https://www.baeldung.com/junit-5-temporary-directory
    @TempDir
    lateinit var tempProjectDir: File
    lateinit var subProj1: File
    lateinit var subProj2: File

    lateinit var localRepoDir: File

    @BeforeEach
    fun setUp() {
        subProj1 = File(tempProjectDir, "lib1")
        subProj2 = File(tempProjectDir, "lib2")
        File("functionalTestData/keystore").copyRecursively(tempProjectDir)
        File("functionalTestData/lib/src").copyRecursively(subProj1)
        File("functionalTestData/lib2/src").copyRecursively(subProj2)
        localRepoDir = File(tempProjectDir, "localRepo")
        localRepoDir.mkdirs()
        System.setProperty("maven.repo.local", localRepoDir.absolutePath)
    }

    @Test
    fun `Normal publish two sub-projects to Maven Local`() {

        val coordinate1 = Coordinate("test.group", "test.artifact.one", "0.1")
        val coordinate2 = Coordinate("test.group", "test.artifact.two", "0.1")

        File("$tempProjectDir/settings.gradle").writeText(
            """
            include(":lib1", ":lib2")
            """.trimIndent()
        )
        File("$tempProjectDir/build.gradle").writeText(
            """
            plugins {
                id 'io.hkhc.jarbird'
            }
            """.trimIndent()
        )
        File("$tempProjectDir/pom.yaml").writeText(simplePomRoot())
        PropertiesEditor("$tempProjectDir/gradle.properties") {
            setupKeyStore(tempProjectDir)
        }

        File("$subProj1/pom.yaml").writeText(simpleSubProj(coordinate1))
        File("$subProj1/build.gradle").writeText(
            """
            plugins {
                id 'java'
                id 'io.hkhc.jarbird'
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

        File("$subProj2/pom.yaml").writeText(simpleSubProj(coordinate2))
        File("$subProj2/build.gradle").writeText(
            """
            plugins {
                id 'java'
                id 'io.hkhc.jarbird'
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

        val task = "jbPublishToMavenLocal"
        val result = runTask(task, tempProjectDir)

        FileTree().dump(tempProjectDir, System.out::println)

        Assertions.assertEquals(TaskOutcome.SUCCESS, result.task(":$task")?.outcome)
        ArtifactChecker()
            .verifyRepostory(localRepoDir, coordinate1, "jar")
        ArtifactChecker()
            .verifyRepostory(localRepoDir, coordinate2, "jar")
    }
}
