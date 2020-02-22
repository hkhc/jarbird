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
    `maven-publish`
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
val artifactEyeD: String = project.name
val pomDescription: String by project
val pomUrl: String by project

publishing {

    publications {

        create<MavenPublication>("lib") {

            groupId = artifactGroup

            // The default artifactId is project.name
            // artifactId = artifactEyeD
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

dependencies {

    implementation(kotlin("stdlib-jdk8"))

    implementation(gradleApi())
    implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")
}
