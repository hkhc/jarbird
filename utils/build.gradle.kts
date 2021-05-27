/*
 * Copyright (c) 2021. Herman Cheung
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

plugins {
    kotlin("jvm")
    `kotlin-dsl`
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.barfuin.gradle.taskinfo")
    id("io.kotest")
    id("io.hkhc.jarbird.bootstrap")
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
        "src/test/kotlin"
    )
    debug = true
    buildUponDefaultConfig = true
    config = files("${project.rootDir}/config/detekt/detekt.yml")
}

ktlint {
    debug.set(false)
    verbose.set(false)
    coloredOutput.set(true)
    reporters {
        setOf(ReporterType.CHECKSTYLE, ReporterType.PLAIN)
    }
}

configurations {
    detekt
}

jarbird {
    mavenLocal()
    mavenCentral()
//    bintray()
    mavenRepo("reposilite")
    artifactory()
    pub {
        useGpg = true
    }
}

dependencies {

    implementation(kotlin("stdlib"))
    implementation("io.github.java-diff-utils:java-diff-utils:_")

    kotest()

}

