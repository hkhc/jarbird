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

import io.hkhc.gradle.pom.internal.isSnapshot
import io.hkhc.gradle.test.Coordinate
import io.hkhc.gradle.test.DefaultGradleProjectSetup
import io.hkhc.gradle.test.MavenRepoResult
import io.hkhc.gradle.test.MockMavenRepositoryServer
import io.hkhc.gradle.test.buildGradlePluginKts
import io.hkhc.gradle.test.getTaskTree
import io.hkhc.gradle.test.maven.publishedPluginToMavenRepositoryCompletely
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

/**
 * snapshot / release
 * - MavenLocal
 * - MavenRepository
 * - Bintray
 * - artifactory
 * - Android AAR
 * - multivariant Android AAR
 *
 * Multi-project
 *
 * snapshot / release
 * plugin gradle plugin portal
 * plugin mavenLocal
 * plugin mavenrepository
 * plugin bintray
 * plugin artifactory
 *
 * all - mavenrepository
 * all - bintray
 *
 * gradle versions
 * signing v1 signing v2
 * groovy/kts script
 * alternate project name
 *
 *
 */
@Tags("Plugin", "MavenRepository")
class BuildMavenPluginRepoTest : FunSpec({

    context("Publish Gradle plugin") {

        val targetTask = "jbPublishToMavenRepositories"

        fun commonSetup(coordinate: Coordinate, expectedTaskGraph: Tree<String>): DefaultGradleProjectSetup {

            val projectDir = tempDirectory()

            return DefaultGradleProjectSetup(projectDir).apply {

                this.sourceSetTemplateDirs = arrayOf("functionalTestData/plugin/src")
                setup()
                mockServers.add(
                    MockMavenRepositoryServer().apply {
                        setUp(listOf(coordinate), "/base")
                    }
                )

                writeFile("build.gradle.kts", buildGradlePluginKts())

                writeFile(
                    "pom.yaml",
                    simplePom(coordinate) + '\n' +
                        pluginPom(
                            coordinate.pluginId ?: "non-exist-plugin-id",
                            "TestPlugin"
                        )
                )

                setupGradleProperties {
                    if (coordinate.version.isSnapshot()) {
                        "repository.maven.mock.release" to "fake-url-that-is-not-going-to-work"
                        "repository.maven.mock.snapshot" to mockServers[0].getServerUrl()
                    } else {
                        "repository.maven.mock.release" to mockServers[0].getServerUrl()
                        "repository.maven.mock.snapshot" to "fake-url-that-is-not-going-to-work"
                    }
                    "repository.maven.mock.username" to "username"
                    "repository.maven.mock.password" to "password"
                    "repository.maven.mock.allowInsecureProtocol" to "true"
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

                setup.mockServers.forEach { server ->
                    MavenRepoResult(
                        server.collectRequests(),
                        listOf(coordinate),
                        "jar"
                    ) should publishedPluginToMavenRepositoryCompletely()
                }
            }
        }

        context("to release Maven Repository") {

            val coordinate = Coordinate("test.group", "test.artifact", "0.1", "test.plugin")
            val setup = commonSetup(
                coordinate,
                stringTreeOf(NoBarTheme) {
                    ":jbPublishToMavenRepositories SUCCESS" {
                        ":jbPublishTestArtifactToMavenRepositories SUCCESS" {
                            ":jbPublishTestArtifactToMavenMock SUCCESS" {
                                ":publishTestArtifactPluginMarkerMavenPublicationToMavenMockRepository SUCCESS" {
                                    ":generatePomFileForTestArtifactPluginMarkerMavenPublication SUCCESS"()
                                    ":signTestArtifactPluginMarkerMavenPublication SUCCESS" {
                                        ":generatePomFileForTestArtifactPluginMarkerMavenPublication SUCCESS"()
                                    }
                                }
                                ":publishTestArtifactPublicationToMavenMockRepository SUCCESS" {
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
                                        ":generatePomFileForTestArtifactPublication SUCCESS"()
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
            )

            testBody(coordinate, setup)
        }

        context("to snapshot Maven Repository") {

            val coordinate = Coordinate("test.group", "test.artifact", "0.1-SNAPSHOT", "test.plugin")
            val setup = commonSetup(
                coordinate,
                stringTreeOf(NoBarTheme) {
                    ":jbPublishToMavenRepositories SUCCESS" {
                        ":jbPublishTestArtifactToMavenRepositories SUCCESS" {
                            ":jbPublishTestArtifactToMavenMock SUCCESS" {
                                ":publishTestArtifactPluginMarkerMavenPublicationToMavenMockRepository SUCCESS" {
                                    ":generatePomFileForTestArtifactPluginMarkerMavenPublication SUCCESS"()
                                }
                                ":publishTestArtifactPublicationToMavenMockRepository SUCCESS" {
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
                            }
                        }
                    }
                }
            )

            testBody(coordinate, setup)
        }
    }
})
