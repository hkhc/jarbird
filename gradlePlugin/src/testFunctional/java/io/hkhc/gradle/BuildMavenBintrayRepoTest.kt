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
import io.hkhc.gradle.test.BintrayRepoResult
import io.hkhc.gradle.test.Coordinate
import io.hkhc.gradle.test.DefaultGradleProjectSetup
import io.hkhc.gradle.test.LocalRepoResult
import io.hkhc.gradle.test.MavenRepoResult
import io.hkhc.gradle.test.MockArtifactoryRepositoryServer
import io.hkhc.gradle.test.MockBintrayRepositoryServer
import io.hkhc.gradle.test.MockMavenRepositoryServer
import io.hkhc.gradle.test.buildGradle
import io.hkhc.gradle.test.publishToMavenLocalCompletely
import io.hkhc.gradle.test.publishedToArtifactoryRepositoryCompletely
import io.hkhc.gradle.test.publishedToBintrayRepositoryCompletely
import io.hkhc.gradle.test.publishedToMavenRepositoryCompletely
import io.hkhc.gradle.test.shouldBeNoDifference
import io.hkhc.gradle.test.simplePom
import io.hkhc.utils.FileTree
import io.hkhc.utils.test.tempDirectory
import io.kotest.assertions.withClue
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContextScope
import io.kotest.core.test.TestStatus
import io.kotest.matchers.should
import isSnapshot

@Tags("Library", "MavenRepository", "Bintray", "Artifactory")
class BuildMavenBintrayRepoTest : FunSpec({

    context("Publish library") {

        val targetTask = "jbPublish"

        fun commonSetup(
            coordinate: Coordinate,
            maven: Boolean = true,
            bintray: Boolean = true,
            expectedTaskList: List<String>
        ): DefaultGradleProjectSetup {

            val projectDir = tempDirectory()

            return DefaultGradleProjectSetup(projectDir).apply {

                setup()
                if (maven) {
                    mavenMockServer = MockMavenRepositoryServer().apply {
                        setUp(coordinate, "/base")
                    }
                }
                if (bintray) {
                    bintrayMockServer = MockBintrayRepositoryServer().apply {
                        setUp(coordinate, "/base")
                    }
                    artifactoryMockServer = MockArtifactoryRepositoryServer().apply {
                        setUp(coordinate, "/base")
                    }
                }

                writeFile("build.gradle.kts", buildGradle(maven, bintray))

                writeFile("pom.yaml", simplePom(coordinate))

                setupGradleProperties {
                    if (maven) {
                        if (coordinate.version.isSnapshot()) {
                            "repository.maven.mock.release" to "fake-url-that-is-not-going-to-work"
                            "repository.maven.mock.snapshot" to mockServer?.getServerUrl()
                        } else {
                            "repository.maven.mock.release" to mockServer?.getServerUrl()
                            "repository.maven.mock.snapshot" to "fake-url-that-is-not-going-to-work"
                        }
                        "repository.maven.mock.username" to "username"
                        "repository.maven.mock.password" to "password"
                    }

                    if (bintray) {
                        if (coordinate.version.isSnapshot()) {
                            "repository.bintray.snapshot" to artifactoryMockServer?.getServerUrl()
                        } else {
                            "repository.bintray.release" to bintrayMockServer?.getServerUrl()
                        }
                        "repository.bintray.username" to "username"
                        "repository.bintray.apikey" to "password"
                    }
                }

                this.expectedTaskList = expectedTaskList
            }
        }

        suspend fun FunSpecContextScope.testBody(coordinate: Coordinate, setup: DefaultGradleProjectSetup) {
            afterTest {
                setup.mockServer?.teardown()
                if (it.b.status == TestStatus.Error || it.b.status == TestStatus.Failure) {
                    FileTree().dump(setup.projectDir, System.out::println)
                }
            }

            test("execute task '$targetTask'") {

                val result = setup.getGradleTaskTester().runTask(targetTask)

                withClue("expected list of tasks executed with expected result") {
                    println(result.tasks.joinToString(",\n") { "\"$it\"" })
                    result.tasks.map { it.toString() } shouldBeNoDifference setup.expectedTaskList
                }

                LocalRepoResult(setup.localRepoDirFile, coordinate, "jar") should
                    publishToMavenLocalCompletely()

                setup.mavenMockServer?.let { server ->
                    MavenRepoResult(
                        server.collectRequests(),
                        coordinate,
                        "jar"
                    ) should publishedToMavenRepositoryCompletely()
                }

                if (coordinate.version.isSnapshot()) {
                    setup.artifactoryMockServer?.let { server ->
                        ArtifactoryRepoResult(
                            server.collectRequests(),
                            coordinate,
                            "username",
                            "maven",
                            "jar"
                        ) should publishedToArtifactoryRepositoryCompletely()
                    }
                } else {
                    setup.bintrayMockServer?.let { server ->
                        BintrayRepoResult(
                            server.collectRequests(),
                            coordinate,
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
                expectedTaskList = listOf(
                    ":jbDokkaHtmlLib=SUCCESS",
                    ":dokkaJarLib=SUCCESS",
                    ":compileKotlin=SUCCESS",
                    ":compileJava=SUCCESS",
                    ":pluginDescriptors=SUCCESS",
                    ":processResources=NO_SOURCE",
                    ":classes=SUCCESS",
                    ":inspectClassesForKotlinIC=SUCCESS",
                    ":jar=SUCCESS",
                    ":generateMetadataFileForLibPublication=SUCCESS",
                    ":generatePomFileForLibPublication=SUCCESS",
                    ":sourcesJarLib=SUCCESS",
                    ":signLibPublication=SUCCESS",
                    ":_bintrayRecordingCopy=SUCCESS",
                    ":publishLibPublicationToMavenLocal=SUCCESS",
                    ":bintrayUpload=SUCCESS",
                    ":bintrayPublish=SUCCESS",
                    ":jbPublishToBintray=SUCCESS",
                    ":jbPublishLibToMavenLocal=SUCCESS",
                    ":jbPublishToMavenLocal=SUCCESS",
                    ":publishLibPublicationToMavenLibRepository=SUCCESS",
                    ":jbPublishLibToMavenmock=SUCCESS",
                    ":jbPublishLibToMavenRepository=SUCCESS",
                    ":jbPublishToMavenRepository=SUCCESS",
                    ":jbPublish=SUCCESS"
                )
            )

            testBody(coordinate, setup)
        }

        context("to snapshot Maven Repository and Artifactory Repository") {

            val coordinate = Coordinate("test.group", "test.artifact", "0.1-SNAPSHOT")
            val setup = commonSetup(
                coordinate,
                maven = true,
                bintray = true,
                expectedTaskList = listOf(
                    ":jbDokkaHtmlLib=SUCCESS",
                    ":dokkaJarLib=SUCCESS",
                    ":compileKotlin=SUCCESS",
                    ":compileJava=SUCCESS",
                    ":pluginDescriptors=SUCCESS",
                    ":processResources=NO_SOURCE",
                    ":classes=SUCCESS",
                    ":inspectClassesForKotlinIC=SUCCESS",
                    ":jar=SUCCESS",
                    ":generateMetadataFileForLibPublication=SUCCESS",
                    ":generatePomFileForLibPublication=SUCCESS",
                    ":sourcesJarLib=SUCCESS",
                    ":artifactoryPublish=SUCCESS",
                    ":extractModuleInfo=SUCCESS",
                    ":artifactoryDeploy=SUCCESS",
                    ":jbPublishToBintray=SUCCESS",
                    ":publishLibPublicationToMavenLocal=SUCCESS",
                    ":jbPublishLibToMavenLocal=SUCCESS",
                    ":jbPublishToMavenLocal=SUCCESS",
                    ":publishLibPublicationToMavenLibRepository=SUCCESS",
                    ":jbPublishLibToMavenmock=SUCCESS",
                    ":jbPublishLibToMavenRepository=SUCCESS",
                    ":jbPublishToMavenRepository=SUCCESS",
                    ":jbPublish=SUCCESS"

                )
            )

            testBody(coordinate, setup)
        }

        context("to snapshot Maven Repository only") {

            val coordinate = Coordinate("test.group", "test.artifact", "0.1")
            val setup = commonSetup(
                coordinate,
                maven = true,
                bintray = false,
                expectedTaskList = listOf(
                    ":jbDokkaHtmlLib=SUCCESS",
                    ":dokkaJarLib=SUCCESS",
                    ":compileKotlin=SUCCESS",
                    ":compileJava=SUCCESS",
                    ":pluginDescriptors=SUCCESS",
                    ":processResources=NO_SOURCE",
                    ":classes=SUCCESS",
                    ":inspectClassesForKotlinIC=SUCCESS",
                    ":jar=SUCCESS",
                    ":generateMetadataFileForLibPublication=SUCCESS",
                    ":generatePomFileForLibPublication=SUCCESS",
                    ":sourcesJarLib=SUCCESS",
                    ":signLibPublication=SUCCESS",
                    ":publishLibPublicationToMavenLocal=SUCCESS",
                    ":jbPublishLibToMavenLocal=SUCCESS",
                    ":jbPublishToMavenLocal=SUCCESS",
                    ":publishLibPublicationToMavenLibRepository=SUCCESS",
                    ":jbPublishLibToMavenmock=SUCCESS",
                    ":jbPublishLibToMavenRepository=SUCCESS",
                    ":jbPublishToMavenRepository=SUCCESS",
                    ":jbPublish=SUCCESS"
                )
            )

            testBody(coordinate, setup)
        }

        context("to snapshot Bintray Repository only") {

            val coordinate = Coordinate("test.group", "test.artifact", "0.1")
            val setup = commonSetup(
                coordinate,
                maven = false,
                bintray = true,
                expectedTaskList = listOf(
                    ":jbDokkaHtmlLib=SUCCESS",
                    ":dokkaJarLib=SUCCESS",
                    ":compileKotlin=SUCCESS",
                    ":compileJava=SUCCESS",
                    ":pluginDescriptors=SUCCESS",
                    ":processResources=NO_SOURCE",
                    ":classes=SUCCESS",
                    ":inspectClassesForKotlinIC=SUCCESS",
                    ":jar=SUCCESS",
                    ":generateMetadataFileForLibPublication=SUCCESS",
                    ":generatePomFileForLibPublication=SUCCESS",
                    ":sourcesJarLib=SUCCESS",
                    ":signLibPublication=SUCCESS",
                    ":_bintrayRecordingCopy=SUCCESS",
                    ":publishLibPublicationToMavenLocal=SUCCESS",
                    ":bintrayUpload=SUCCESS",
                    ":bintrayPublish=SUCCESS",
                    ":jbPublishToBintray=SUCCESS",
                    ":jbPublishLibToMavenLocal=SUCCESS",
                    ":jbPublishToMavenLocal=SUCCESS",
                    ":jbPublish=SUCCESS"
                )
            )

            testBody(coordinate, setup)
        }
    }
})
