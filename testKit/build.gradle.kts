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

plugins {
    kotlin("jvm")
    `kotlin-dsl`
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.barfuin.gradle.taskinfo")
    id("io.kotest")
}

tasks {
    withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
        }
//        shouldRunAfter("test")
    }
}

dependencies {

    implementation(project(":jarbird-utils"))

    implementation(project(":gradlePluginBasic"))
    implementation(project(":gradlePluginAndroid"))

    implementation(kotlin("stdlib"))
    implementation("com.google.code.gson:gson:_")
    implementation("io.github.java-diff-utils:java-diff-utils:_")

    Testing.junit

    kotest()

    testImplementation(gradleTestKit())
    testImplementation(Square.OkHttp3.mockWebServer)
    testImplementation("com.squareup.okhttp3:okhttp-tls:_")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:_")
}
