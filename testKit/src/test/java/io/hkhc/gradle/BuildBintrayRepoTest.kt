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

import io.hkhc.gradle.test.BintrayRepoResult
import io.hkhc.gradle.test.Coordinate
import io.hkhc.gradle.test.DefaultGradleProjectSetup
import io.hkhc.gradle.test.bintray.MockBintrayRepositoryServer
import io.hkhc.gradle.test.bintray.publishedToBintrayRepositoryCompletely
import io.hkhc.gradle.test.buildGradleCustomBintrayKts
import io.hkhc.gradle.test.getTaskTree
import io.hkhc.gradle.test.printFileTree
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

@Tags("Bintray", "Library")
class BuildBintrayRepoTest : FunSpec({

    context("Publish library") {

        val targetTask = "jbPublishToBintray"

        fun commonSetup(coordinate: Coordinate, expectedTaskGraph: Tree<String>): DefaultGradleProjectSetup {

            val projectDir = tempDirectory()

            return DefaultGradleProjectSetup(projectDir).apply {

                setup()
                mockServers.add(
                    MockBintrayRepositoryServer().apply {
                        setUp(listOf(coordinate), "/base")
                    }
                )

                writeFile("build.gradle.kts", buildGradleCustomBintrayKts())

                writeFile("pom.yaml", simplePom(coordinate))

                setupGradleProperties {
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

                withClue("expected list of tasks executed with expected result") {
                    val actualTaskTree = getTaskTree(setup.projectDir, targetTask, result)
                        .chopChilds { it.value().path == ":jar" }
                        .toStringTree()

                    actualTaskTree shouldBe setup.expectedTaskGraph
                }

                setup.mockServers[0].let { server ->
                    BintrayRepoResult(
                        server.collectRequests(),
                        listOf(coordinate),
                        "username",
                        "maven",
                        "jar"
                    ) should publishedToBintrayRepositoryCompletely()
                }
            }
        }

        context("to release Bintray Repository") {

            val coordinate = Coordinate("test.group", "test.artifact", "0.1")
            val setup = commonSetup(
                coordinate,
                stringTreeOf(NoBarTheme) {
                    ":jbPublishToBintray SUCCESS" {
                        ":bintrayUpload SUCCESS" {
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
                                    ":generatePomFileForTestArtifactPublication SUCCESS" ()
                                    ":jar SUCCESS"()
                                    ":jbDokkaJarTestArtifact SUCCESS" {
                                        ":jbDokkaHtmlTestArtifact SUCCESS"()
                                    }
                                    ":sourcesJarTestArtifact SUCCESS"()
                                }
                                ":sourcesJarTestArtifact SUCCESS"()
                            }
                            ":bintrayPublish SUCCESS"()
                        }
                    }
                }
            )

            testBody(coordinate, setup)
        }
    }
})
