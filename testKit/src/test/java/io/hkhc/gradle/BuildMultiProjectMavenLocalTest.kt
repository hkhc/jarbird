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
import io.hkhc.gradle.test.getTestGradleHomePair
import io.hkhc.gradle.test.publishToMavenLocalCompletely
import io.hkhc.gradle.test.shouldBeNoDifference
import io.hkhc.gradle.test.simplePomRoot
import io.hkhc.gradle.test.simpleSubProj
import io.hkhc.utils.FileTree
import io.hkhc.utils.test.tempDirectory
import io.kotest.assertions.withClue
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestStatus
import io.kotest.matchers.should

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
                        id 'io.hkhc.jarbird'
                        id 'com.dorongold.task-tree' version '1.5'
                    }
                    """.trimIndent()
                )
                coordinates.zip(subProjDirs).forEach { (coor, dir) ->
                    writeFile("$dir/pom.yaml", simpleSubProj(coor))
                    writeFile(
                        "$dir/build.gradle",
                        """
                        plugins {
                            id 'java'
                            id 'io.hkhc.jarbird'
                        }
                        repositories {
                            jcenter()
                        }
                        """.trimIndent()
                    )
                }
            }
        }

        afterTest {
            if (it.b.status == TestStatus.Error || it.b.status == TestStatus.Failure) {
                FileTree().dump(projectDir, System.out::println)
            }
        }

        test("execute task '$targetTask'") {

            val result = tester.runTask(targetTask)

            withClue("expected list of tasks executed with expected result") {
                result.tasks.map { it.toString() } shouldBeNoDifference listOf(
                    ":lib1:compileJava=SUCCESS",
                    ":lib1:processResources=NO_SOURCE",
                    ":lib1:classes=SUCCESS",
                    ":lib1:jar=SUCCESS",
                    ":lib1:generateMetadataFileForTestArtifactOnePublication=SUCCESS",
                    ":lib1:generatePomFileForTestArtifactOnePublication=SUCCESS",
                    ":lib1:jbDokkaHtmlTestArtifactOne=SUCCESS",
                    ":lib1:jbDokkaJarTestArtifactOne=SUCCESS",
                    ":lib1:sourcesJarTestArtifactOne=SUCCESS",
                    ":lib1:signTestArtifactOnePublication=SUCCESS",
                    ":lib1:publishTestArtifactOnePublicationToMavenLocal=SUCCESS",
                    ":lib1:jbPublishTestArtifactOneToMavenLocal=SUCCESS",
                    ":lib1:jbPublishToMavenLocal=SUCCESS",
                    ":lib2:compileJava=SUCCESS",
                    ":lib2:processResources=NO_SOURCE",
                    ":lib2:classes=SUCCESS",
                    ":lib2:jar=SUCCESS",
                    ":lib2:generateMetadataFileForTestArtifactTwoPublication=SUCCESS",
                    ":lib2:generatePomFileForTestArtifactTwoPublication=SUCCESS",
                    ":lib2:jbDokkaHtmlTestArtifactTwo=SUCCESS",
                    ":lib2:jbDokkaJarTestArtifactTwo=SUCCESS",
                    ":lib2:sourcesJarTestArtifactTwo=SUCCESS",
                    ":lib2:signTestArtifactTwoPublication=SUCCESS",
                    ":lib2:publishTestArtifactTwoPublicationToMavenLocal=SUCCESS",
                    ":lib2:jbPublishTestArtifactTwoToMavenLocal=SUCCESS",
                    ":lib2:jbPublishToMavenLocal=SUCCESS",
                    ":jbPublishToMavenLocal=SUCCESS"
                )
            }

            coordinates.forEach { coor ->
                LocalRepoResult(setup.localRepoDirFile, coor, "jar") should publishToMavenLocalCompletely()
            }
        }
    }
})
