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

package io.hkhc.gradle.android

import io.hkhc.gradle.test.BintrayRepoResult
import io.hkhc.gradle.test.Coordinate
import io.hkhc.gradle.test.DefaultGradleProjectSetup
import io.hkhc.gradle.test.bintray.MockBintrayRepositoryServer
import io.hkhc.gradle.test.bintray.publishedToBintrayRepositoryCompletely
import io.hkhc.gradle.test.commonAndroidGradle
import io.hkhc.gradle.test.commonAndroidRootGradle
import io.hkhc.gradle.test.except
import io.hkhc.gradle.test.getTaskTree
import io.hkhc.gradle.test.getTestAndroidSdkHomePair
import io.hkhc.gradle.test.printFileTree
import io.hkhc.gradle.test.setupAndroidProperties
import io.hkhc.gradle.test.shouldBeNoDifference
import io.hkhc.gradle.test.simplePom
import io.hkhc.test.utils.test.tempDirectory
import io.hkhc.utils.tree.NoBarTheme
import io.hkhc.utils.tree.Tree
import io.hkhc.utils.tree.chopChilds
import io.hkhc.utils.tree.stringTreeOf
import io.hkhc.utils.tree.toStringTree
import io.kotest.assertions.withClue
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContextScope
import io.kotest.core.test.TestStatus
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import java.io.File

@Tags("Multi", "Bintray", "AAR", "Variant")
class BuildAndroidBintrayTest : FunSpec({

    context("Publish Android AAR in to Bintray Repository") {

        val targetTask = "jbPublishToBintray"

        val expectedTaskGraph = stringTreeOf(NoBarTheme) {
            ":lib:jbPublishToBintray SUCCESS" {
                ":lib:bintrayUpload SUCCESS" {
                    ":bintrayPublish SUCCESS"() // TODO Sounds not great to have root task depends on child project tasks
                    ":lib:publishTestArtifactReleasePublicationToMavenLocal SUCCESS" {
                        ":lib:bundleReleaseAar SUCCESS"()
                        ":lib:generateMetadataFileForTestArtifactReleasePublication SUCCESS" {
                            ":lib:bundleReleaseAar SUCCESS"()
                        }
                        ":lib:generatePomFileForTestArtifactReleasePublication SUCCESS"()
                        ":lib:jbDokkaJarTestArtifactRelease SUCCESS" {
                            ":lib:jbDokkaHtmlTestArtifactRelease SUCCESS"()
                        }
                        ":lib:signTestArtifactReleasePublication SUCCESS" {
                            ":lib:bundleReleaseAar SUCCESS"()
                            ":lib:generateMetadataFileForTestArtifactReleasePublication SUCCESS" {
                                ":lib:bundleReleaseAar SUCCESS"()
                            }
                            ":lib:generatePomFileForTestArtifactReleasePublication SUCCESS" {

                            }
                            ":lib:jbDokkaJarTestArtifactRelease SUCCESS" {
                                ":lib:jbDokkaHtmlTestArtifactRelease SUCCESS"()
                            }
                            ":lib:sourcesJarTestArtifactRelease SUCCESS"()
                        }
                        ":lib:sourcesJarTestArtifactRelease SUCCESS"()
                    }
                }
            }
        }

        fun commonSetup(coordinate: Coordinate, expectedTaskGraph: Tree<String>): DefaultGradleProjectSetup {

            val projectDir = tempDirectory()

            return DefaultGradleProjectSetup(projectDir).apply {

                subProjDirs = arrayOf("lib")
                sourceSetTemplateDirs = arrayOf("functionalTestData/libaar")
                setup()
                mockServers.add(
                    MockBintrayRepositoryServer().apply {
                        setUp(listOf(coordinate), "/base")
                    }
                )

                envs.apply {
                    val pair = getTestAndroidSdkHomePair()
                    put(pair.first, pair.second)
                }

                setupSettingsGradle(
                    """
                    pluginManagement {
                        repositories {
                            mavenLocal()
                            gradlePluginPortal()
                            mavenCentral()
                        }
                    }
                    """.trimIndent()
                )

                writeFile("build.gradle", commonAndroidRootGradle())
                writeFile(
                    "${subProjDirs[0]}/pom.yaml",
                    simplePom(coordinate, "release", "aar")
                )

                setupGradleProperties {
                    setupAndroidProperties()
                    "repository.bintray.release" to mockServers[0].getServerUrl()
                    "repository.bintray.username" to "username"
                    "repository.bintray.apikey" to "password"
                }

                this.expectedTaskGraph = expectedTaskGraph
            }
        }

        suspend fun FunSpecContextScope.testBody(coordinate: Coordinate, setup: DefaultGradleProjectSetup) {

            afterTest {
                setup.mockServers.forEach { it.teardown() }
                if (it.b.status == TestStatus.Error || it.b.status == TestStatus.Failure) {
                    printFileTree(setup.projectDir)
                }
            }

            test("execute task '$targetTask'") {

                setup.getGradleTaskTester().runTasks(arrayOf("tiJson", targetTask))
                val result = setup.getGradleTaskTester().runTask(targetTask)

                println(result.tasks.joinToString(",\n") { "\"$it\"" })

                withClue("expected list of tasks executed with expected result\n${result.tasks.joinToString(",\n") { "\"$it\"" }}") {
                    val actualTaskTree = getTaskTree(File(setup.projectDir, "lib"), targetTask, result)
                        .chopChilds { it.value().path in arrayOf(":lib:bundleReleaseAar") }
                        .toStringTree()

                    actualTaskTree shouldBe setup.expectedTaskGraph
                }

                setup.mockServers.forEach { server ->
                    BintrayRepoResult(
                        server.collectRequests(),
                        listOf(coordinate),
                        "username",
                        "maven",
                        "aar"
                    ) should publishedToBintrayRepositoryCompletely()
                }
            }
        }

        context("with variant attached to version") {
            val coordinate = Coordinate(
                "test.group",
                "test.artifact",
                "0.1",
                versionWithVariant = "0.1-release"
            )
            val setup = commonSetup(coordinate, expectedTaskGraph)
            setup.writeFile(
                "${setup.subProjDirs[0]}/build.gradle",
                commonAndroidGradle(variantMode = "variantWithVersion()")
            )
            testBody(coordinate, setup)
        }

        context("with variant attached to artifactId") {
            val coordinate = Coordinate(
                "test.group",
                "test.artifact",
                "0.1",
                artifactIdWithVariant = "test.artifact-release"
            )
            val setup = commonSetup(coordinate, expectedTaskGraph)
            setup.writeFile(
                "${setup.subProjDirs[0]}/build.gradle",
                commonAndroidGradle(variantMode = "variantWithArtifactId()")
            )
            testBody(coordinate, setup)
        }
    }
})
