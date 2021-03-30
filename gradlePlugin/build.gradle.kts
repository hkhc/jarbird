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

import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

repositories {
    mavenCentral()
    /* We need this to be in repositories block and not only the pluginManagement block,
     because our plugin code applys other plugins, so that make those dependent plugins
     part of the dependenciies */
    gradlePluginPortal()
    google()
    mavenLocal()
}

plugins {
    kotlin("jvm")
    `kotlin-dsl`
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
    id("com.dorongold.task-tree")
    id("io.kotest")
    id("io.hkhc.jarbird.bootstrap")
}

// TODO Simplify functional test creation

// e.g. 14 at https://docs.gradle.org/6.7.1/userguide/java_testing.html#sec:configuring_java_integration_tests
sourceSets {
    create("testFunctional") {
        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    }
}

val testFunctionalImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

configurations["testFunctionalRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())

tasks {

    // e.g. 15 at https://docs.gradle.org/6.7.1/userguide/java_testing.html#sec:configuring_java_integration_tests
    val functionalTestTask = register<Test>("testFunctional") {
        description = "Runs the functional tests."
        group = "verification"
        testClassesDirs = sourceSets["testFunctional"].output.classesDirs
        classpath = sourceSets["testFunctional"].runtimeClasspath
        shouldRunAfter("test")
    }

    functionalTestTask.get().dependsOn(get("pluginUnderTestMetadata"))

    check { dependsOn(get("testFunctional")) }
}

/*
 It is needed to make sure every version of java compiler to generate same kind of bytecode.
 Without it and build this with java 8+ compiler, then the project build with java 8
 will get error like this:
   > Unable to find a matching variant of <your-artifact>:
      - Variant 'apiElements' capability <your-artifact>:
          - Incompatible attributes:
              - Required org.gradle.jvm.version '8' and found incompatible value '13'.
              - Required org.gradle.usage 'java-runtime' and found incompatible value 'java-api'.
              ...
 */
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {

    /*
    Without this Kotlin generate java 6 bytecode, which is hardly fatal.
    There are multiple KotlinCompile tasks, for main and test source sets
     */
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = jvmTargetVersion
            languageVersion = kotlinLanguageVersion
        }
    }

    withType<Detekt>().configureEach {
        jvmTarget = jvmTargetVersion
        languageVersion = kotlinLanguageVersion
    }

    withType<Test> {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
        }
    }
}

detekt {
    input = files(
        "src/main/java",
        "src/main/kotlin",
        "src/test/java",
        "src/test/kotlin",
        "src/testFunctional/java",
        "src/testFunctional/kotlin"
    )
    debug = true
    buildUponDefaultConfig = true
    config = files("${project.projectDir}/config/detekt/detekt.yml")
}

ktlint {
    debug.set(false)
    verbose.set(false)
    coloredOutput.set(true)
    reporters {
        setOf(ReporterType.CHECKSTYLE, ReporterType.PLAIN)
    }
}

gradlePlugin {
    pluginSourceSet(sourceSets["main"])
    testSourceSets(sourceSets["testFunctional"])
}

configurations {
    detekt
}

jarbird {
    mavenLocal()
    mavenCentral()
    bintray()
    pub {
        useGpg = true
    }
}

dependencies {

    // TODO extract common dependencies to a separate file

    implementation(gradleApi())
    implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:_")
    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:_")
    implementation("org.yaml:snakeyaml:_")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:_")
    implementation("org.jetbrains.dokka:dokka-core:_")
    implementation("org.jetbrains.dokka:dokka-base:_")
    implementation("com.gradle.publish:plugin-publish-plugin:_")

    Testing.junit
    implementation("com.android.tools.build:gradle:_")

    kotest()

    testFunctionalImplementation(gradleTestKit())
    testFunctionalImplementation(Square.OkHttp3.mockWebServer)

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:_")
}
