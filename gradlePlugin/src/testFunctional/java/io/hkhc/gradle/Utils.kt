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
import io.hkhc.utils.PropertiesEditor
import io.hkhc.utils.StringNodeBuilder
import io.hkhc.utils.TextCutter
import io.hkhc.utils.TextTree
import junit.framework.Assert.assertTrue
import org.gradle.api.GradleException
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNotNull
import java.io.File
import java.io.StringWriter

fun PropertiesEditor.setupKeyStore(baseDir: File) {

    "signing.keyId" to "6B70FAE3"
    "signing.password" to "password"
    "signing.secretKeyRingFile" to File(baseDir, "gnupg/secring.gpg").absolutePath
}

fun PropertiesEditor.setupAndroidProeprties() {
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

fun simplePom(coordinate: Coordinate) = with(coordinate) {
    """
    group: $group
    artifactId: $artifactId
    version: $version
    description: Test artifact
    packaging: jar

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
}

fun simpleAndroidPom(coordinate: Coordinate) = with(coordinate) {
    """
    group: $group
    artifactId: $artifactId
    version: $version
    description: Test artifact
    packaging: aar

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
}

fun pluginPom(id: String, className: String): String {
    return """
        plugin:
            id: $id
            displayName: Testing Plugin
            implementationClass: $className
    """.trimIndent()
}

// jcenter is required by dokka, mavenCentral alone is not enough

fun buildGradle(): String {
    return """
        plugins {
            kotlin("jvm") version "1.3.72"
            `kotlin-dsl`
            id("io.hkhc.jarbird")
            id("com.dorongold.task-tree") version "1.5"
        }
        repositories {
            jcenter()
        }
        jarbird {
            pub {
                withMavenByProperties("mock")
            }
        }
    """.trimIndent()
}

fun buildGradleCustomBintray(): String {
    return """
        plugins {
            kotlin("jvm") version "1.3.72"
            `kotlin-dsl`
            id("io.hkhc.jarbird")
            id("com.dorongold.task-tree") version "1.5"
        }
        repositories {
            jcenter()
        }
    """.trimIndent()
}

fun buildGradleCustomArtifactrory(): String {
    return """
        plugins {
            kotlin("jvm") version "1.3.72"
            `kotlin-dsl`
            id("io.hkhc.jarbird")
            id("com.dorongold.task-tree") version "1.5"
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
            id("io.hkhc.jarbird")
            id("com.dorongold.task-tree") version "1.5"
        }
        repositories {
            jcenter()
        }
        jarbird {
            pub {
                withMavenByProperties("mock")
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
}

fun commonAndroidGradle(mavenRepo: String = ""): String {

    return """
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
        }

        android.libraryVariants.configureEach { v ->
            def variantName = v.name
            if (variantName == "release") {
                jarbird {
                     pub(variantName) { 
                        ${if (mavenRepo=="") "withMavenByProperties(\"mock\")" else ""}
                        versionWithVariant = true
                        useGpg = true
                        pubComponent = variantName
//                                sourceSets = sourceSets[0].javaDirectories
                    }
                }
            }
        }
    """.trimIndent()
}

fun <T> treeStr(node: TextTree.Node<T>): String {
    val treeWriter = StringWriter().also {
        TextTree<String>(TextTree.TaskTreeTheme()).dump(node, { line -> it.write(line + "\n") })
    }
    return treeWriter.toString()
}

fun runTask(task: String, projectDir: File, envs: Map<String, String> = defaultEnvs(projectDir)): BuildResult {

    assertTrue("Project directory '$projectDir' shall exist", projectDir.exists())
    envs.forEach {
        assertNotNull("Environment Variable '${it.key}' should have non null value", it.value)
    }

    val result = GradleRunner.create()
        .withProjectDir(projectDir)
        .withEnvironment(envs)
        .withArguments("--stacktrace", "tasks", "--all", task)
        .withPluginClasspath()
        .forwardOutput()
        .build()

    // FileTree().dump(projectDir, System.out::println)

    return result
}

fun runTaskWithOutput(tasks: Array<String>, projectDir: File, envs: Map<String, String> = defaultEnvs(projectDir)): BuildOutput {

    assertTrue("Project directory '$projectDir' shall exist", projectDir.exists())
    envs.forEach {
        assertNotNull("Environment Variable '${it.key}' should have non null value", it.value)
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

fun assertTaskTree(taskName: String, expectedTree: String, taskDepth: Int, projectDir: File, envs: Map<String, String> = defaultEnvs(projectDir)) {

    val output = runTaskWithOutput(arrayOf(taskName, "taskTree", "--task-depth", "$taskDepth"), projectDir, envs)
    Assertions.assertEquals(
        expectedTree,
        TextCutter(output.stdout).cut(":$taskName", ""), "task tree"
    )
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
    if (path == null) {
        throw GradleException("environment variable 'ANDROID_SDK_ROOT' is missed. It shall contain path to Android SDK")
    }
    return "ANDROID_SDK_ROOT" to path
}
