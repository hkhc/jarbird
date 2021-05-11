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
import io.hkhc.gradle.test.MavenRepoResult
import io.hkhc.gradle.test.MockMavenRepositoryServer
import io.hkhc.gradle.test.buildTwoGlobalGradleKts
import io.hkhc.gradle.test.buildTwoLocalGradleKts
import io.hkhc.gradle.test.getTaskTree
import io.hkhc.gradle.test.maven.publishedToMavenRepositoryCompletely
import io.hkhc.gradle.test.printFileTree
import io.hkhc.gradle.test.shouldBeNoDifference
import io.hkhc.gradle.test.simpleTwoPoms
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
import java.io.FileReader
import java.util.Properties

/**
 * snapshot / release
 * +- MavenLocal
 * +- MavenRepository
 * + Bintray
 * + artifactory
 * + Android AAR
 * + multivariant Android AAR
 *
 * + Multi-project
 *
 * + snapshot / release
 * plugin gradle plugin portal
 * + plugin mavenLocal
 * + plugin mavenrepository
 * + plugin bintray
 * - plugin artifactory
 *
 * all - mavenrepository
 * all - bintray
 *
 * gradle versions
 * signing v1 signing v2
 * groovy/kts script
 * alternate project name
 * credential with env variable
 * handle publishing rejection
 *
 * - (should fail) try to invoke disabled target (e.g. jbPublishToMavenRepository when maven = false)
 * - (should fail) build snapshot plugin to bintray
 * - support javadoc rather than dokka
 * - build multiple project with maven repository
 * - build multiple project with bintray
 * - build multiple project with artifactory
 * - build multiple artifacts with variant, maven repository
 * - build multiple artifacts with variant, bintray
 * - build multiple artifacts with variant, artifactory
 * - build multiple AAR with flavour, mavenLocal
 * - build multiple AAR with flavour, mavenRepository
 * - build multiple AAR with flavour, bintray
 * - build multiple AAR with flavour, artifactory
 * - build multiple AAR with buildtype, flavour, mavenLocal
 * - build multiple AAR with buildtype, flavour, mavenRepository
 * - build multiple AAR with buildtype, flavour, bintray
 * - build multiple AAR with buildtype, flavour, artifactory
 *
 *
 */
@Tags("Library", "MavenRepository")
class BuildTwoMavenRepoTest : FunSpec({

    context("Publish library") {

        val targetTask = "jbPublishToMavenRepository"

        fun commonSetup(coordinates: List<Coordinate>, expectedTaskGraph: Tree<String>): DefaultGradleProjectSetup {

            val projectDir = tempDirectory()

            return DefaultGradleProjectSetup(projectDir).apply {

                sourceSetTemplateDirs = arrayOf("functionalTestData/libTwin")
                setup()
                coordinates.forEach { coordinate ->
                    mockServers.add(
                        MockMavenRepositoryServer().apply {
                            setUp(listOf(coordinate), "/base")
                        }
                    )
                }

                writeFile("pom.yaml", simpleTwoPoms(coordinates, "lib1", "lib2"))

                setupGradleProperties {
                    mockServers.forEachIndexed { index, server ->
                        "repository.maven.mock${index + 1}.release" to server.getServerUrl()
                        "repository.maven.mock${index + 1}.snapshot" to "fake-url-that-is-not-going-to-work"
                        "repository.maven.mock${index + 1}.username" to "username"
                        "repository.maven.mock${index + 1}.password" to "password"
                        "repository.maven.mock${index + 1}.allowInsecureProtocol" to "true"
                    }
                }

                val prop = Properties()
                prop.load(FileReader("$projectDir/gradle.properties"))
                prop.list(System.out)

                this.expectedTaskGraph = expectedTaskGraph
            }
        }

        suspend fun FunSpecContextScope.testBody(coordinates: List<Coordinate>, setup: DefaultGradleProjectSetup) {
            afterTest {
                setup.mockServers.forEach { it.teardown() }
                if (it.b.status == TestStatus.Error || it.b.status == TestStatus.Failure) {
                    printFileTree(setup.projectDir)
                }
            }

            test("execute task '$targetTask'") {

                setup.getGradleTaskTester().runTasks(arrayOf("tiJson", targetTask))
                val result = setup.getGradleTaskTester().runTask(targetTask)

                withClue("expected graph of task executed with expected result task graph") {

                    val actualTaskTree = getTaskTree(setup.projectDir, targetTask, result)
                        .chopChilds { it.value().path == ":jar" }
                        .toStringTree()

                    actualTaskTree shouldBe setup.expectedTaskGraph
                }

                setup.mockServers.forEach { server ->
                    MavenRepoResult(
                        server.collectRequests(),
                        coordinates,
                        "jar"
                    ) should publishedToMavenRepositoryCompletely(withMetadata = false)
                }
            }
        }

        context("to release two global Maven Repositories") {

            val coordinates = listOf(
                Coordinate("test.group", "test.artifact1", "0.1"),
                Coordinate("test.group", "test.artifact2", "0.1")
            )
            val setup = commonSetup(
                coordinates,
                stringTreeOf(NoBarTheme) {
                    ":jbPublishToMavenRepository SUCCESS" {
                        ":jbPublishTestArtifact1Lib1ToMavenRepository SUCCESS" {
                            ":jbPublishTestArtifact1Lib1ToMavenMock1Repository SUCCESS" {
                                ":publishTestArtifact1Lib1PublicationToMavenMock1Repository SUCCESS" {
                                    ":generatePomFileForTestArtifact1Lib1Publication SUCCESS"()
                                    ":jbDokkaJarTestArtifact1Lib1 SUCCESS" {
                                        ":jbDokkaHtmlTestArtifact1Lib1 SUCCESS"()
                                    }
                                    ":signTestArtifact1Lib1Publication SUCCESS" {
                                        ":generatePomFileForTestArtifact1Lib1Publication SUCCESS"()
                                        ":jbDokkaJarTestArtifact1Lib1 SUCCESS" {
                                            ":jbDokkaHtmlTestArtifact1Lib1 SUCCESS"()
                                        }
                                        ":sourceSet1Jar SUCCESS"()
                                        ":sourcesJarTestArtifact1Lib1 SUCCESS"()
                                    }
                                    ":sourceSet1Jar SUCCESS"()
                                    ":sourcesJarTestArtifact1Lib1 SUCCESS"()
                                }
                            }
                            ":jbPublishTestArtifact1Lib1ToMavenMock2Repository SUCCESS" {
                                ":publishTestArtifact1Lib1PublicationToMavenMock2Repository SUCCESS" {
                                    ":generatePomFileForTestArtifact1Lib1Publication SUCCESS"()
                                    ":jbDokkaJarTestArtifact1Lib1 SUCCESS" {
                                        ":jbDokkaHtmlTestArtifact1Lib1 SUCCESS"()
                                    }
                                    ":signTestArtifact1Lib1Publication SUCCESS" {
                                        ":generatePomFileForTestArtifact1Lib1Publication SUCCESS"()
                                        ":jbDokkaJarTestArtifact1Lib1 SUCCESS" {
                                            ":jbDokkaHtmlTestArtifact1Lib1 SUCCESS"()
                                        }
                                        ":sourceSet1Jar SUCCESS"()
                                        ":sourcesJarTestArtifact1Lib1 SUCCESS"()
                                    }
                                    ":sourceSet1Jar SUCCESS"()
                                    ":sourcesJarTestArtifact1Lib1 SUCCESS"()
                                }
                            }
                        }
                        ":jbPublishTestArtifact2Lib2ToMavenRepository SUCCESS" {
                            ":jbPublishTestArtifact2Lib2ToMavenMock1Repository SUCCESS" {
                                ":publishTestArtifact2Lib2PublicationToMavenMock1Repository SUCCESS" {
                                    ":generatePomFileForTestArtifact2Lib2Publication SUCCESS"()
                                    ":jbDokkaJarTestArtifact2Lib2 SUCCESS" {
                                        ":jbDokkaHtmlTestArtifact2Lib2 SUCCESS"()
                                    }
                                    ":signTestArtifact2Lib2Publication SUCCESS" {
                                        ":generatePomFileForTestArtifact2Lib2Publication SUCCESS"()
                                        ":jbDokkaJarTestArtifact2Lib2 SUCCESS" {
                                            ":jbDokkaHtmlTestArtifact2Lib2 SUCCESS"()
                                        }
                                        ":sourceSet2Jar SUCCESS"()
                                        ":sourcesJarTestArtifact2Lib2 SUCCESS"()
                                    }
                                    ":sourceSet2Jar SUCCESS"()
                                    ":sourcesJarTestArtifact2Lib2 SUCCESS"()
                                }
                            }
                            ":jbPublishTestArtifact2Lib2ToMavenMock2Repository SUCCESS" {
                                ":publishTestArtifact2Lib2PublicationToMavenMock2Repository SUCCESS" {
                                    ":generatePomFileForTestArtifact2Lib2Publication SUCCESS"()
                                    ":jbDokkaJarTestArtifact2Lib2 SUCCESS" {
                                        ":jbDokkaHtmlTestArtifact2Lib2 SUCCESS"()
                                    }
                                    ":signTestArtifact2Lib2Publication SUCCESS" {
                                        ":generatePomFileForTestArtifact2Lib2Publication SUCCESS"()
                                        ":jbDokkaJarTestArtifact2Lib2 SUCCESS" {
                                            ":jbDokkaHtmlTestArtifact2Lib2 SUCCESS"()
                                        }
                                        ":sourceSet2Jar SUCCESS"()
                                        ":sourcesJarTestArtifact2Lib2 SUCCESS"()
                                    }
                                    ":sourceSet2Jar SUCCESS"()
                                    ":sourcesJarTestArtifact2Lib2 SUCCESS"()
                                }
                            }
                        }
                    }
                }

            )

            setup.writeFile("build.gradle.kts", buildTwoGlobalGradleKts())

            afterTest {
                setup.mockServers.forEach { it.teardown() }
                if (it.b.status == TestStatus.Error || it.b.status == TestStatus.Failure) {
                    printFileTree(setup.projectDir)
                }
            }

            test("execute task '$targetTask'") {

                setup.getGradleTaskTester().runTasks(arrayOf("tiJson", targetTask))
                val result = setup.getGradleTaskTester().runTask(targetTask)

                withClue("expected graph of task executed with expected result task graph") {

                    val actualTaskTree = getTaskTree(setup.projectDir, targetTask, result)
                        .chopChilds { it.value().path in arrayOf(":sourceSet1Jar", ":sourceSet2Jar") }
                        .toStringTree()

                    actualTaskTree shouldBe setup.expectedTaskGraph
                }

                setup.mockServers.forEach { server ->
                    MavenRepoResult(
                        server.collectRequests(),
                        coordinates,
                        "jar"
                    ) should publishedToMavenRepositoryCompletely(withMetadata = false)
                }
            }
        }

        context("to release two local Maven Repository") {

            val coordinates = listOf(
                Coordinate("test.group", "test.artifact1", "0.1"),
                Coordinate("test.group", "test.artifact2", "0.1")
            )
            val setup = commonSetup(
                coordinates,
                stringTreeOf(NoBarTheme) {
                    ":jbPublishToMavenRepository SUCCESS" {
                        ":jbPublishTestArtifact1Lib1ToMavenRepository SUCCESS" {
                            ":jbPublishTestArtifact1Lib1ToMavenMock1Repository SUCCESS" {
                                ":publishTestArtifact1Lib1PublicationToMavenMock1Repository SUCCESS" {
                                    ":generatePomFileForTestArtifact1Lib1Publication SUCCESS"()
                                    ":jbDokkaJarTestArtifact1Lib1 SUCCESS" {
                                        ":jbDokkaHtmlTestArtifact1Lib1 SUCCESS"()
                                    }
                                    ":signTestArtifact1Lib1Publication SUCCESS" {
                                        ":generatePomFileForTestArtifact1Lib1Publication SUCCESS"()
                                        ":jbDokkaJarTestArtifact1Lib1 SUCCESS" {
                                            ":jbDokkaHtmlTestArtifact1Lib1 SUCCESS"()
                                        }
                                        ":sourceSet1Jar SUCCESS"()
                                        ":sourcesJarTestArtifact1Lib1 SUCCESS"()
                                    }
                                    ":sourceSet1Jar SUCCESS"()
                                    ":sourcesJarTestArtifact1Lib1 SUCCESS"()
                                }
                            }
                        }
                        ":jbPublishTestArtifact2Lib2ToMavenRepository SUCCESS" {
                            ":jbPublishTestArtifact2Lib2ToMavenMock2Repository SUCCESS" {
                                ":publishTestArtifact2Lib2PublicationToMavenMock2Repository SUCCESS" {
                                    ":generatePomFileForTestArtifact2Lib2Publication SUCCESS"()
                                    ":jbDokkaJarTestArtifact2Lib2 SUCCESS" {
                                        ":jbDokkaHtmlTestArtifact2Lib2 SUCCESS"()
                                    }
                                    ":signTestArtifact2Lib2Publication SUCCESS" {
                                        ":generatePomFileForTestArtifact2Lib2Publication SUCCESS"()
                                        ":jbDokkaJarTestArtifact2Lib2 SUCCESS" {
                                            ":jbDokkaHtmlTestArtifact2Lib2 SUCCESS"()
                                        }
                                        ":sourceSet2Jar SUCCESS"()
                                        ":sourcesJarTestArtifact2Lib2 SUCCESS"()
                                    }
                                    ":sourceSet2Jar SUCCESS"()
                                    ":sourcesJarTestArtifact2Lib2 SUCCESS"()
                                }
                            }
                        }
                    }

                }

            )

            setup.writeFile("build.gradle.kts", buildTwoLocalGradleKts())

            afterTest {
                setup.mockServers.forEach { it.teardown() }
                if (it.b.status == TestStatus.Error || it.b.status == TestStatus.Failure) {
                    printFileTree(setup.projectDir)
                }
            }

            test("execute task '$targetTask'") {

                setup.getGradleTaskTester().runTasks(arrayOf("tiJson", targetTask))
                val result = setup.getGradleTaskTester().runTask(targetTask)

                withClue("expected graph of task executed with expected result task graph") {

                    val actualTaskTree = getTaskTree(setup.projectDir, targetTask, result)
                        .chopChilds { it.value().path in arrayOf(":sourceSet1Jar", ":sourceSet2Jar") }
                        .toStringTree()

                    actualTaskTree shouldBe setup.expectedTaskGraph
                }

                setup.mockServers.zip(coordinates).forEach { (server, coordinate) ->
                    MavenRepoResult(
                        server.collectRequests(),
                        listOf(coordinate),
                        "jar"
                    ) should publishedToMavenRepositoryCompletely(withMetadata = false)
                }
            }
        }
    }
})
