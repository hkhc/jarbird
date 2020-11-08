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

import io.hkhc.gradle.test.ArtifactoryPublishingChecker
import io.hkhc.gradle.test.Coordinate
import io.hkhc.gradle.test.MockArtifactoryRepositoryServer
import io.hkhc.utils.FileTree
import io.hkhc.utils.PropertiesEditor
import io.hkhc.utils.StringNodeBuilder
import io.hkhc.utils.TextCutter
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class BuildAndroidArtifactoryTest {

    // https://www.baeldung.com/junit-5-temporary-directory
    @TempDir
    lateinit var tempProjectDir: File
    lateinit var mockRepositoryServer: MockArtifactoryRepositoryServer
    lateinit var libProj: File

    lateinit var localRepoDir: File
    lateinit var envs: MutableMap<String, String>

    @BeforeEach
    fun setUp() {

        mockRepositoryServer = MockArtifactoryRepositoryServer()

        envs = defaultEnvs(tempProjectDir).apply {
            val pair = getTestAndroidSdkHomePair()
            put(pair.first, pair.second)
        }

        libProj = File(tempProjectDir, "lib")
        File("functionalTestData/keystore").copyRecursively(tempProjectDir)
        File("functionalTestData/libaar").copyRecursively(libProj)
        localRepoDir = File(tempProjectDir, "localRepo")
        localRepoDir.mkdirs()
        System.setProperty("maven.repo.local", localRepoDir.absolutePath)
    }

    @AfterEach
    fun teardown() {
        mockRepositoryServer.teardown()
    }

    @Test
    fun `Normal publish Android AAR to Artifactory Repository`() {

        val coordinate = Coordinate("test.group", "test.artifact", "0.1-SNAPSHOT", versionWithVariant = "0.1-release-SNAPSHOT")
        mockRepositoryServer.setUp(coordinate, "/base")

        File("$tempProjectDir/settings.gradle").writeText(
            """
            pluginManagement {
                repositories {
                    mavenLocal()
                    gradlePluginPortal()
                    mavenCentral()
                }
            }
            include(":lib")
            """.trimIndent()
        )

        File("$tempProjectDir/build.gradle").writeText(
            """
            buildscript {
                ext.kotlin_version = "1.3.72"
                repositories {
                    mavenLocal()
                    google()
                    jcenter()
                }
                dependencies {
                    classpath "com.android.tools.build:gradle:4.0.0"
                    classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:${'$'}kotlin_version"
            
                    // NOTE: Do not place your application dependencies here; they belong
                    // in the individual module build.gradle files
                }
            }
            plugins {
                id 'io.hkhc.jarbird'
                id 'com.dorongold.task-tree' version '1.5'
            }
            allprojects {
                repositories {
                    google()
                    jcenter()
                }
            }
            """.trimIndent()
        )

        File("$libProj/build.gradle").writeText(
            """
                plugins {
                    id 'com.android.library'
                    id 'kotlin-android'
                    id 'kotlin-android-extensions'
                    id 'io.hkhc.jarbird'
                    id 'com.dorongold.task-tree' 
                }

                sourceSets {
                    main {
                        java.srcDirs("src/main/java", "src/main/kotlin")
                    }
                    release {
                        java.srcDirs("src/release/java", "src/release/kotlin")
                    }
                }

                android {
                    compileSdkVersion 29
                    buildToolsVersion "29.0.3"
                
                    defaultConfig {
                        minSdkVersion 21
                        targetSdkVersion 29
                        versionCode 1
                        versionName "1.0"
                
                        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
                        consumerProguardFiles "consumer-rules.pro"
                    }
                
                    buildTypes {
                        release {
                            minifyEnabled false
                            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
                        }
                    }
                    compileOptions {
                        sourceCompatibility = JavaVersion.VERSION_1_8
                        targetCompatibility = JavaVersion.VERSION_1_8
                    }
                }

                android.libraryVariants.configureEach {
                    def variantName = name
                    if (variantName == "release") {
                        jarbird {
                             pub(variantName) {
                                withMavenByProperties("mock")
                                versionWithVariant = true
                                useGpg = true
                                pubComponent = variantName
                                sourceSets = sourceSets[0].javaDirectories
                            }
                        }
                    }
                }
            """.trimIndent()
        )

        File("$libProj/pom.yaml")
            .writeText("variant: release\n" + simpleAndroidPom(coordinate))

        val username = "username"
        val repo = "maven"

        PropertiesEditor("$tempProjectDir/gradle.properties") {
            setupKeyStore(tempProjectDir)
            "repository.bintray.snapshot" to mockRepositoryServer.getServerUrl()
            "repository.bintray.username" to username
            "repository.bintray.apikey" to "password"
            "android.useAndroidX" to "true"
            "android.enableJetifier" to "true"
            "kotlin.code.style" to "official"
        }

        val targetTask = "lib:jbPublishToBintray"

        val taskTree = treeStr(
            StringNodeBuilder(":$targetTask").build {
                +":lib:artifactoryPublish" {
                    +":lib:bundleReleaseAar ..>"
                    +":lib:dokkaJarRelease ..>"
                    +":lib:generateMetadataFileForLibReleasePublication ..>"
                    +":lib:generatePomFileForLibReleasePublication"
                    +":lib:sourcesJarRelease"
                }
            }
        )

        val output = runTaskWithOutput(arrayOf(targetTask, "taskTree", "--task-depth", "2"), tempProjectDir, envs)
        Assertions.assertEquals(
            taskTree,
            TextCutter(output.stdout).cut(":$targetTask", ""), "task tree"
        )

        FileTree().dump(tempProjectDir, ::println)

        val result = runTask(targetTask, tempProjectDir, envs)

        FileTree().dump(tempProjectDir, ::println)

        // FileTree().dump(tempProjectDir, System.out::println)

        Assertions.assertEquals(TaskOutcome.SUCCESS, result.task(":$targetTask")?.outcome)
        ArtifactoryPublishingChecker(coordinate, "aar").assertReleaseArtifacts(
            mockRepositoryServer.collectRequests().apply {
                forEach {
                    println("recorded request ${it.method} ${it.path}")
                }
            },
            username,
            repo
        )
    }
}
