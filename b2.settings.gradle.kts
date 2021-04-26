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

import de.fayard.refreshVersions.bootstrapRefreshVersions

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        jcenter()
    }
}

buildscript {
    repositories { gradlePluginPortal() }
    dependencies {
        classpath("de.fayard.refreshVersions:refreshVersions:0.9.7")
    }
}

bootstrapRefreshVersions()

include(":gradlePlugin")
include(":gradlePluginBasic")


rootProject.buildFileName = "b2.build.gradle.kts"

//rootProject.name = "simplepublisher"
project(":gradlePluginBasic").apply {
    name = "jarbird-bootstrap"
    buildFileName = "b2.build.gradle.kts"
    /*
        There is not any error message when the file name of buildFileName is not valid.
        So we check it by ourself.
     */
    File("$projectDir${File.separatorChar}$buildFileName").also { file ->
        if (!file.exists()) {
            throw GradleException("Build file '${file.absolutePath}' does not exist")
        }
    }
}

//rootProject.name = "simplepublisher"
project(":gradlePlugin").apply {
    name = "jarbird-bootstrap-base"
    buildFileName = "b2.build.gradle.kts"
    /*
        There is not any error message when the file name of buildFileName is not valid.
        So we check it by ourself.
     */
    File("$projectDir${File.separatorChar}$buildFileName").also { file ->
        if (!file.exists()) {
            throw GradleException("Build file '${file.absolutePath}' does not exist")
        }
    }
}

