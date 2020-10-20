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

import io.hkhc.gradle.test.ArtifactChecker
import io.hkhc.gradle.test.Coordinate
import io.hkhc.utils.FileTree
import io.hkhc.utils.PropertiesEditor
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class BuildAndroidMavenLocalTest {

    // https://www.baeldung.com/junit-5-temporary-directory
    @TempDir
    lateinit var tempProjectDir: File
    lateinit var libProj: File

    lateinit var localRepoDir: File

    @BeforeEach
    fun setUp() {
        libProj = File(tempProjectDir, "lib")
        File("functionalTestData/keystore").copyRecursively(tempProjectDir)
        File("functionalTestData/libaar").copyRecursively(libProj)
        localRepoDir = File(tempProjectDir, "localRepo")
        localRepoDir.mkdirs()
        System.setProperty("maven.repo.local", localRepoDir.absolutePath)
    }

    @Test
    fun `Normal publish Android AAR to Maven Local`() {

        val coordinate = Coordinate("test.group", "test.artifact", "0.1")

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
                            pub {
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
            .writeText(simpleAndroidPom(coordinate))

        PropertiesEditor("$tempProjectDir/gradle.properties") {
            setupKeyStore(tempProjectDir)
            "android.useAndroidX" to "true"
            "android.enableJetifier" to "true"
            "kotlin.code.style" to "official"
        }

        val task = "lib:jbPublishToMavenLocal"

        FileTree().dump(tempProjectDir, ::println)

        val result = runTask(task, tempProjectDir)

        FileTree().dump(tempProjectDir, ::println)

        // FileTree().dump(tempProjectDir, System.out::println)

        Assertions.assertEquals(TaskOutcome.SUCCESS, result.task(":$task")?.outcome)
        ArtifactChecker()
            .verifyRepository(localRepoDir, coordinate, "aar")
    }
}
