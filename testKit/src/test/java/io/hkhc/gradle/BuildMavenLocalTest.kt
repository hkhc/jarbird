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

import io.hkhc.gradle.test.Coordinate
import io.hkhc.gradle.test.DefaultGradleProjectSetup
import io.hkhc.gradle.test.LocalRepoResult
import io.hkhc.gradle.test.buildGradle
import io.hkhc.gradle.test.getTaskTree
import io.hkhc.gradle.test.printFileTree
import io.hkhc.gradle.test.mavenlocal.publishToMavenLocalCompletely
import io.hkhc.gradle.test.simplePom
import io.hkhc.test.utils.test.tempDirectory
import io.hkhc.utils.tree.NoBarTheme
import io.hkhc.utils.tree.chopChilds
import io.hkhc.utils.tree.stringTreeOf
import io.hkhc.utils.tree.toStringTree
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestStatus
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe

// @Suppress("unused")
// @Tags("Library", "MavenLocal")
class BuildMavenLocalTest : FunSpec({

    context("Publish library to Maven local") {

        val coordinate = Coordinate("test.group", "test.artifact", "0.1")
        val targetTask = "jbPublishToMavenLocal"
        val projectDir = tempDirectory()
        lateinit var setup: DefaultGradleProjectSetup

        /**
         * Setup
         * - source sets
         * - build.gradle
         * - pom.yaml
         * - gradle.properties
         */
        beforeTest {

            setup = DefaultGradleProjectSetup(projectDir).apply {
                sourceSetTemplateDirs = arrayOf("functionalTestData/libJavaKotlin")
                setup()
            }

            setup.writeFile("build.gradle", buildGradle())

            setup.writeFile("pom.yaml", simplePom(coordinate))

            setup.setupGradleProperties()
        }

        afterTest {
            if (it.b.status == TestStatus.Error || it.b.status == TestStatus.Failure) {
                printFileTree(setup.projectDir)
            }
        }

        test("execute task '$targetTask'") {

            setup.getGradleTaskTester().runTasks(arrayOf("tiJson", targetTask))
            val result = setup.getGradleTaskTester().runTask(targetTask)

            withClue("expected graph of task executed with expected result task graph") {

                val actualTaskTree = getTaskTree(projectDir, targetTask, result)
                    .chopChilds { it.value().path == ":jar" }
                    .toStringTree()

                actualTaskTree shouldBe
                stringTreeOf(NoBarTheme) {
                    ":jbPublishToMavenLocal SUCCESS" {
                        ":jbPublishTestArtifactToMavenLocal SUCCESS" {
                            ":publishTestArtifactPublicationToMavenLocal SUCCESS" {
                                ":generateMetadataFileForTestArtifactPublication SUCCESS" {
                                    ":jar SUCCESS"()
                                }
                                ":generatePomFileForTestArtifactPublication SUCCESS"()
                                ":jar SUCCESS"()
                                ":jbDokkaJarTestArtifact SUCCESS" {
                                    ":jbDokkaHtmlTestArtifact SUCCESS"()
                                }
                                ":signTestArtifactPublication SUCCESS" {
                                    ":generateMetadataFileForTestArtifactPublication SUCCESS" {
                                        ":jar SUCCESS"()
                                    }
                                    ":generatePomFileForTestArtifactPublication SUCCESS" {

                                    }
                                    ":jar SUCCESS"()
                                    ":jbDokkaJarTestArtifact SUCCESS" {
                                        ":jbDokkaHtmlTestArtifact SUCCESS"()
                                    }
                                    ":sourcesJarTestArtifact SUCCESS"()
                                }
                                ":sourcesJarTestArtifact SUCCESS"()
                            }
                        }
                    }
                }
            }

            LocalRepoResult(setup.localRepoDirFile, coordinate, "jar") should publishToMavenLocalCompletely()
        }
    }
})
