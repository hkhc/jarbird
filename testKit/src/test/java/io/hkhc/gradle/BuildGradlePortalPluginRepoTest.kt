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
import io.hkhc.gradle.test.MockGradlePortalRepositoryServer
import io.hkhc.gradle.test.buildGradlePortalPluginKts
import io.hkhc.gradle.test.getTaskTree
import io.hkhc.gradle.test.pluginPom
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
import io.kotest.core.spec.style.scopes.FunSpecContextScope
import io.kotest.core.test.TestStatus
import io.kotest.matchers.shouldBe

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
class BuildGradlePortalPluginRepoTest : FunSpec({

    context("Publish Gradle plugin") {

        val targetTask = "jbPublishToGradlePluginPortal"

        fun commonSetup(coordinate: Coordinate, expectedTaskGraph: Tree<String>): DefaultGradleProjectSetup {

            val projectDir = tempDirectory()

            return DefaultGradleProjectSetup(projectDir).apply {

                sourceSetTemplateDirs = arrayOf("functionalTestData/plugin")
                setup()
                mockServers.add(
                    MockGradlePortalRepositoryServer().apply {
                        setUp(listOf(coordinate), "/base")
                    }
                )

                System.setProperty("gradle.portal.url", mockServers[0].getServerUrl())

                setupGradleProperties {
                    "gradle.publish.key" to "key"
                    "gradle.publish.secret" to "secret"
                }

                writeFile("build.gradle.kts", buildGradlePortalPluginKts())

                writeFile(
                    "pom.yaml",
                    simplePom(coordinate) + '\n' +
                        pluginPom(
                            coordinate.pluginId ?: "non-exist-plugin-id",
                            "TestPlugin"
                        )
                )

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

//                val pluginPom = File(
//                    setup.projectDir,
//                    "build/publish-generated-resources/pom.xml"
//                ).readText()
//                pluginPom shouldBe pluginPom(coordinate)

//                setup.mockServers.forEach { server ->
//                    GradlePortalRepoResult(
//                        server.collectRequests(),
//                        listOf(coordinate),
//                        "jar"
//                    ) should publishedPluginToGradlePortalCompletely()
//                }
            }
        }

        context("to release Maven Repository") {

            val coordinate = Coordinate("test.group", "test.artifact", "0.1", "test.plugin")
            val setup = commonSetup(
                coordinate,
                stringTreeOf(NoBarTheme) {
                    ":jbPublishToGradlePluginPortal SUCCESS" {
                        ":jbPublishTestArtifactToGradlePluginPortal SUCCESS" {
                            ":publishPlugins SUCCESS" {
                                ":jar SUCCESS"()
                                ":publishPluginJar SUCCESS"()
                                ":publishPluginJavaDocsJar SUCCESS" {
                                    ":javadoc SUCCESS" {
                                        ":classes SUCCESS" {
                                            ":compileJava SUCCESS" {
                                                ":compileKotlin SUCCESS"()
                                            }
                                            ":processResources SUCCESS" {
                                                ":pluginDescriptors SUCCESS"()
                                            }
                                        }
                                        ":compileKotlin SUCCESS"()
                                    }
                                }
                            }
                        }
                    }
                }
            )

            testBody(coordinate, setup)
        }

//        context("to snapshot Maven Repository") {
//
//            val coordinate = Coordinate("test.group", "test.artifact", "0.1-SNAPSHOT", "test.plugin")
//            val setup = commonSetup(
//                coordinate,
//                stringTreeOf(NoBarTheme) {
//                    ":jbPublishToGradlePluginPortal SUCCESS" {
//                        ":jbPublishTestArtifactToGradlePluginPortal SUCCESS" {
//                            ":publishPlugins SUCCESS" {
//                                ":jar SUCCESS"()
//                                ":publishPluginJar SUCCESS"()
//                                ":publishPluginJavaDocsJar SUCCESS" {
//                                    ":javadoc SUCCESS" {
//                                        ":classes SUCCESS" {
//                                            ":compileJava SUCCESS" {
//                                                ":compileKotlin SUCCESS"()
//                                            }
//                                            ":processResources SUCCESS" {
//                                                ":pluginDescriptors SUCCESS"()
//                                            }
//                                        }
//                                        ":compileKotlin SUCCESS"()
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            )
//
//            testBody(coordinate, setup)
//        }
    }
})
