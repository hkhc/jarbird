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
import io.hkhc.gradle.test.MockBintrayRepositoryServer
import io.hkhc.gradle.test.buildGradleCustomBintray
import io.hkhc.gradle.test.pluginPom
import io.hkhc.gradle.test.publishedToBintrayRepositoryCompletely
import io.hkhc.gradle.test.simplePom
import io.hkhc.utils.FileTree
import io.hkhc.utils.test.tempDirectory
import io.kotest.assertions.fail
import io.kotest.assertions.withClue
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContextScope
import io.kotest.core.test.TestStatus
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.should

@Tags("Plugin", "Bintray")
class BuildBintrayPluginRepoTest : FunSpec({

    context("Publish Gradle plugin") {

        val targetTask = "jbPublishToBintray"

        fun commonSetup(coordinate: Coordinate, expectedTaskList: List<String>): DefaultGradleProjectSetup {

            val projectDir = tempDirectory()

            return DefaultGradleProjectSetup(projectDir).apply {

                setup()
                mockServer = MockBintrayRepositoryServer().apply {
                    setUp(coordinate, "/base")
                }

                writeFile("build.gradle.kts", buildGradleCustomBintray())

                writeFile(
                    "pom.yaml",
                    simplePom(coordinate) + '\n' +
                        pluginPom(
                            coordinate.pluginId ?: "non-exist-plugin-id",
                            "TestPluginClass"
                        )
                )

                setupGradleProperties {
                    "repository.bintray.release" to mockServer?.getServerUrl()
                    "repository.bintray.username" to "username"
                    "repository.bintray.apikey" to "password"
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
                    result.tasks.map { it.toString() } shouldContainExactly setup.expectedTaskList!!
                }

                setup.mockServer?.let { server ->
                    BintrayRepoResult(
                        server.collectRequests(),
                        coordinate,
                        "username",
                        "maven",
                        "jar"
                    ) should publishedToBintrayRepositoryCompletely()
                } ?: fail("mock server is not available")
            }
        }

        context("to release Bintray Repository") {

            val coordinate = Coordinate("test.group", "test.artifact", "0.1", "test.plugin")
            val setup = commonSetup(
                coordinate,
                listOf(
                    ":dokka=SUCCESS",
                    ":dokkaJar=SUCCESS",
                    ":compileKotlin=SUCCESS",
                    ":compileJava=SUCCESS",
                    ":pluginDescriptors=SUCCESS",
                    ":processResources=SUCCESS",
                    ":classes=SUCCESS",
                    ":inspectClassesForKotlinIC=SUCCESS",
                    ":jar=SUCCESS",
                    ":generateMetadataFileForLibPublication=SUCCESS",
                    ":generatePomFileForLibPublication=SUCCESS",
                    ":sourcesJar=SUCCESS",
                    ":signLibPublication=SUCCESS",
                    ":_bintrayRecordingCopy=SUCCESS",
                    ":generatePomFileForLibPluginMarkerMavenPublication=SUCCESS",
                    ":publishLibPluginMarkerMavenPublicationToMavenLocal=SUCCESS",
                    ":publishLibPublicationToMavenLocal=SUCCESS",
                    ":bintrayUpload=SUCCESS",
                    ":bintrayPublish=SUCCESS",
                    ":jbPublishToBintray=SUCCESS"
                )
            )

            testBody(coordinate, setup)
        }
    }
})
