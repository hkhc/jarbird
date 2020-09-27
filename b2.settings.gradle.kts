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

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        jcenter()
    }
}

include(":gradlePlugin")
//include(":gradlePlugin", ":simplepublisherTestLib")

//rootProject.name = "simplepublisher"
project(":gradlePlugin").apply {
    name="jarbird-bootstrap"
    buildFileName="b2.build.gradle.kts"
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
