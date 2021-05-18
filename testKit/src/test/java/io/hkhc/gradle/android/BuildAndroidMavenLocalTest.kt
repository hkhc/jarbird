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

import io.hkhc.gradle.test.Coordinate
import io.hkhc.gradle.test.DefaultGradleProjectSetup
import io.hkhc.gradle.test.LocalRepoResult
import io.hkhc.gradle.test.commonAndroidExtGradle
import io.hkhc.gradle.test.commonAndroidRootGradle
import io.hkhc.gradle.test.getTaskTree
import io.hkhc.gradle.test.getTestAndroidSdkHomePair
import io.hkhc.gradle.test.printFileTree
import io.hkhc.gradle.test.mavenlocal.publishToMavenLocalCompletely
import io.hkhc.gradle.test.setupAndroidProperties
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

@Tags("Multi", "AAR", "MavenLocal", "Variant")
class BuildAndroidMavenLocalTest : FunSpec({

    context("Publish Android AAR in to Maven Local") {

        val targetTask = "jbPublishToMavenLocal"

        val releaseExpectedTaskGraph = stringTreeOf(NoBarTheme) {
            ":jbPublishToMavenLocal SUCCESS" {
                ":lib:jbPublishToMavenLocal SUCCESS" {
                    ":lib:jbPublishTestArtifactReleaseToMavenLocal SUCCESS" {
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
        }

        val snapshotExpectedTaskGraph = stringTreeOf(NoBarTheme) {
            ":jbPublishToMavenLocal SUCCESS" {
                ":lib:jbPublishToMavenLocal SUCCESS" {
                    ":lib:jbPublishTestArtifactReleaseToMavenLocal SUCCESS" {
                        ":lib:publishTestArtifactReleasePublicationToMavenLocal SUCCESS" {
                            ":lib:bundleReleaseAar SUCCESS"()
                            ":lib:generateMetadataFileForTestArtifactReleasePublication SUCCESS" {
                                ":lib:bundleReleaseAar SUCCESS"()
                            }
                            ":lib:generatePomFileForTestArtifactReleasePublication SUCCESS"()
                            ":lib:jbDokkaJarTestArtifactRelease SUCCESS" {
                                ":lib:jbDokkaHtmlTestArtifactRelease SUCCESS"()
                            }
                            ":lib:sourcesJarTestArtifactRelease SUCCESS"()
                        }
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
                setupGradleProperties {
                    setupAndroidProperties()
                }

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

                // Note: sub-projects build.gradle is specified in test case specific context

                writeFile(
                    "${subProjDirs[0]}/pom.yaml",
                    simplePom(coordinate, "release", "aar")
                )

                this.expectedTaskGraph = expectedTaskGraph
            }
        }

        suspend fun FunSpecContextScope.testBody(coordinate: Coordinate, setup: DefaultGradleProjectSetup) {

            afterTest {
                if (it.b.status == TestStatus.Error || it.b.status == TestStatus.Failure) {
                    printFileTree(setup.projectDir)
                }
            }

            test("execute task '$targetTask'") {

                setup.getGradleTaskTester().runTasks(arrayOf("tiJson", targetTask))
                val result = setup.getGradleTaskTester().runTask(targetTask)

                println(result.tasks.joinToString("\n") { "\"$it\"," })

                withClue("expected list of tasks executed with expected result") {
                    val actualTaskTree = getTaskTree(File(setup.projectDir, "lib"), targetTask, result)
                        .chopChilds { it.value().path in arrayOf(":lib:bundleReleaseAar") }
                        .toStringTree()

                    actualTaskTree shouldBe setup.expectedTaskGraph
                }

                LocalRepoResult(
                    setup.localRepoDirFile,
                    coordinate,
                    "aar"
                ) should publishToMavenLocalCompletely()
            }
        }

        context("with variant attached to version") {
            context("to release Mavel Local") {
                val coordinate = Coordinate(
                    "test.group",
                    "test.artifact",
                    "0.1",
                    versionWithVariant = "0.1-release"
                )
                val setup = commonSetup(coordinate, releaseExpectedTaskGraph)
                setup.writeFile(
                    "${setup.subProjDirs[0]}/build.gradle",
                    commonAndroidExtGradle(variantMode = "variantWithVersion()")
                )
                testBody(coordinate, setup)
            }
            context("to snapshot Mavel Local") {
                val coordinate = Coordinate(
                    "test.group",
                    "test.artifact",
                    "0.1-SNAPSHOT",
                    versionWithVariant = "0.1-release-SNAPSHOT"
                )
                val setup = commonSetup(coordinate, snapshotExpectedTaskGraph)
                setup.writeFile(
                    "${setup.subProjDirs[0]}/build.gradle",
                    commonAndroidExtGradle(variantMode = "variantWithVersion()")
                )
                testBody(coordinate, setup)
            }
        }

        context("with variant attached to artifactId") {
            val coordinate = Coordinate(
                "test.group",
                "test.artifact",
                "0.1",
                artifactIdWithVariant = "test.artifact-release"
            )
            val setup = commonSetup(coordinate, releaseExpectedTaskGraph)
            setup.writeFile(
                "${setup.subProjDirs[0]}/build.gradle",
                commonAndroidExtGradle(variantMode = "variantWithArtifactId()")
            )
            testBody(coordinate, setup)
        }
    }
})
