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

package io.hkhc.gradle.test

import io.hkhc.gradle.internal.PLUGIN_ID
import io.hkhc.utils.PropertiesEditor
import io.kotest.assertions.withClue
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.nulls.shouldNotBeNull
import org.gradle.api.GradleException
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import java.io.StringWriter

fun PropertiesEditor.setupKeyStore(baseDir: File) {

    "signing.keyId" to "6B70FAE3"
    "signing.password" to "password"
    "signing.secretKeyRingFile" to File(baseDir, "gnupg/secring.gpg").absolutePath
}

fun PropertiesEditor.setupAndroidProperties() {
    "android.useAndroidX" to "true"
    "android.enableJetifier" to "true"
    "kotlin.code.style" to "official"
}

fun simplePomRoot() =
    """
    licenses:
      - name: Apache-2.0
        dist: repo

    developers:
      - id: test.user
        name: Test User
        email: test.user@mail.com

    scm:
      repoType: github.com
      repoName: test.user/test.repo
    """.trimIndent()

fun simpleSubProj(coordinate: Coordinate) = with(coordinate) {
    """
    group: $group
    artifactId: $artifactId
    version: $version
    description: Test artifact $artifactId
    packaging: jar
    """.trimIndent()
}

fun simplePom(coordinate: Coordinate, variant: String = "", packaging: String = "jar") = with(coordinate) {
    val pom =
        """
        group: $group
        artifactId: $artifactId
        version: $version
        description: Test artifact
        packaging: $packaging
    
        licenses:
          - name: Apache-2.0
            dist: repo
    
        developers:
          - id: test.user
            name: Test User
            email: test.user@mail.com
    
        scm:
          repoType: github.com
          repoName: test.user/test.repo
        """.trimIndent()

    if (variant != "") {
        "variant: $variant\n$pom"
    } else {
        pom
    }
}

fun pluginPom(id: String, className: String): String {
    return """
        plugin:
            id: $id
            displayName: Testing Plugin
            implementationClass: $className
    """.trimIndent()
}

fun pluginPom(coordinate: Coordinate): String {

    @Suppress("MaxLineLength")
    return """
    <?xml version="1.0" encoding="UTF-8"?>
    <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
      <modelVersion>4.0.0</modelVersion>
      <groupId>${coordinate.pluginId}</groupId>
      <artifactId>${coordinate.pluginId}.gradle.plugin</artifactId>
      <version>${coordinate.versionWithVariant}</version>
      <packaging>pom</packaging>
      <name>Testing Plugin</name>
      <dependencies>
        <dependency>
          <groupId>${coordinate.group}</groupId>
          <artifactId>${coordinate.artifactIdWithVariant}</artifactId>
          <version>${coordinate.versionWithVariant}</version>
        </dependency>
      </dependencies>
    </project>
    
    """.trimIndent()
}

// jcenter is required by dokka, mavenCentral alone is not enough

fun buildGradle(maven: Boolean = true, bintray: Boolean = true): String {
    return """
        plugins {
            kotlin("jvm") version "1.3.72"
            `kotlin-dsl`
            id("$PLUGIN_ID")
            id("com.dorongold.task-tree") version "1.5"
        }
        repositories {
            jcenter()
        }
        jarbird {
            ${if (maven) "mavenRepo(\"mock\")" else ""}
            ${if (bintray) "bintray()" else ""}
        }
    """.trimIndent()
}

fun buildGradleCustomBintray(): String {
    return """
        plugins {
            kotlin("jvm") version "1.3.72"
            `kotlin-dsl`
            id("$PLUGIN_ID")
            id("com.dorongold.task-tree") version "1.5"
        }
        repositories {
            jcenter()
        }
        jarbird {
            bintray()
        }
    """.trimIndent()
}

fun buildGradleCustomArtifactrory(): String {
    return """
        plugins {
            kotlin("jvm") version "1.3.72"
            `kotlin-dsl`
            id("$PLUGIN_ID")
            id("com.dorongold.task-tree") version "1.5"
        }
        jarbird {
            bintray()
        }
        repositories {
            jcenter()
        }
    """.trimIndent()
}

// why does org.jetbrains.kotlin:kotlin-compiler-embeddable:1.3.72 cannot be load successfully with mavenCentral() ??
fun buildGradlePlugin(): String {
    return """
        plugins {
            kotlin("jvm") version "1.3.72"
            `kotlin-dsl` 
            id("$PLUGIN_ID")
            id("com.dorongold.task-tree") version "1.5"
        }
        repositories {
            jcenter()
        }
        jarbird {
            pub {
                mavenRepo("mock")
            }
        }
    """.trimIndent()
}

fun commonSetting(): String {

    return """
        pluginManagement {
            repositories {
                mavenLocal()
                gradlePluginPortal()
                mavenCentral()
            }
        }
        include(":lib")
    """.trimIndent()
}

fun commonAndroidRootGradle(): String {

    return """
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
            id '$PLUGIN_ID'
            id 'com.dorongold.task-tree' version '1.5'
        }
        allprojects {
            repositories {
                google()
                jcenter()
            }
        }
    """.trimIndent()
}

fun commonAndroidGradle(variantMode: String = "variantInvisible()", mavenRepo: Boolean = false): String {

    return """
        plugins {
            id 'com.android.library'
            id 'kotlin-android'
            id 'kotlin-android-extensions'
            id '$PLUGIN_ID'
            id 'com.dorongold.task-tree' 
        }


        android {
            compileSdkVersion 29
            buildToolsVersion "29.0.3"
        
            defaultConfig {
                minSdkVersion 21
                targetSdkVersion 29
                versionCode 1
                versionName "1.0a"
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
            
            sourceSets {
                main.java.srcDirs += 'src/main/kotlin'
                release.java.srcDirs += 'src/release/kotlin'
            }
            
        }
        
        jarbird {
            ${if (mavenRepo) "mavenRepo(\"mock\")" else ""}
            bintray()
        }

        android.libraryVariants.configureEach { variant ->
            def variantName = variant.name
            if (variantName == "release") {
                jarbird {
                     pub(variantName) { 
                        ${if (variantMode != "") variantMode else "" } 
                        useGpg = true
                        pubComponent = variantName
                        sourceSets = variant.sourceSets.inject([]) { sets, sourceProvider -> 
                            sets += sourceProvider.javaDirectories  
                            sets += sourceProvider.resourcesDirectories
                        }
                    }
                }
            }
            else if (variantName == "debug") {
            }
            
        }        
    """.trimIndent()
}

fun runTask(
    task: String,
    projectDir: File,
    envs: Map<String, String> = defaultEnvs(
        projectDir
    )
): BuildResult {

    withClue("\"Project directory '$projectDir' shall exist\"") {
        projectDir.shouldExist()
    }
    envs.forEach {
        withClue("Environment Variable '${it.key}' should have non null value") {
            it.value.shouldNotBeNull()
        }
    }

    return GradleRunner.create()
        .withProjectDir(projectDir)
        .withEnvironment(envs)
        .withArguments("--stacktrace", "tasks", "--all", task)
        .withPluginClasspath()
        .forwardOutput()
        .build()
}

@Suppress("SpreadOperator")
fun runTaskWithOutput(
    tasks: Array<String>,
    projectDir: File,
    envs: Map<String, String> = defaultEnvs(projectDir)
): BuildOutput {

    withClue("\"Project directory '$projectDir' shall exist\"") {
        projectDir.shouldExist()
    }
    envs.forEach {
        withClue("Environment Variable '${it.key}' should have non null value") {
            it.value.shouldNotBeNull()
        }
    }

    val stdout = StringWriter()
    val stderr = StringWriter()

    val result = GradleRunner.create()
        .withProjectDir(projectDir)
        .withEnvironment(envs)
        .withArguments("--stacktrace", *tasks)
        .withPluginClasspath()
        .forwardStdOutput(stdout)
        .forwardStdError(stderr)
        .build()

    return BuildOutput(result, stdout.toString(), stderr.toString())
}

fun defaultEnvs(projectDir: File) = mutableMapOf(getTestGradleHomePair(projectDir))

fun getTestGradleHomePair(projectDir: File): Pair<String, String> {
    val path = (
        System.getenv()["GRADLE_USER_HOME"]
            ?: System.getenv()["HOME"] ?: projectDir.absolutePath
        ) + "/.gradle"
    return "GRADLE_USER_HOME" to path
}

fun getTestAndroidSdkHomePair(): Pair<String, String> {
    val path = System.getenv()["ANDROID_HOME"] ?: System.getenv()["ANDROID_SDK_ROOT"]
        ?: throw GradleException(
            "environment variable 'ANDROID_SDK_ROOT' is missed." +
                " It shall contain path to Android SDK"
        )
    return "ANDROID_SDK_ROOT" to path
}
