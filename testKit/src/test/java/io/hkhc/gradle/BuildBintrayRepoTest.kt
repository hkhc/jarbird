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
import io.hkhc.gradle.test.publishedToBintrayRepositoryCompletely
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

@Tags("Bintray", "Library")
class BuildBintrayRepoTest : FunSpec({

    context("Publish library") {

        val targetTask = "jbPublishToBintray"

        fun commonSetup(coordinate: Coordinate, expectedTaskList: List<String>): DefaultGradleProjectSetup {

            val projectDir = tempDirectory()

            return DefaultGradleProjectSetup(projectDir).apply {

                setup()
                mockServers.add(
                    MockBintrayRepositoryServer().apply {
                        setUp(listOf(coordinate), "/base")
                    }
                )

                writeFile("build.gradle.kts", buildGradleCustomBintray())

                writeFile("pom.yaml", simplePom(coordinate))

                setupGradleProperties {
                    "repository.bintray.release" to mockServers[0].getServerUrl()
                    "repository.bintray.username" to "username"
                    "repository.bintray.apikey" to "password"
                }

                this.expectedTaskList = expectedTaskList
            }
        }

        suspend fun FunSpecContextScope.testBody(coordinate: Coordinate, setup: DefaultGradleProjectSetup) {
            afterTest {
                setup.mockServers.forEach { it.teardown() }
                if (it.b.status == TestStatus.Error || it.b.status == TestStatus.Failure) {
                    FileTree().dump(setup.projectDir, System.out::println)
                }
            }

            test("execute task '$targetTask'") {

                val result = setup.getGradleTaskTester().runTask(targetTask)

                withClue("expected list of tasks executed with expected result") {
                    result.tasks.map { it.toString() } shouldBeNoDifference setup.expectedTaskList
                }

                setup.mockServers[0].let { server ->
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

        context("to release Bintray Repository") {

            val coordinate = Coordinate("test.group", "test.artifact", "0.1")
            val setup = commonSetup(
                coordinate,
                listOf(
                    ":compileKotlin=SUCCESS",
                    ":compileJava=SUCCESS",
                    ":pluginDescriptors=SUCCESS",
                    ":processResources=NO_SOURCE",
                    ":classes=SUCCESS",
                    ":inspectClassesForKotlinIC=SUCCESS",
                    ":jar=SUCCESS",
                    ":generateMetadataFileForTestArtifactPublication=SUCCESS",
                    ":generatePomFileForTestArtifactPublication=SUCCESS",
                    ":jbDokkaHtmlTestArtifact=SUCCESS",
                    ":jbDokkaJarTestArtifact=SUCCESS",
                    ":sourcesJarTestArtifact=SUCCESS",
                    ":signTestArtifactPublication=SUCCESS",
                    ":publishTestArtifactPublicationToMavenLocal=SUCCESS",
                    ":bintrayUpload=SUCCESS",
                    ":bintrayPublish=SUCCESS",
                    ":jbPublishToBintray=SUCCESS"
                )
            )

            testBody(coordinate, setup)
        }
    }
})
