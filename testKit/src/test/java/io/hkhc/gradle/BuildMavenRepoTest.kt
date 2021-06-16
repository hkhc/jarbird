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
import io.hkhc.gradle.test.buildGradleKts
import io.hkhc.gradle.test.getTaskTree
import io.hkhc.gradle.test.maven.publishedToMavenRepositoryCompletely
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
import io.kotest.core.spec.style.scopes.FunSpecContainerContext
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
 * - (should fail) try to invoke disabled target (e.g. jbPublishToMavenRepositories when maven = false)
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
class BuildMavenRepoTest : FunSpec({

    context("Publish library") {

        val targetTask = "jbPublishToMavenRepositories"

        fun commonSetup(coordinate: Coordinate, expectedTaskGraph: Tree<String>): DefaultGradleProjectSetup {

            val projectDir = tempDirectory()

            return DefaultGradleProjectSetup(projectDir).apply {

                setup()
                mockServers.add(
                    MockMavenRepositoryServer().apply {
                        setUp(listOf(coordinate), "/base")
                    }
                )

                writeFile("build.gradle.kts", buildGradleKts(artifactory = false))

                writeFile("pom.yaml", simplePom(coordinate))

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

                val prop = Properties()
                prop.load(FileReader("$projectDir/gradle.properties"))
                prop.list(System.out)

                this.expectedTaskGraph = expectedTaskGraph
            }
        }

        suspend fun FunSpecContainerContext.testBody(coordinate: Coordinate, setup: DefaultGradleProjectSetup) {
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
                        listOf(coordinate),
                        "jar"
                    ) should publishedToMavenRepositoryCompletely()
                }
            }
        }

        context("to release Maven Repository") {

            val coordinate = Coordinate("test.group", "test.artifact", "0.1")
            val setup = commonSetup(
                coordinate,
                stringTreeOf(NoBarTheme) {
                    ":jbPublishToMavenRepositories SUCCESS" {
                        ":jbPublishToMavenMock SUCCESS" {
                            ":jbPublishTestArtifactToMavenMock SUCCESS" {
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
            )

            testBody(coordinate, setup)
        }

        context("to snapshot Maven Repository") {

            val coordinate = Coordinate("test.group", "test.artifact", "0.1-SNAPSHOT")
            val setup = commonSetup(
                coordinate,
                stringTreeOf {
                    ":jbPublishToMavenRepositories SUCCESS" {
                        ":jbPublishToMavenMock SUCCESS" {
                            ":jbPublishTestArtifactToMavenMock SUCCESS" {
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
