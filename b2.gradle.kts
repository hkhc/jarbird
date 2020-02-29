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

/*
This is a bootstrap script to do minimal thing to deploy the plugin to mavel local repository, so that
the build.gradle.kts script can make use of itself to do full feature publishing

run the following to publish bootstrap plugin

./gradlew -b b2.gradle.kts publishLibToMavenLocal

 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
        jcenter()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
}

repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
}

plugins {
    kotlin("jvm") version "1.3.61"
    `kotlin-dsl`
    kotlin("plugin.serialization") version "1.3.61"
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.10.1"
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val artifactGroup: String by project
val artifactVersion: String by project
val artifactId: String by project
val artifactEyeD = artifactId
val pomDescription: String by project
val pomUrl: String by project

group = artifactGroup
version = artifactVersion

publishing {

    publications {

        create<MavenPublication>("lib") {

            groupId = artifactGroup

            // The default artifactId is project.name
            artifactId = artifactEyeD
            // version is gotten from an external plugin
            //            version = project.versioning.info.display
            version = artifactVersion
            // This is the main artifact
            from(project.components["java"])

            pom {
                name.set(artifactEyeD)
                description.set(pomDescription)
                url.set(pomUrl)
            }
        }
    }
}

gradlePlugin {
    plugins {
        create("simplepublisher") {
            id = "io.hkhc.simplepublisher"
            displayName = "Plugin to make publishing artifacts easy."
            description = "Wrapping build script for major repositories and make simple things as simple as possible"
            implementationClass = "io.hkhc.gradle.SimplePublisherPlugin"
        }
    }
}


dependencies {

    implementation(kotlin("stdlib-jdk8"))

    implementation(gradleApi())
    implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")
    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:4.13.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.14.0")
    implementation("com.charleskorn.kaml:kaml:0.15.0")
}
