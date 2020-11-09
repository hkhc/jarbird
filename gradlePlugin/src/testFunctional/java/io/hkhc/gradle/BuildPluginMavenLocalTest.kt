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
@Tags("Plugin", "MavenLocal")
class BuildPluginMavenLocalTest : StringSpec({

    lateinit var tempProjectDir: File
    lateinit var tester: GradleTaskTester

    lateinit var localRepoDir: File

    fun commonSetup(coordinate: Coordinate) {
        File("functionalTestData/keystore").copyRecursively(tempProjectDir)
        File("functionalTestData/plugin/src").copyRecursively(tempProjectDir)
        localRepoDir = File(tempProjectDir, "localRepo")
        localRepoDir.mkdirs()
        System.setProperty("maven.repo.local", localRepoDir.absolutePath)

        File("$tempProjectDir/pom.yaml")
            .writeText(
                simplePom(coordinate) + '\n' +
                    pluginPom(coordinate.pluginId ?: "non-exist-plugin-id", "TestPlugin")
            )

        File("$tempProjectDir/build.gradle").writeText(
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

    "Normal publish plugin to Maven Local" {

        val coordinate = Coordinate("test.group", "test.artifact", "0.1", "test.plugin")
        commonSetup(coordinate)

        val targetTask = "jbPublishToMavenLocal"

        val result = tester.runTask(targetTask)

        withClue("expected list of tasks executed with expected result") {
            result.tasks.map { it.toString() } shouldBe listOf(
                ":generatePomFileForLibPluginMarkerMavenPublication=SUCCESS",
                ":publishLibPluginMarkerMavenPublicationToMavenLocal=SUCCESS",
                ":dokka=SUCCESS",
                ":dokkaJar=SUCCESS",
                ":compileJava=NO_SOURCE",
                ":pluginDescriptors=SUCCESS",
                ":processResources=SUCCESS",
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

        ArtifactChecker().verifyRepository(localRepoDir, coordinate)
    }
})

/*
val taskTree = treeStr(
    StringNodeBuilder(":$targetTask").build {
        +":jbPublishLibToMavenLocal" {
            +":publishLibPluginMarkerMavenPublicationToMavenLocal" {
                +":generatePomFileForLibPluginMarkerMavenPublication"
            }
            +":publishLibPublicationToMavenLocal" {
                +":dokkaJar ..>"
                +":generateMetadataFileForLibPublication ..>"
                +":generatePomFileForLibPublication"
                +":jar ..>"
                +":signLibPublication ..>"
                +":sourcesJar"
            }
        }
    }
)
*/
