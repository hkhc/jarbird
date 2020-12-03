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
import io.hkhc.gradle.test.buildGradlePlugin
import io.hkhc.gradle.test.pluginPom
import io.hkhc.gradle.test.publishedToMavenRepositoryCompletely
import io.hkhc.gradle.test.shouldBeNoDifference
import io.hkhc.gradle.test.simplePom
import io.hkhc.utils.FileTree
import io.hkhc.utils.test.tempDirectory
import io.kotest.assertions.fail
import io.kotest.assertions.withClue
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContextScope
import io.kotest.core.test.TestStatus
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import isSnapshot
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

        val targetTask = "jbPublishToMavenRepository"

        fun commonSetup(coordinate: Coordinate, expectedTaskList: List<String>): DefaultGradleProjectSetup {

            val projectDir = tempDirectory()

            return DefaultGradleProjectSetup(projectDir).apply {

                this.sourceSetTemplateDirs = arrayOf("functionalTestData/plugin/src")
                setup()
                mockServer = MockMavenRepositoryServer().apply {
                    setUp(coordinate, "/base")
                }

                writeFile("build.gradle.kts", buildGradlePlugin())

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
                        "repository.maven.mock.snapshot" to mockServer?.getServerUrl()
                    } else {
                        "repository.maven.mock.release" to mockServer?.getServerUrl()
                        "repository.maven.mock.snapshot" to "fake-url-that-is-not-going-to-work"
                    }
                    "repository.maven.mock.username" to "username"
                    "repository.maven.mock.password" to "password"
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
                    result.tasks.map { it.toString() } shouldBeNoDifference setup.expectedTaskList
                }

                val pluginPom = File(setup.projectDir, "build/publications/libPluginMarkerMaven/pom-default.xml")
                    .readText()
                pluginPom shouldBe pluginPom(coordinate)

                setup.mockServer?.let { server ->
                    MavenRepoResult(
                        server.collectRequests(),
                        coordinate,
                        "jar"
                    ) should publishedToMavenRepositoryCompletely()
                } ?: fail("mock server is not available")
            }
        }

        context("to release Maven Repository") {

            val coordinate = Coordinate("test.group", "test.artifact", "0.1", "test.plugin")
            val setup = commonSetup(
                coordinate,
                listOf(
                    ":generatePomFileForLibPluginMarkerMavenPublication=SUCCESS",
                    ":publishLibPluginMarkerMavenPublicationToMavenLibRepository=SUCCESS",
                    ":jbDokkaHtmlLib=SUCCESS",
                    ":dokkaJarLib=SUCCESS",
                    ":compileKotlin=NO_SOURCE",
                    ":compileJava=NO_SOURCE",
                    ":pluginDescriptors=SUCCESS",
                    ":processResources=SUCCESS",
                    ":classes=SUCCESS",
                    ":inspectClassesForKotlinIC=SUCCESS",
                    ":jar=SUCCESS",
                    ":generateMetadataFileForLibPublication=SUCCESS",
                    ":generatePomFileForLibPublication=SUCCESS",
                    ":sourcesJarLib=SUCCESS",
                    ":signLibPublication=SUCCESS",
                    ":publishLibPublicationToMavenLibRepository=SUCCESS",
                    ":jbPublishLibToMavenmock=SUCCESS",
                    ":jbPublishLibToMavenRepository=SUCCESS",
                    ":jbPublishToMavenRepository=SUCCESS"
                )
            )

            testBody(coordinate, setup)
        }

        context("to snapshot Maven Repository") {

            val coordinate = Coordinate("test.group", "test.artifact", "0.1-SNAPSHOT", "test.plugin")
            val setup = commonSetup(
                coordinate,
                listOf(
                    ":generatePomFileForLibPluginMarkerMavenPublication=SUCCESS",
                    ":publishLibPluginMarkerMavenPublicationToMavenLibRepository=SUCCESS",
                    ":jbDokkaHtmlLib=SUCCESS",
                    ":dokkaJarLib=SUCCESS",
                    ":compileKotlin=NO_SOURCE",
                    ":compileJava=NO_SOURCE",
                    ":pluginDescriptors=SUCCESS",
                    ":processResources=SUCCESS",
                    ":classes=SUCCESS",
                    ":inspectClassesForKotlinIC=SUCCESS",
                    ":jar=SUCCESS",
                    ":generateMetadataFileForLibPublication=SUCCESS",
                    ":generatePomFileForLibPublication=SUCCESS",
                    ":sourcesJarLib=SUCCESS",
                    ":publishLibPublicationToMavenLibRepository=SUCCESS",
                    ":jbPublishLibToMavenmock=SUCCESS",
                    ":jbPublishLibToMavenRepository=SUCCESS",
                    ":jbPublishToMavenRepository=SUCCESS"
                )
            )

            testBody(coordinate, setup)
        }
    }
})
