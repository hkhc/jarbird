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
import io.hkhc.gradle.test.buildTwoPubGradleKts
import io.hkhc.gradle.test.getTaskTree
import io.hkhc.gradle.test.printFileTree
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
class BuildBintrayTwoPubRepoTest : FunSpec({

    context("Publish library") {

        val targetTask = "jbPublishToBintray"

        fun commonSetup(coordinates: List<Coordinate>, expectedTaskGraph: Tree<String>): DefaultGradleProjectSetup {

            val projectDir = tempDirectory()

            return DefaultGradleProjectSetup(projectDir).apply {

                setup()

                mockServers.add(
                    MockBintrayRepositoryServer().apply {
                        setUp(coordinates, "/base")
                    }
                )

                writeFile("pom.yaml", simpleTwoPoms(coordinates, "lib1", "lib2"))

                setupGradleProperties {
                    mockServers.forEachIndexed { index, server ->
                        "repository.bintray.release" to mockServers[0].getServerUrl()
                        "repository.bintray.username" to "username"
                        "repository.bintray.apikey" to "password"
                    }
                }

                val prop = Properties()
                prop.load(FileReader("$projectDir/gradle.properties"))
                prop.list(System.out)

                this.expectedTaskGraph = expectedTaskGraph
            }
        }

//        suspend fun FunSpecContextScope.testBody(coordinates: List<Coordinate>, setup: DefaultGradleProjectSetup) {
//            afterTest {
//                setup.mockServers.forEach { it.teardown() }
//                if (it.b.status == TestStatus.Error || it.b.status == TestStatus.Failure) {
//                    FileTree().dump(setup.projectDir, System.out::println)
//                }
//            }
//
//            test("execute task '$targetTask'") {
//
//                val result = setup.getGradleTaskTester().runTask(targetTask)
//
// //                withClue("expected list of tasks executed with expected result") {
// //                    result.tasks.map { it.toString() } shouldBeNoDifference setup.expectedTaskList
// //                }
//
//                setup.mockServers.forEach { server ->
//                    MavenRepoResult(
//                        server.collectRequests(),
//                        coordinates,
//                        "jar"
//                    ) should publishedToMavenRepositoryCompletely()
//                }
//            }
//        }

        context("to release two pubs to Bintray Repositories") {

            val coordinates = listOf(
                Coordinate("test.group", "test.artifact1", "0.1"),
                Coordinate("test.group", "test.artifact2", "0.1")
            )
            val setup = commonSetup(
                coordinates,
                stringTreeOf(NoBarTheme) {
                    ":jbPublishToBintray SUCCESS" {
                        ":bintrayUpload SUCCESS" {
                            ":publishTestArtifact1Lib1PublicationToMavenLocal SUCCESS" {
                                ":generatePomFileForTestArtifact1Lib1Publication SUCCESS"()
                                ":jar SUCCESS"()
                                ":jbDokkaJarTestArtifact1Lib1 SUCCESS" {
                                    ":jbDokkaHtmlTestArtifact1Lib1 SUCCESS"()
                                }
                                ":signTestArtifact1Lib1Publication SUCCESS" {
                                    ":generatePomFileForTestArtifact1Lib1Publication SUCCESS" ()
                                    ":jar SUCCESS"()
                                    ":jbDokkaJarTestArtifact1Lib1 SUCCESS" {
                                        ":jbDokkaHtmlTestArtifact1Lib1 SUCCESS"()
                                    }
                                    ":sourcesJarTestArtifact1Lib1 SUCCESS"()
                                }
                                ":sourcesJarTestArtifact1Lib1 SUCCESS"()
                            }
                            ":publishTestArtifact2Lib2PublicationToMavenLocal SUCCESS" {
                                ":generatePomFileForTestArtifact2Lib2Publication SUCCESS"()
                                ":jbDokkaJarTestArtifact2Lib2 SUCCESS" {
                                    ":jbDokkaHtmlTestArtifact2Lib2 SUCCESS"()
                                }
                                ":main2Jar SUCCESS"()
                                ":signTestArtifact2Lib2Publication SUCCESS" {
                                    ":generatePomFileForTestArtifact2Lib2Publication SUCCESS" ()
                                    ":jbDokkaJarTestArtifact2Lib2 SUCCESS" {
                                        ":jbDokkaHtmlTestArtifact2Lib2 SUCCESS"()
                                    }
                                    ":main2Jar SUCCESS"()
                                    ":sourcesJarTestArtifact2Lib2 SUCCESS"()
                                }
                                ":sourcesJarTestArtifact2Lib2 SUCCESS"()
                            }
                            ":bintrayPublish SUCCESS"()
                        }
                    }
                }
            )

            setup.writeFile("build.gradle.kts", buildTwoPubGradleKts(maven = false, bintray = true))

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
                        .chopChilds { it.value().path in arrayOf(":jar", ":main2Jar") }
                        .toStringTree()

                    actualTaskTree shouldBe setup.expectedTaskGraph
                }

                setup.mockServers.forEach { server ->
                    BintrayRepoResult(
                        server.collectRequests(),
                        coordinates,
                        "username",
                        "maven",
                        "jar",
                        withMetadata = false
                    ) should publishedToBintrayRepositoryCompletely()
                }
            }
        }
    }
})
