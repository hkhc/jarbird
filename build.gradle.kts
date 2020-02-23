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

import io.hkhc.gradle.simplyPublish
import io.hkhc.gradle.PublishConfig
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("io.hkhc.gradle:simplepublisher:0.1-SNAPSHOT")
    }
}

repositories {
    mavenCentral()
    jcenter()
}

plugins {
    kotlin("jvm") version "1.3.61"
    `kotlin-dsl`
    id("org.jetbrains.dokka") version "0.10.1"
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
    id("io.gitlab.arturbosch.detekt") version "1.5.1"
    `maven-publish`
    signing
    id("com.jfrog.bintray") version "1.8.4"
}

apply(plugin = "io.hkhc.simplepublisher")

val pubConfig = PublishConfig(project)

group = pubConfig.artifactGroup!!
version = pubConfig.artifactVersion!!

tasks {
    dokka {
        outputFormat = "html"
        outputDirectory = "$buildDir/dokka"
    }
}

ktlint {
    debug.set(true)
    verbose.set(true)
    coloredOutput.set(true)
    reporters {
        setOf(ReporterType.CHECKSTYLE, ReporterType.PLAIN)
    }
}

detekt {
    buildUponDefaultConfig = true
    config = files("detekt-config.yml")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

simplyPublish {
    publish()
}

dependencies {

    implementation(kotlin("stdlib-jdk8"))

    implementation(gradleApi())
    implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")
}
