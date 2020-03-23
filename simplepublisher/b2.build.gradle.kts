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
This is a bootstrap script to do minimal thing to deploy the plugin to maven local repository, so that
the build.gradle.kts script can make use of itself to do full feature publishing

run the following to publish bootstrap plugin

./gradlew -b b2.build.gradle.kts publishLibToMavenLocal

 */

repositories {
    mavenCentral()
    jcenter()
    gradlePluginPortal()
}

plugins {
    kotlin("jvm") version "1.3.70"
    `kotlin-dsl`
    `maven-publish`
    id("com.dorongold.task-tree") version "1.5"
    id("com.gradle.plugin-publish") version "0.10.1"
}

group = "io.hkhc.gradle"
version = "1.0.0"

tasks {

    create("spPublishToMavenLocal") {
        dependsOn(
            "publishPluginMavenPublicationToMavenLocal",
            "publishSpPluginMarkerMavenPublicationToMavenLocal"
        )
    }
}

gradlePlugin {
    plugins {
        create("sp") {
            id = "io.hkhc.simplepublisher.bootstrap"
            displayName = "Bootstrap plugin for io.hkhc.simplepublisher"
            implementationClass = "io.hkhc.gradle.SimplePublisherPlugin"
        }
    }
}

dependencies {

    // These are the dependencies needed to build the plugin code

    implementation(kotlin("stdlib-jdk8", "1.3.70"))

    // TODO extract common dependencies to a separate file

    implementation(gradleApi())
    implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")
    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:4.13.0")
    implementation("org.yaml:snakeyaml:1.25")
    implementation("com.gradle.publish:plugin-publish-plugin:0.10.1")
}
