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
import io.hkhc.utils.FileTree
import io.hkhc.utils.PropertiesEditor
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File

fun PropertiesEditor.setupKeyStore(baseDir: File) {

    "signing.keyId" to "6B70FAE3"
    "signing.password" to "password"
    "signing.secretKeyRingFile" to File(baseDir, "gnupg/secring.gpg").absolutePath
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
        }
        repositories {
            jcenter()
        }
        jarbird {
            withMavenByProperties("mock")
        }
    """.trimIndent()
}

fun buildGradleCustomBintray(): String {
    return """
        plugins {
            kotlin("jvm") version "1.3.72"
            `kotlin-dsl`
            id("io.hkhc.jarbird")
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
        }
        repositories {
            jcenter()
        }
        jarbird {
            withMavenByProperties("mock")
        }
    """.trimIndent()
}

fun runTask(task: String, projectDir: File): BuildResult {

    val result = GradleRunner.create()
        .withProjectDir(projectDir)
        .withEnvironment(mapOf("GRADLE_USER_HOME" to System.getenv()["HOME"] + "/.gradle"))
        .withArguments("--stacktrace", task)
        .withPluginClasspath()
        .forwardOutput()
        .build()

    // FileTree().dump(projectDir, System.out::println)

    return result
}
