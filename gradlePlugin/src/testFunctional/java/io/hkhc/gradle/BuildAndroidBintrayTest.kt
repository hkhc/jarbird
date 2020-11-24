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
import io.hkhc.gradle.test.commonAndroidGradle
import io.hkhc.gradle.test.commonAndroidRootGradle
import io.hkhc.gradle.test.getTestAndroidSdkHomePair
import io.hkhc.gradle.test.publishedToBintrayRepositoryCompletely
import io.hkhc.gradle.test.setupAndroidProperties
import io.hkhc.gradle.test.simplePom
import io.hkhc.utils.FileTree
import io.hkhc.utils.test.tempDirectory
import io.kotest.assertions.fail
import io.kotest.assertions.withClue
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContextScope
import io.kotest.core.test.TestStatus
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.should

@Tags("Multi", "Bintray", "AAR", "Variant")
class BuildAndroidBintrayTest : FunSpec({

    context("Publish Android AAR in to Bintray Repository") {

        val targetTask = "jbPublishToBintray"

        val expectedTaskList = listOf(
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
            ":lib:stripReleaseDebugSymbols=NO_SOURCE",
            ":lib:copyReleaseJniLibsProjectAndLocalJars=SUCCESS",
            ":lib:processReleaseJavaRes=NO_SOURCE",
            ":lib:jbDokkaHtmlLibRelease=SUCCESS",
            ":lib:dokkaJarLibReleaseRelease=SUCCESS",
            ":lib:generatePomFileForLibReleasePublication=SUCCESS",
            ":lib:sourcesJarLibReleaseRelease=SUCCESS",
            ":lib:generateReleaseRFile=SUCCESS",
            ":lib:compileReleaseKotlin=SUCCESS",
            ":lib:compileReleaseJavaWithJavac=SUCCESS",
            ":lib:extractReleaseAnnotations=SUCCESS",
            ":lib:mergeReleaseGeneratedProguardFiles=SUCCESS",
            ":lib:mergeReleaseConsumerProguardFiles=SUCCESS",
            ":lib:mergeReleaseJavaResource=SUCCESS",
            ":lib:syncReleaseLibJars=SUCCESS",
            ":lib:bundleReleaseAar=SUCCESS",
            ":lib:generateMetadataFileForLibReleasePublication=SUCCESS",
            ":lib:signLibReleasePublication=SUCCESS",
            ":lib:_bintrayRecordingCopy=SUCCESS",
            ":lib:publishLibReleasePublicationToMavenLocal=SUCCESS",
            ":lib:bintrayUpload=SUCCESS",
            ":bintrayPublish=SUCCESS",
            ":lib:jbPublishToBintray=SUCCESS"
        )

        fun commonSetup(coordinate: Coordinate, expectedTaskList: List<String>): DefaultGradleProjectSetup {

            val projectDir = tempDirectory()

            return DefaultGradleProjectSetup(projectDir).apply {

                subProjDirs = arrayOf("lib")
                sourceSetTemplateDirs = arrayOf("functionalTestData/libaar")
                setup()
                mockServer = MockBintrayRepositoryServer().apply {
                    setUp(coordinate, "/base")
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
                writeFile(
                    "${subProjDirs[0]}/pom.yaml",
                    simplePom(coordinate, "release", "aar")
                )

                setupGradleProperties {
                    setupAndroidProperties()
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

                println(result.tasks.joinToString(",\n") { "\"$it\"" })

                withClue("expected list of tasks executed with expected result") {
                    result.tasks.map { it.toString() } shouldContainExactlyInAnyOrder setup.expectedTaskList
                }

                setup.mockServer?.let { server ->
                    BintrayRepoResult(
                        server.collectRequests(),
                        coordinate,
                        "username",
                        "maven",
                        "aar"
                    ) should publishedToBintrayRepositoryCompletely()
                } ?: fail("mock server is not available")
            }
        }

        context("with variant attached to version") {
            val coordinate = Coordinate(
                "test.group",
                "test.artifact",
                "0.1",
                versionWithVariant = "0.1-release"
            )
            val setup = commonSetup(coordinate, expectedTaskList)
            setup.writeFile(
                "${setup.subProjDirs[0]}/build.gradle",
                commonAndroidGradle(variantMode = "variantWithVersion()")
            )
            testBody(coordinate, setup)
        }

        context("with variant attached to artifactId") {
            val coordinate = Coordinate(
                "test.group",
                "test.artifact",
                "0.1",
                artifactIdWithVariant = "test.artifact-release"
            )
            val setup = commonSetup(coordinate, expectedTaskList)
            setup.writeFile(
                "${setup.subProjDirs[0]}/build.gradle",
                commonAndroidGradle(variantMode = "variantWithArtifactId()")
            )
            testBody(coordinate, setup)
        }
    }
})
