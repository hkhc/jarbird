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
import io.hkhc.gradle.test.MockArtifactoryRepositoryServer
import io.hkhc.gradle.test.buildGradleCustomArtifactrory
import io.hkhc.gradle.test.publishedToArtifactoryRepositoryCompletely
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

@Tags("Artifactory", "Library")
class BuildArtifactoryRepoTest : FunSpec({

    context("Publish library") {

        val targetTask = "jbPublishToBintray"

        fun commonSetup(coordinate: Coordinate, expectedTaskList: List<String>): DefaultGradleProjectSetup {

            val projectDir = tempDirectory()

            return DefaultGradleProjectSetup(projectDir).apply {

                setup()
                mockServer = MockArtifactoryRepositoryServer().apply {
                    setUp(coordinate, "/base")
                }

                writeFile("build.gradle.kts", buildGradleCustomArtifactrory())

                writeFile("pom.yaml", simplePom(coordinate))

                setupGradleProperties {
                    "repository.bintray.snapshot" to mockServer?.getServerUrl()
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
                    result.tasks.map { it.toString() } shouldContainExactly setup.expectedTaskList
                }

                setup.mockServer?.let { server ->
                    ArtifactoryRepoResult(
                        server.collectRequests(),
                        coordinate,
                        "username",
                        "maven",
                        "jar"
                    ) should publishedToArtifactoryRepositoryCompletely()
                } ?: fail("mock server is not available")
            }
        }

        context("to release Artifactory Repository") {

            val coordinate = Coordinate("test.group", "test.artifact", "0.1-SNAPSHOT")
            val setup = commonSetup(
                coordinate,
                listOf(
                    ":dokka=SUCCESS",
                    ":dokkaJar=SUCCESS",
                    ":compileKotlin=SUCCESS",
                    ":compileJava=SUCCESS",
                    ":pluginDescriptors=SUCCESS",
                    ":processResources=NO_SOURCE",
                    ":classes=SUCCESS",
                    ":inspectClassesForKotlinIC=SUCCESS",
                    ":jar=SUCCESS",
                    ":generateMetadataFileForLibPublication=SUCCESS",
                    ":generatePomFileForLibPublication=SUCCESS",
                    ":sourcesJar=SUCCESS",
                    ":artifactoryPublish=SUCCESS",
                    ":extractModuleInfo=SUCCESS",
                    ":artifactoryDeploy=SUCCESS",
                    ":jbPublishToBintray=SUCCESS"
                )
            )

            testBody(coordinate, setup)
        }
    }
})
