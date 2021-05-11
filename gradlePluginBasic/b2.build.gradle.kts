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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
the build.gradle.x.kts script can make use of itself to do full feature publishing

run the following to publish bootstrap plugin

./gradlew -b b2.build.gradle.x.kts publishLibToMavenLocal

 */

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
    google()
    gradlePluginPortal()
}

plugins {
    kotlin("jvm")
    `kotlin-dsl`
    `maven-publish`
    id("io.kotest")
    id("org.barfuin.gradle.taskinfo")
    id("com.gradle.plugin-publish") version "0.14.0"
}

group = "io.hkhc.gradle"
version = "1.0.0"

/*
 It is needed to make sure every version of java compiler to generate same kind of bytecode.
 Without it and build bootstrap with java 8+ compiler, then the Jarbird build with java 8
 will get error like this:
   > Unable to find a matching variant of io.hkhc.gradle:jarbird-bootstrap:1.0.0:
      - Variant 'apiElements' capability io.hkhc.gradle:jarbird-bootstrap:1.0.0:
          - Incompatible attributes:
              - Required org.gradle.jvm.version '8' and found incompatible value '13'.
              - Required org.gradle.usage 'java-runtime' and found incompatible value 'java-api'.
              ...
 */
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val pluginPubName = "jb"

tasks {

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = jvmTargetVersion
        kotlinOptions.languageVersion = kotlinLanguageVersion
    }

    withType<Test> {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
        }
    }

    create("jbPublishToMavenLocal") {
        dependsOn(
            "publishPluginMavenPublicationToMavenLocal",
            "publish${pluginPubName.capitalize()}PluginMarkerMavenPublicationToMavenLocal"
        )
    }
}

gradlePlugin {
    plugins {
        create(pluginPubName) {
            id = "io.hkhc.jarbird.bootstrap"
            displayName = "Bootstrap plugin for io.hkhc.jarbird"
            implementationClass = "io.hkhc.gradle.JarbirdPlugin"
        }
    }
}

dependencies {

    // These are the dependencies needed to build the plugin code

    implementation(project(":jarbird-bootstrap-base"))

    implementation(Kotlin.stdlib.jdk8)

    // TODO extract common dependencies to a separate file

    implementation(gradleApi())

    kotest()
}
