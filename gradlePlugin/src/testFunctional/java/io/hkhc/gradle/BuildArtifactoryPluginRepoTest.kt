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
import io.hkhc.gradle.test.buildGradleCustomBintray
import io.hkhc.gradle.test.pluginPom
import io.hkhc.gradle.test.publishedToArtifactoryRepositoryCompletely
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
import io.kotest.matchers.shouldBe
import java.io.File

@Tags("Plugin", "Bintray")
class BuildArtifactoryPluginRepoTest : FunSpec({

    context("Publish Gradle plugin") {

        val targetTask = "jbPublishToBintray"

        fun commonSetup(coordinate: Coordinate, expectedTaskList: List<String>): DefaultGradleProjectSetup {

            val projectDir = tempDirectory()

            return DefaultGradleProjectSetup(projectDir).apply {

                setup()
                mockServers.add(
                    MockArtifactoryRepositoryServer().apply {
                        setUp(listOf(coordinate), "/base")
                    }
                )

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
                    "repository.bintray.snapshot" to mockServers[0].getServerUrl()
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

                val pluginPom = File(
                    setup.projectDir,
                    "build/publications/testArtifactPluginMarkerMaven/pom-default.xml"
                ).readText()
                pluginPom shouldBe pluginPom(coordinate)

                setup.mockServers[0].let { server ->
                    ArtifactoryRepoResult(
                        server.collectRequests(),
                        coordinate,
                        "username",
                        "oss-snapshot-local",
                        "jar"
                    ) should publishedToArtifactoryRepositoryCompletely()
                }
            }
        }

        context("to snapshot Bintray Repository") {

            val coordinate = Coordinate("test.group", "test.artifact", "0.1-SNAPSHOT", "test.plugin")
            val setup = commonSetup(
                coordinate,
                listOf(
                    ":compileKotlin=SUCCESS",
                    ":compileJava=SUCCESS",
                    ":pluginDescriptors=SUCCESS",
                    ":processResources=SUCCESS",
                    ":classes=SUCCESS",
                    ":inspectClassesForKotlinIC=SUCCESS",
                    ":jar=SUCCESS",
                    ":generateMetadataFileForTestArtifactPublication=SUCCESS",
                    ":generatePomFileForTestArtifactPluginMarkerMavenPublication=SUCCESS",
                    ":generatePomFileForTestArtifactPublication=SUCCESS",
                    ":jbDokkaHtmlTestArtifact=SUCCESS",
                    ":jbDokkaJarTestArtifact=SUCCESS",
                    ":sourcesJarTestArtifact=SUCCESS",
                    ":artifactoryPublish=SUCCESS",
                    ":extractModuleInfo=SUCCESS",
                    ":artifactoryDeploy=SUCCESS",
                    ":jbPublishToArtifactory=SUCCESS",
                    ":jbPublishToBintray=SUCCESS"
                )
            )

            testBody(coordinate, setup)
        }
    }
})