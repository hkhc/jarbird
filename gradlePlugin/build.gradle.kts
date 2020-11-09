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
    // for bintray-gradle-plugin:1.8.5-hkhc
    mavenLocal()
}

plugins {
    kotlin("jvm")
    `kotlin-dsl`
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint") version ktlintVersion
    id("com.dorongold.task-tree") version taskTreeVersion
    id("io.hkhc.jarbird.bootstrap") version "1.0.0"
}

// TODO Simplify functional test creation

sourceSets {
    create("testFunctional") {
        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    }
}

val testFunctionalImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

configurations["testFunctionalRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())

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

    val functionalTestTask = register<Test>("functionalTest") {
        description = "Runs the functional tests."
        group = "verification"
        testClassesDirs = sourceSets["testFunctional"].output.classesDirs
        classpath = sourceSets["testFunctional"].runtimeClasspath
        shouldRunAfter("test")
    }

    functionalTestTask.get().dependsOn(get("pluginUnderTestMetadata"))

    check { dependsOn(get("functionalTest")) }

    /*
    Without this Kotlin generate java 6 bytecode, which is hardly fatal.
    There are multiple KotlinCompile tasks, for main and test source sets
     */
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = freeCompilerArgs.plus("-XXLanguage:+NewInference")
        }
    }

    dokka {
        outputFormat = "html"
        outputDirectory = "$buildDir/dokka"
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

dependencies {

    // TODO extract common dependencies to a separate file

    implementation(gradleApi())
    implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:$bintrayVersion")
    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:$buildInfoVersion")
    implementation("org.yaml:snakeyaml:$snakeYamlVersion")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:$dokkaVersion")
    implementation("com.gradle.publish:plugin-publish-plugin:0.12.0")
//    implementation("")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8") {
        version {
            strictly(kotlinVersion)
        }
    }
    implementation("org.jetbrains.kotlin:kotlin-stdlib") {
        version {
            strictly(kotlinVersion)
        }
    }
    implementation("org.jetbrains.kotlin:kotlin-stdlib-common") {
        version {
            strictly(kotlinVersion)
        }
    }
    implementation("org.jetbrains.kotlin:kotlin-reflect") {
        version {
            strictly(kotlinVersion)
        }
    }

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit5Version")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junit5Version")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")

    testFunctionalImplementation(gradleTestKit())
    testFunctionalImplementation("com.squareup.okhttp3:mockwebserver:$mockWebServerVersion")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion")
}
