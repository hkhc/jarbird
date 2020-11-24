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
import io.hkhc.gradle.test.LocalRepoResult
import io.hkhc.gradle.test.publishToMavenLocalCompletely
import io.hkhc.gradle.test.simplePom
import io.hkhc.utils.FileTree
import io.hkhc.utils.test.tempDirectory
import io.kotest.assertions.withClue
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestStatus
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.should

@Tags("Library", "MavenLocal")
class BuildMavenLocalTest : FunSpec({

    context("Publish library to Maven local") {

        val coordinate = Coordinate("test.group", "test.artifact", "0.1")
        val targetTask = "jbPublishToMavenLocal"
        val projectDir = tempDirectory()
        lateinit var setup: DefaultGradleProjectSetup

        beforeTest {

            setup = DefaultGradleProjectSetup(projectDir).apply {
                sourceSetTemplateDirs = arrayOf("functionalTestData/libJavaKotlin")
                setup()
            }

            setup.writeFile(
                "build.gradle",
                """
                plugins {
                    id 'java'
                    id 'org.jetbrains.kotlin.jvm' version '1.3.72'
                    id 'io.hkhc.jarbird'
                }
                repositories {
                    jcenter()
                }
                dependencies {
                    implementation "org.jetbrains.kotlin:kotlin-stdlib"
                }
                """.trimIndent()
            )

            setup.writeFile("pom.yaml", simplePom(coordinate))

            setup.setupGradleProperties()
        }

        afterTest {
            if (it.b.status == TestStatus.Error || it.b.status == TestStatus.Failure) {
                FileTree().dump(projectDir, System.out::println)
            }
        }

        test("execute task '$targetTask'") {

            val result = setup.getGradleTaskTester().runTask(targetTask)

            withClue("expected list of tasks executed with expected result") {
                result.tasks.map { it.toString() } shouldContainExactly listOf(
                    ":jbDokkaHtmlLib=SUCCESS",
                    ":dokkaJarLib=SUCCESS",
                    ":compileKotlin=SUCCESS",
                    ":compileJava=SUCCESS",
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
                    ":jbPublishToMavenLocal=SUCCESS"
                )
            }

            LocalRepoResult(setup.localRepoDirFile, coordinate, "jar") should publishToMavenLocalCompletely()
        }
    }
})
