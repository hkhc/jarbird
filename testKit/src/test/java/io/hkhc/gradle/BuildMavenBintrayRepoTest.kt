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
import io.hkhc.gradle.test.ArtifactoryRepoResult
import io.hkhc.gradle.test.BintrayRepoResult
import io.hkhc.gradle.test.Coordinate
import io.hkhc.gradle.test.DefaultGradleProjectSetup
import io.hkhc.gradle.test.LocalRepoResult
import io.hkhc.gradle.test.MavenRepoResult
import io.hkhc.gradle.test.MockMavenRepositoryServer
import io.hkhc.gradle.test.artifacory.MockArtifactoryRepositoryServer
import io.hkhc.gradle.test.artifacory.publishedToArtifactoryRepositoryCompletely
import io.hkhc.gradle.test.bintray.MockBintrayRepositoryServer
import io.hkhc.gradle.test.bintray.publishedToBintrayRepositoryCompletely
import io.hkhc.gradle.test.buildGradleKts
import io.hkhc.gradle.test.getTaskTree
import io.hkhc.gradle.test.maven.publishedToMavenRepositoryCompletely
import io.hkhc.gradle.test.printFileTree
import io.hkhc.gradle.test.publishToMavenLocalCompletely
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

@Tags("Library", "MavenRepository", "Bintray", "Artifactory")
class BuildMavenBintrayRepoTest : FunSpec({

    context("Publish library") {

        val targetTask = "jbPublish"

        fun commonSetup(
            coordinate: Coordinate,
            maven: Boolean = true,
            bintray: Boolean = true,
            expectedTaskGraph: Tree<String>
        ): DefaultGradleProjectSetup {

            val projectDir = tempDirectory()

            return DefaultGradleProjectSetup(projectDir).apply {

                setup()
                if (maven) {
                    mavenMockServer = MockMavenRepositoryServer().apply {
                        setUp(listOf(coordinate), "/base")
                    }
                }
                if (bintray) {
                    bintrayMockServer = MockBintrayRepositoryServer().apply {
                        setUp(listOf(coordinate), "/base")
                    }
                    artifactoryMockServer = MockArtifactoryRepositoryServer().apply {
                        setUp(listOf(coordinate), "/base")
                    }
                }

                writeFile("build.gradle.kts", buildGradleKts(maven, bintray))

                writeFile("pom.yaml", simplePom(coordinate))

                setupGradleProperties {
                    if (maven) {
                        if (coordinate.version.isSnapshot()) {
                            "repository.maven.mock.release" to "fake-url-that-is-not-going-to-work"
                            "repository.maven.mock.snapshot" to mavenMockServer?.getServerUrl()
                        } else {
                            "repository.maven.mock.release" to mavenMockServer?.getServerUrl()
                            "repository.maven.mock.snapshot" to "fake-url-that-is-not-going-to-work"
                        }
                        "repository.maven.mock.username" to "username"
                        "repository.maven.mock.password" to "password"
                        "repository.maven.mock.allowInsecureProtocol" to "true"
                    }

                    if (bintray) {
                        if (coordinate.version.isSnapshot()) {
                            "repository.bintray.release" to "fake-url-that-is-not-going-to-work"
                            "repository.bintray.snapshot" to artifactoryMockServer?.getServerUrl()
                        } else {
                            "repository.bintray.release" to bintrayMockServer?.getServerUrl()
                            "repository.bintray.snapshot" to "fake-url-that-is-not-going-to-work"
                        }
                        "repository.bintray.username" to "username"
                        "repository.bintray.apikey" to "password"
                    }
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

                LocalRepoResult(setup.localRepoDirFile, coordinate, "jar") should
                    publishToMavenLocalCompletely()

                setup.mavenMockServer?.let { server ->
                    MavenRepoResult(
                        server.collectRequests(),
                        listOf(coordinate),
                        "jar"
                    ) should publishedToMavenRepositoryCompletely()
                }

                if (coordinate.version.isSnapshot()) {
                    setup.artifactoryMockServer?.let { server ->
                        ArtifactoryRepoResult(
                            server.collectRequests(),
                            coordinate,
                            "username",
                            "oss-snapshot-local",
                            "jar"
                        ) should publishedToArtifactoryRepositoryCompletely()
                    }
                } else {
                    setup.bintrayMockServer?.let { server ->
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
        }

        context("to release Maven Repository and Bintray Repository") {

            val coordinate = Coordinate("test.group", "test.artifact", "0.1")
            val setup = commonSetup(
                coordinate,
                maven = true,
                bintray = true,
                expectedTaskGraph = stringTreeOf(NoBarTheme) {
                    ":jbPublish SUCCESS" {
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
                        ":jbPublishToMavenRepository SUCCESS" {
                            ":jbPublishTestArtifactToMavenRepository SUCCESS" {
                                ":jbPublishTestArtifactToMavenMockRepository SUCCESS" {
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
                                            ":generatePomFileForTestArtifactPublication SUCCESS" ()
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
                }
            )

            testBody(coordinate, setup)
        }

        context("to snapshot Maven Repository and Artifactory Repository") {

            val coordinate = Coordinate("test.group", "test.artifact", "0.1-SNAPSHOT")
            val setup = commonSetup(
                coordinate,
                maven = true,
                bintray = true,
                expectedTaskGraph =
                stringTreeOf(NoBarTheme) {
                    ":jbPublish SUCCESS" {
                        ":jbPublishToBintray SUCCESS" {
                            ":jbPublishToArtifactory SUCCESS" {
                                ":artifactoryPublish SUCCESS" {
                                    ":generateMetadataFileForTestArtifactPublication SUCCESS" {
                                        ":jar SUCCESS"()
                                    }
                                    ":generatePomFileForTestArtifactPublication SUCCESS"()
                                    ":jar SUCCESS"()
                                    ":jbDokkaJarTestArtifact SUCCESS" {
                                        ":jbDokkaHtmlTestArtifact SUCCESS"()
                                    }
                                    ":sourcesJarTestArtifact SUCCESS"()
                                    ":artifactoryDeploy SUCCESS" {
                                        ":extractModuleInfo SUCCESS"()
                                    }
                                }
                            }
                        }
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
                                    ":sourcesJarTestArtifact SUCCESS"()
                                }
                            }
                        }
                        ":jbPublishToMavenRepository SUCCESS" {
                            ":jbPublishTestArtifactToMavenRepository SUCCESS" {
                                ":jbPublishTestArtifactToMavenMockRepository SUCCESS" {
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
                }
            )

            testBody(coordinate, setup)
        }

        context("to snapshot Maven Repository only") {

            val coordinate = Coordinate("test.group", "test.artifact", "0.1")
            val setup = commonSetup(
                coordinate,
                maven = true,
                bintray = false,
                expectedTaskGraph =
                stringTreeOf(NoBarTheme) {
                    ":jbPublish SUCCESS" {
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
                        ":jbPublishToMavenRepository SUCCESS" {
                            ":jbPublishTestArtifactToMavenRepository SUCCESS" {
                                ":jbPublishTestArtifactToMavenMockRepository SUCCESS" {
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
                                            ":generatePomFileForTestArtifactPublication SUCCESS" ()
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
                }
            )

            testBody(coordinate, setup)
        }

        context("to snapshot Bintray Repository only") {

            val coordinate = Coordinate("test.group", "test.artifact", "0.1")
            val setup = commonSetup(
                coordinate,
                maven = false,
                bintray = true,
                expectedTaskGraph =
                stringTreeOf(NoBarTheme) {
                    ":jbPublish SUCCESS" {
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
            )

            testBody(coordinate, setup)
        }
    }
})
