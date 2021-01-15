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

import com.github.difflib.DiffUtils
import com.github.difflib.algorithm.myers.MyersDiff
import io.hkhc.gradle.pom.internal.isSnapshot
import io.hkhc.gradle.test.Coordinate
import io.hkhc.gradle.test.DefaultGradleProjectSetup
import io.hkhc.gradle.test.MavenRepoResult
import io.hkhc.gradle.test.MockMavenRepositoryServer
import io.hkhc.gradle.test.commonAndroidGradle
import io.hkhc.gradle.test.commonAndroidRootGradle
import io.hkhc.gradle.test.except
import io.hkhc.gradle.test.getTestAndroidSdkHomePair
import io.hkhc.gradle.test.publishedToMavenRepositoryCompletely
import io.hkhc.gradle.test.setupAndroidProperties
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

@Tags("Multi", "AAR", "MavenRepository", "Variant")
class BuildAndroidMavenRepoTest : FunSpec({

    context("Publish Android AAR in to Maven Repository") {

        val targetTask = "jbPublishToMavenRepository"

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
            ":lib:generatePomFileForTestArtifactReleasePublication=SUCCESS",
            ":lib:jbDokkaHtmlTestArtifactRelease=SUCCESS",
            ":lib:jbDokkaJarTestArtifactReleaseRelease=SUCCESS",
            ":lib:sourcesJarTestArtifactReleaseRelease=SUCCESS",
            ":lib:generateReleaseRFile=SUCCESS",
            ":lib:compileReleaseKotlin=SUCCESS",
            ":lib:compileReleaseJavaWithJavac=SUCCESS",
            ":lib:extractReleaseAnnotations=SUCCESS",
            ":lib:mergeReleaseGeneratedProguardFiles=SUCCESS",
            ":lib:mergeReleaseConsumerProguardFiles=SUCCESS",
            ":lib:mergeReleaseJavaResource=SUCCESS",
            ":lib:syncReleaseLibJars=SUCCESS",
            ":lib:bundleReleaseAar=SUCCESS",
            ":lib:generateMetadataFileForTestArtifactReleasePublication=SUCCESS",
            ":lib:signTestArtifactReleasePublication=SUCCESS",
            ":lib:publishTestArtifactReleasePublicationToMavenMockRepository=SUCCESS",
            ":lib:jbPublishTestArtifactReleaseToMavenMock=SUCCESS",
            ":lib:jbPublishTestArtifactReleaseToMavenRepository=SUCCESS",
            ":lib:jbPublishToMavenRepository=SUCCESS"
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
            ":lib:stripReleaseDebugSymbols=NO_SOURCE",
            ":lib:copyReleaseJniLibsProjectAndLocalJars=SUCCESS",
            ":lib:processReleaseManifest=SUCCESS",
            ":lib:javaPreCompileRelease=SUCCESS",
            ":lib:mergeReleaseShaders=SUCCESS",
            ":lib:compileReleaseShaders=NO_SOURCE",
            ":lib:generateReleaseAssets=UP_TO_DATE",
            ":lib:packageReleaseAssets=SUCCESS",
            ":lib:packageReleaseRenderscript=NO_SOURCE",
            ":lib:prepareLintJarForPublish=SUCCESS",
            ":lib:processReleaseJavaRes=NO_SOURCE",
            ":lib:generatePomFileForTestArtifactReleasePublication=SUCCESS",
            ":lib:jbDokkaHtmlTestArtifactRelease=SUCCESS",
            ":lib:jbDokkaJarTestArtifactReleaseRelease=SUCCESS",
            ":lib:sourcesJarTestArtifactReleaseRelease=SUCCESS",
            ":lib:generateReleaseRFile=SUCCESS",
            ":lib:compileReleaseKotlin=SUCCESS",
            ":lib:compileReleaseJavaWithJavac=SUCCESS",
            ":lib:extractReleaseAnnotations=SUCCESS",
            ":lib:mergeReleaseGeneratedProguardFiles=SUCCESS",
            ":lib:mergeReleaseConsumerProguardFiles=SUCCESS",
            ":lib:mergeReleaseJavaResource=SUCCESS",
            ":lib:syncReleaseLibJars=SUCCESS",
            ":lib:bundleReleaseAar=SUCCESS",
            ":lib:generateMetadataFileForTestArtifactReleasePublication=SUCCESS",
            ":lib:publishTestArtifactReleasePublicationToMavenMockRepository=SUCCESS",
            ":lib:jbPublishTestArtifactReleaseToMavenMock=SUCCESS",
            ":lib:jbPublishTestArtifactReleaseToMavenRepository=SUCCESS",
            ":lib:jbPublishToMavenRepository=SUCCESS"
        )

        fun commonSetup(coordinate: Coordinate, expectedTaskList: List<String>): DefaultGradleProjectSetup {

            val projectDir = tempDirectory()

            return DefaultGradleProjectSetup(projectDir).apply {

                subProjDirs = arrayOf("lib")
                sourceSetTemplateDirs = arrayOf("functionalTestData/libaar")
                setup()
                mockServers.add(
                    MockMavenRepositoryServer().apply {
                        setUp(listOf(coordinate), "/base")
                    }
                )

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
                    if (coordinate.version.isSnapshot()) {
                        "repository.maven.mock.release" to "fake-url-that-is-not-going-to-work"
                        "repository.maven.mock.snapshot" to mockServers[0].getServerUrl()
                    } else {
                        "repository.maven.mock.release" to mockServers[0].getServerUrl()
                        "repository.maven.mock.snapshot" to "fake-url-that-is-not-going-to-work"
                    }
                    "repository.maven.mock.username" to "username"
                    "repository.maven.mock.password" to "password"
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
                    println(result.tasks.joinToString(",\n") { "\"$it\"" })

                    val patch = DiffUtils.diff(
                        setup.expectedTaskList,
                        result.tasks.map { it.toString() },
                        MyersDiff()
                    )

                    result.tasks.map { it.toString() } shouldBeNoDifference (
                        setup.expectedTaskList except listOf(
                            ":lib:stripReleaseDebugSymbols=NO_SOURCE",
                            ":lib:copyReleaseJniLibsProjectAndLocalJars=SUCCESS",
                            ":lib:generateReleaseRFile=SUCCESS",
                            ":lib:mergeReleaseJavaResource=SUCCESS"
                        )
                        )
                }

                setup.mockServers.forEach { server ->
                    MavenRepoResult(
                        server.collectRequests(),
                        listOf(coordinate),
                        "aar"
                    ) should publishedToMavenRepositoryCompletely()
                }
            }
        }

        context("with variant attached to version") {
            context("to release Maven Repository") {
                val coordinate = Coordinate(
                    "test.group",
                    "test.artifact",
                    "0.1",
                    versionWithVariant = "0.1-release"
                )
                val setup = commonSetup(coordinate, releaseExpectedTaskList)
                setup.writeFile(
                    "${setup.subProjDirs[0]}/build.gradle",
                    commonAndroidGradle(variantMode = "variantWithVersion()", mavenRepo = true)
                )
                testBody(coordinate, setup)
            }
            context("to snapshot Maven Repository") {
                val coordinate = Coordinate(
                    "test.group",
                    "test.artifact",
                    "0.1-SNAPSHOT",
                    versionWithVariant = "0.1-release-SNAPSHOT"
                )
                val setup = commonSetup(coordinate, snapshotExpectedTaskList)
                setup.writeFile(
                    "${setup.subProjDirs[0]}/build.gradle",
                    commonAndroidGradle(variantMode = "variantWithVersion()", mavenRepo = true)
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
                commonAndroidGradle(variantMode = "variantWithArtifactId()", mavenRepo = true)
            )
            testBody(coordinate, setup)
        }
    }
})
