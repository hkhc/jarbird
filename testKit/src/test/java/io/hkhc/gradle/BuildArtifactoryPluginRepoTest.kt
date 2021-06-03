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

import io.hkhc.gradle.test.ArtifactoryRepoResult
import io.hkhc.gradle.test.Coordinate
import io.hkhc.gradle.test.DefaultGradleProjectSetup
import io.hkhc.gradle.test.artifacory.MockArtifactoryRepositoryServer
import io.hkhc.gradle.test.artifacory.publishedToArtifactoryRepositoryCompletely
import io.hkhc.gradle.test.buildGradleCustomArtifactoryKts
import io.hkhc.gradle.test.getTaskTree
import io.hkhc.gradle.test.pluginPom
import io.hkhc.gradle.test.printFileTree
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

@Tags("Plugin", "Bintray")
class BuildArtifactoryPluginRepoTest : FunSpec({

    context("Publish Gradle plugin") {

        val targetTask = "jbPublishToArtifactory"

        fun commonSetup(coordinate: Coordinate, expectedTaskGraph: Tree<String>): DefaultGradleProjectSetup {

            val projectDir = tempDirectory()

            return DefaultGradleProjectSetup(projectDir).apply {

                setup()
                mockServers.add(
                    MockArtifactoryRepositoryServer().apply {
                        setUp(listOf(coordinate), "/base")
                    }
                )

                writeFile("build.gradle.kts", buildGradleCustomArtifactoryKts())

                writeFile(
                    "pom.yaml",
                    simplePom(coordinate) + '\n' +
                        pluginPom(
                            coordinate.pluginId ?: "non-exist-plugin-id",
                            "TestPluginClass"
                        )
                )

                setupGradleProperties {
                    "repository.artifactory.mock.release" to mockServers[0].getServerUrl()
                    "repository.artifactory.mock.snapshot" to mockServers[0].getServerUrl()
                    "repository.artifactory.mock.username" to "username"
                    "repository.artifactory.mock.apikey" to "password"
                    "repository.artifactory.mock.repoKey" to "oss-snapshot-local"
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

                val pluginPom = File(
                    setup.projectDir,
                    "build/publications/testArtifactPluginMarkerMaven/pom-default.xml"
                ).readText()
                pluginPom shouldBe pluginPom(coordinate)

                setup.mockServers[0].let { server ->
                    ArtifactoryRepoResult(
                        server.collectRequests(),
                        coordinate,
                        "username",
                        "oss-snapshot-local",
                        "jar"
                    ) should publishedToArtifactoryRepositoryCompletely()
                }
            }
        }

        context("to release Artifactory Repository") {

            val coordinate = Coordinate("test.group", "test.artifact", "0.1", "test.plugin")
            val setup = commonSetup(
                coordinate,
                stringTreeOf(NoBarTheme) {
                    ":jbPublishToArtifactory SUCCESS" {
                        ":artifactoryPublish SUCCESS" {
                            ":generateMetadataFileForTestArtifactPublication SUCCESS" {
                                ":jar SUCCESS"()
                            }
                            ":generatePomFileForTestArtifactPluginMarkerMavenPublication SUCCESS"()
                            ":generatePomFileForTestArtifactPublication SUCCESS"()
                            ":jar SUCCESS"()
                            ":jbDokkaJarTestArtifact SUCCESS" {
                                ":jbDokkaHtmlTestArtifact SUCCESS"()
                            }
                            ":signTestArtifactPluginMarkerMavenPublication SUCCESS" {
                                ":generatePomFileForTestArtifactPluginMarkerMavenPublication SUCCESS"()
                            }
                            ":signTestArtifactPublication SUCCESS" {
                                ":generateMetadataFileForTestArtifactPublication SUCCESS" {
                                    ":jar SUCCESS"()
                                }
                                ":generatePomFileForTestArtifactPublication SUCCESS"()
                                ":jar SUCCESS"()
                                ":jbDokkaJarTestArtifact SUCCESS" {
                                    ":jbDokkaHtmlTestArtifact SUCCESS"()
                                }
                                ":sourcesJarTestArtifact SUCCESS"()
                            }
                            ":sourcesJarTestArtifact SUCCESS"()
                            ":artifactoryDeploy SUCCESS" {
                                ":extractModuleInfo SUCCESS"()
                            }
                        }
                    }
                }
            )

            testBody(coordinate, setup)
        }
    }
})
