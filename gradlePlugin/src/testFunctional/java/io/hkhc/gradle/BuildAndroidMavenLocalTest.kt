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
import io.hkhc.gradle.test.commonAndroidGradle
import io.hkhc.gradle.test.commonAndroidRootGradle
import io.hkhc.gradle.test.getTestAndroidSdkHomePair
import io.hkhc.gradle.test.publishToMavenLocalCompletely
import io.hkhc.gradle.test.setupAndroidProperties
import io.hkhc.gradle.test.simplePom
import io.hkhc.utils.FileTree
import io.hkhc.utils.test.tempDirectory
import io.kotest.assertions.withClue
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContextScope
import io.kotest.core.test.TestStatus
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.should

@Tags("Multi", "AAR", "MavenLocal", "Variant")
class BuildAndroidMavenLocalTest : FunSpec({

    context("Publish Android AAR in to Maven Local") {

        val targetTask = "jbPublishToMavenLocal"

        val releaseExpectedTaskList = listOf(
            ":lib:preBuild=UP_TO_DATE",
            ":lib:preReleaseBuild=UP_TO_DATE",
            ":lib:compileReleaseAidl=NO_SOURCE",
            ":lib:mergeReleaseJniLibFolders=SUCCESS",
            ":lib:mergeReleaseNativeLibs=SUCCESS",
            ":lib:compileReleaseRenderscript=NO_SOURCE",
            ":lib:generateReleaseBuildConfig=SUCCESS",
            ":lib:generateReleaseResValues=SUCCESS",
            ":lib:generateReleaseResources=SUCCESS",
            ":lib:packageReleaseResources=SUCCESS", // line 10
            ":lib:parseReleaseLocalResources=SUCCESS",
            ":lib:processReleaseManifest=SUCCESS",
            ":lib:javaPreCompileRelease=SUCCESS",
            ":lib:mergeReleaseShaders=SUCCESS",
            ":lib:compileReleaseShaders=NO_SOURCE",
            ":lib:generateReleaseAssets=UP_TO_DATE",
            ":lib:packageReleaseAssets=SUCCESS",
            ":lib:packageReleaseRenderscript=NO_SOURCE",
            ":lib:prepareLintJarForPublish=SUCCESS",
            ":lib:processReleaseJavaRes=NO_SOURCE", // line 20
            ":lib:preDebugBuild=UP_TO_DATE",
            ":lib:compileDebugAidl=NO_SOURCE",
            ":lib:compileDebugRenderscript=NO_SOURCE",
            ":lib:generateDebugBuildConfig=SUCCESS",
            ":lib:generateDebugResValues=SUCCESS",
            ":lib:generateDebugResources=SUCCESS",
            ":lib:packageDebugResources=SUCCESS",
            ":lib:parseDebugLocalResources=SUCCESS",
            ":lib:processDebugManifest=SUCCESS",
            ":lib:stripReleaseDebugSymbols=NO_SOURCE",
            ":lib:copyReleaseJniLibsProjectAndLocalJars=SUCCESS",
            ":lib:generatePomFileForLibReleasePublication=SUCCESS", // line 30
            ":lib:sourcesJarRelease=SUCCESS",
            ":lib:generateDebugRFile=SUCCESS",
            ":lib:generateReleaseRFile=SUCCESS",
            ":lib:compileReleaseKotlin=SUCCESS",
            ":lib:compileReleaseJavaWithJavac=SUCCESS",
            ":lib:extractReleaseAnnotations=SUCCESS",
            ":lib:mergeReleaseGeneratedProguardFiles=SUCCESS",
            ":lib:mergeReleaseConsumerProguardFiles=SUCCESS", // line 40
            ":lib:mergeReleaseJavaResource=SUCCESS",
            ":lib:dokka=SUCCESS",
            ":lib:dokkaJarRelease=SUCCESS",
            ":lib:syncReleaseLibJars=SUCCESS",
            ":lib:bundleReleaseAar=SUCCESS",
            ":lib:generateMetadataFileForLibReleasePublication=SUCCESS",
            ":lib:signLibReleasePublication=SUCCESS",
            ":lib:publishLibReleasePublicationToMavenLocal=SUCCESS",
            ":lib:jbPublishLibReleaseToMavenLocal=SUCCESS",
            ":lib:jbPublishToMavenLocal=SUCCESS", // line 50
            ":jbPublishToMavenLocal=SUCCESS"
        )

        val snapshotExpectedTaskList = listOf(
            ":lib:preBuild=UP_TO_DATE",
            ":lib:preReleaseBuild=UP_TO_DATE",
            ":lib:compileReleaseAidl=NO_SOURCE",
            ":lib:mergeReleaseJniLibFolders=SUCCESS",
            ":lib:mergeReleaseNativeLibs=SUCCESS",
            ":lib:compileReleaseRenderscript=NO_SOURCE",
            ":lib:generateReleaseBuildConfig=SUCCESS",
            ":lib:generateReleaseResValues=SUCCESS",
            ":lib:generateReleaseResources=SUCCESS",
            ":lib:packageReleaseResources=SUCCESS",
            ":lib:parseReleaseLocalResources=SUCCESS",
            ":lib:processReleaseManifest=SUCCESS",
            ":lib:javaPreCompileRelease=SUCCESS",
            ":lib:mergeReleaseShaders=SUCCESS",
            ":lib:compileReleaseShaders=NO_SOURCE",
            ":lib:generateReleaseAssets=UP_TO_DATE",
            ":lib:packageReleaseAssets=SUCCESS",
            ":lib:packageReleaseRenderscript=NO_SOURCE",
            ":lib:prepareLintJarForPublish=SUCCESS",
            ":lib:processReleaseJavaRes=NO_SOURCE",
            ":lib:preDebugBuild=UP_TO_DATE",
            ":lib:compileDebugAidl=NO_SOURCE",
            ":lib:compileDebugRenderscript=NO_SOURCE",
            ":lib:generateDebugBuildConfig=SUCCESS",
            ":lib:generateDebugResValues=SUCCESS",
            ":lib:generateDebugResources=SUCCESS",
            ":lib:packageDebugResources=SUCCESS",
            ":lib:parseDebugLocalResources=SUCCESS",
            ":lib:processDebugManifest=SUCCESS",
            ":lib:generatePomFileForLibReleasePublication=SUCCESS",
            ":lib:sourcesJarRelease=SUCCESS",
            ":lib:stripReleaseDebugSymbols=NO_SOURCE",
            ":lib:copyReleaseJniLibsProjectAndLocalJars=SUCCESS",
            ":lib:generateReleaseRFile=SUCCESS",
            ":lib:generateDebugRFile=SUCCESS",
            ":lib:compileReleaseKotlin=SUCCESS",
            ":lib:compileReleaseJavaWithJavac=SUCCESS",
            ":lib:extractReleaseAnnotations=SUCCESS",
            ":lib:mergeReleaseGeneratedProguardFiles=SUCCESS",
            ":lib:mergeReleaseConsumerProguardFiles=SUCCESS",
            ":lib:mergeReleaseJavaResource=SUCCESS",
            ":lib:dokka=SUCCESS",
            ":lib:dokkaJarRelease=SUCCESS",
            ":lib:syncReleaseLibJars=SUCCESS",
            ":lib:bundleReleaseAar=SUCCESS",
            ":lib:generateMetadataFileForLibReleasePublication=SUCCESS",
            ":lib:publishLibReleasePublicationToMavenLocal=SUCCESS",
            ":lib:jbPublishLibReleaseToMavenLocal=SUCCESS",
            ":lib:jbPublishToMavenLocal=SUCCESS",
            ":jbPublishToMavenLocal=SUCCESS"
        )

        fun commonSetup(coordinate: Coordinate, expectedTaskList: List<String>): DefaultGradleProjectSetup {

            val projectDir = tempDirectory()

            return DefaultGradleProjectSetup(projectDir).apply {

                subProjDirs = arrayOf("lib")
                sourceSetTemplateDirs = arrayOf("functionalTestData/libaar")
                setup()
                setupGradleProperties {
                    setupAndroidProperties()
                }

                envs.apply {
                    val pair = getTestAndroidSdkHomePair()
                    put(pair.first, pair.second)
                }

                setupSettingsGradle(
                    """
                    pluginManagement {
                        repositories {
                            mavenLocal()
                            gradlePluginPortal()
                            mavenCentral()
                        }
                    }
                    """.trimIndent()
                )

                writeFile("build.gradle", commonAndroidRootGradle())

                // Note: sub-projects build.gradle is specified in test case specific context

                writeFile(
                    "${subProjDirs[0]}/pom.yaml",
                    simplePom(coordinate, "release", "aar")
                )

                this.expectedTaskList = expectedTaskList
            }
        }

        suspend fun FunSpecContextScope.testBody(coordinate: Coordinate, setup: DefaultGradleProjectSetup) {

            afterTest {
                if (it.b.status == TestStatus.Error || it.b.status == TestStatus.Failure) {
                    FileTree().dump(setup.projectDir, System.out::println)
                }
            }

            test("execute task '$targetTask'") {

                val result = setup.getGradleTaskTester().runTask(targetTask)

                println(result.tasks.joinToString("\n") { "\"$it\"," })

                withClue("expected list of tasks executed with expected result") {
                    result.tasks.map { it.toString() } shouldContainExactlyInAnyOrder setup.expectedTaskList!!
                }

                LocalRepoResult(
                    setup.localRepoDirFile,
                    coordinate,
                    "aar"
                ) should publishToMavenLocalCompletely()
            }
        }

        context("with variant attached to version") {
            context("to release Mavel Local") {
                val coordinate = Coordinate(
                    "test.group",
                    "test.artifact",
                    "0.1",
                    versionWithVariant = "0.1-release"
                )
                val setup = commonSetup(coordinate, releaseExpectedTaskList)
                setup.writeFile(
                    "${setup.subProjDirs[0]}/build.gradle",
                    commonAndroidGradle(variantMode = "variantWithVersion()")
                )
                testBody(coordinate, setup)
            }
            context("to snapshot Mavel Local") {
                val coordinate = Coordinate(
                    "test.group",
                    "test.artifact",
                    "0.1-SNAPSHOT",
                    versionWithVariant = "0.1-release-SNAPSHOT"
                )
                val setup = commonSetup(coordinate, snapshotExpectedTaskList)
                setup.writeFile(
                    "${setup.subProjDirs[0]}/build.gradle",
                    commonAndroidGradle(variantMode = "variantWithVersion()")
                )
                testBody(coordinate, setup)
            }
        }

        context("with variant attached to artifactId") {
            val coordinate = Coordinate(
                "test.group",
                "test.artifact",
                "0.1",
                artifactIdWithVariant = "test.artifact-release"
            )
            val setup = commonSetup(coordinate, releaseExpectedTaskList)
            setup.writeFile(
                "${setup.subProjDirs[0]}/build.gradle",
                commonAndroidGradle(variantMode = "variantWithArtifactId()")
            )
            testBody(coordinate, setup)
        }
    }
})
