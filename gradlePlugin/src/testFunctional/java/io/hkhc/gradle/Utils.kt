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
import io.hkhc.utils.TextTree
import junit.framework.Assert.assertTrue
import org.gradle.api.GradleException
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertNotNull
import java.io.File
import java.io.StringWriter

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



fun taskTree(bintray: Boolean = true, plugin: Boolean = false, snapshot: Boolean = false, publications: List<String> = mutableListOf()): String {

    val taskTree = StringNodeBuilder(":jbPublish").build {
        +":jbPublishLib" {
            +":jbPublishLibToMavenLocal" {
                +":publishLibPluginMarkerMavenPublicationToMavenLocal" {
                    +":generatePomFileForLibPluginMarkerMavenPublication"
                }
                +":publishLibPublicationToMavenLocal" {
                    +":dokkaJar" {
                        +":dokka"
                    }
                    +":generateMetadataFileForLibPublication" {
                        +":jar" {
                            +":classes" {
                                +":compileJava" {
                                    +":compileKotlin"
                                }
                                +":processResources" {
                                    +":pluginDescriptors"
                                }
                            }
                            +":compileKotlin *"
                            +":inspectClassesForKotlinIC" {
                                +":classes *"
                            }
                        }
                    }
                    +":generatePomFileForLibPublication"
                    +":jar *"
                    if (!snapshot) {
                        +":signLibPublication" {
                            +":dokkaJar *"
                            +":generateMetadataFileForLibPublication *"
                            +":generatePomFileForLibPublication *"
                            +":jar *"
                            +":sourcesJar"
                        }
                    }
                    +":sourcesJar *"
                }
            }
            +":jbPublishLibToMavenRepository" {
                +":jbPublishLibToMavenmock" {
                    +":publishLibPluginMarkerMavenPublicationToMavenLibRepository" {
                        +":generatePomFileForLibPluginMarkerMavenPublication *"
                    }
                    +":publishLibPublicationToMavenLibRepository" {
                        +":dokkaJar *"
                        +":generateMetadataFileForLibPublication *"
                        +":generatePomFileForLibPublication *"
                        +":jar *"
                        +":signLibPublication *"
                        +":sourcesJar *"
                    }
                }
            }
        }
        if (bintray) {
            +":jbPublishToBintray" {
                +":bintrayUpload" {
                    +":_bintrayRecordingCopy" {
                        +":publishLibPublicationToMavenLocal *"
                    }
                    +":publishLibPluginMarkerMavenPublicationToMavenLocal *"
                    +":publishLibPublicationToMavenLocal *"
                }
            }
        }
        if (plugin) {
            +":jbPublishToGradlePortal" {
                +":publishPlugins" {
                    +":generatePomFileForPluginMavenPublication"
                    +":jar *"
                    +":publishPluginJar"
                    +":publishPluginJavaDocsJar" {
                        +":javadoc" {
                            +":classes *"
                            +":compileKotlin *"
                        }
                    }
                }
            }
        }
    }

    val taskTreeWriter = StringWriter().also {
        TextTree<String>(TextTree.TaskTreeTheme()).dump(taskTree, { line -> it.write(line + "\n") })
    }

    return taskTreeWriter.toString()
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
