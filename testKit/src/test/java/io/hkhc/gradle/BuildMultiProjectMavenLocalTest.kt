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
import io.hkhc.gradle.test.GradleTaskTester
import io.hkhc.gradle.test.LocalRepoResult
import io.hkhc.gradle.test.getTaskTree
import io.hkhc.gradle.test.getTestGradleHomePair
import io.hkhc.gradle.test.printFileTree
import io.hkhc.gradle.test.mavenlocal.publishToMavenLocalCompletely
import io.hkhc.gradle.test.simplePomRoot
import io.hkhc.gradle.test.simpleSubProj
import io.hkhc.test.utils.test.tempDirectory
import io.hkhc.utils.tree.NoBarTheme
import io.hkhc.utils.tree.chopChilds
import io.hkhc.utils.tree.stringTreeOf
import io.hkhc.utils.tree.toStringTree
import io.kotest.assertions.withClue
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestStatus
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import java.io.File

@Tags("Library", "MavenLocal", "Multi")
class BuildMultiProjectMavenLocalTest : FunSpec({

    context("Publish two libraries in two sub-projects to Maven Local") {

        val coordinates = arrayOf(
            Coordinate("test.group", "test.artifact.one", "0.1"),
            Coordinate("test.group", "test.artifact.two", "0.1")
        )
        val targetTask = "jbPublishToMavenLocal"
        val projectDir = tempDirectory()
        lateinit var setup: DefaultGradleProjectSetup
        val tester = GradleTaskTester(
            projectDir,
            mutableMapOf(
                getTestGradleHomePair(projectDir)
            )
        )

        beforeTest {

            setup = DefaultGradleProjectSetup(projectDir).apply {
                subProjDirs = arrayOf("lib1", "lib2")
                sourceSetTemplateDirs = arrayOf("functionalTestData/lib", "functionalTestData/lib2")
                setup()
                setupGradleProperties()
                writeFile("pom.yaml", simplePomRoot())
                writeFile(
                    "build.gradle",
                    """
                    plugins {
                        id 'org.barfuin.gradle.taskinfo' version '1.0.5'
                        id 'io.hkhc.jarbird'
                    }
                   
                    """.trimIndent()
                )
                coordinates.zip(subProjDirs).forEach { (coor, dir) ->
                    writeFile("$dir/pom.yaml", simpleSubProj(coor))
                    writeFile(
                        "$dir/build.gradle",
                        """
                        plugins {
                            id 'org.barfuin.gradle.taskinfo' 
                            id 'java'
                            id 'io.hkhc.jarbird'
                        }
                        repositories {
                            jcenter()
                        }
                        jarbird {
                            mavenLocal()
                            pub {}
                        }
                        """.trimIndent()
                    )
                }
            }
        }

        afterTest {
            if (it.b.status == TestStatus.Error || it.b.status == TestStatus.Failure) {
                printFileTree(setup.projectDir)
            }
        }

        test("execute task '$targetTask'") {

            setup.getGradleTaskTester().runTasks(arrayOf("lib1:tiJson", "lib1:$targetTask"))
            setup.getGradleTaskTester().runTasks(arrayOf("lib2:tiJson", "lib2:$targetTask"))
            val result = tester.runTask(targetTask)

            withClue("expected list of tasks executed with expected result") {
                val actualTaskTreeLib1 = getTaskTree(File(projectDir,"lib1"), targetTask, result)
                    .chopChilds { it.value().path in arrayOf(":lib1:jar") }
                    .toStringTree()

                actualTaskTreeLib1 shouldBe
                    stringTreeOf(NoBarTheme) {
                            ":lib1:jbPublishToMavenLocal SUCCESS" {
                                ":lib1:jbPublishTestArtifactOneToMavenLocal SUCCESS" {
                                    ":lib1:publishTestArtifactOnePublicationToMavenLocal SUCCESS" {
                                        ":lib1:generateMetadataFileForTestArtifactOnePublication SUCCESS" {
                                            ":lib1:jar SUCCESS"()
                                        }
                                        ":lib1:generatePomFileForTestArtifactOnePublication SUCCESS"()
                                        ":lib1:jar SUCCESS"()
                                        ":lib1:jbDokkaJarTestArtifactOne SUCCESS" {
                                            ":lib1:jbDokkaHtmlTestArtifactOne SUCCESS"()
                                        }
                                        ":lib1:signTestArtifactOnePublication SUCCESS" {
                                            ":lib1:generateMetadataFileForTestArtifactOnePublication SUCCESS" {
                                                ":lib1:jar SUCCESS"()
                                            }
                                            ":lib1:generatePomFileForTestArtifactOnePublication SUCCESS" ()
                                            ":lib1:jar SUCCESS"()
                                            ":lib1:jbDokkaJarTestArtifactOne SUCCESS" {
                                                ":lib1:jbDokkaHtmlTestArtifactOne SUCCESS"()
                                            }
                                            ":lib1:sourcesJarTestArtifactOne SUCCESS"()
                                        }
                                        ":lib1:sourcesJarTestArtifactOne SUCCESS"()
                                    }
                                }
                            }
                    }

                val actualTaskTreeLib2 = getTaskTree(File(projectDir,"lib2"), targetTask, result)
                    .chopChilds { it.value().path in arrayOf(":lib2:jar") }
                    .toStringTree()

                actualTaskTreeLib2 shouldBe
                    stringTreeOf(NoBarTheme) {
                            ":lib2:jbPublishToMavenLocal SUCCESS" {
                                ":lib2:jbPublishTestArtifactTwoToMavenLocal SUCCESS" {
                                    ":lib2:publishTestArtifactTwoPublicationToMavenLocal SUCCESS" {
                                        ":lib2:generateMetadataFileForTestArtifactTwoPublication SUCCESS" {
                                            ":lib2:jar SUCCESS"()
                                        }
                                        ":lib2:generatePomFileForTestArtifactTwoPublication SUCCESS"()
                                        ":lib2:jar SUCCESS"()
                                        ":lib2:jbDokkaJarTestArtifactTwo SUCCESS" {
                                            ":lib2:jbDokkaHtmlTestArtifactTwo SUCCESS"()
                                        }
                                        ":lib2:signTestArtifactTwoPublication SUCCESS" {
                                            ":lib2:generateMetadataFileForTestArtifactTwoPublication SUCCESS" {
                                                ":lib2:jar SUCCESS"()
                                            }
                                            ":lib2:generatePomFileForTestArtifactTwoPublication SUCCESS" ()
                                            ":lib2:jar SUCCESS"()
                                            ":lib2:jbDokkaJarTestArtifactTwo SUCCESS" {
                                                ":lib2:jbDokkaHtmlTestArtifactTwo SUCCESS"()
                                            }
                                            ":lib2:sourcesJarTestArtifactTwo SUCCESS"()
                                        }
                                        ":lib2:sourcesJarTestArtifactTwo SUCCESS"()
                                    }
                                }
                            }
                    }

            }

            coordinates.forEach { coor ->
                LocalRepoResult(setup.localRepoDirFile, coor, "jar") should publishToMavenLocalCompletely()
            }
        }
    }
})
