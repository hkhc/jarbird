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
        // we need mavenaLocal repo here so that we can get the bootstrap plugin
        mavenLocal()
        gradlePluginPortal()
        // I wonder if we really need these two if we don't use old style plugin declaration at all
        mavenCentral()
        jcenter()
    }
}

include(":gradlePlugin")
//include(":gradlePlugin", ":simplepublisherTestLib")

// If you want the root project name to be different from the directory name
//rootProject.name = "your-project-name"
