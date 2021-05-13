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
        // we need mavenaLocal repo here so that we can get the bootstrap plugin
        mavenLocal()
        // the default place for gradle plugin (marker)
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

buildscript {
    repositories { gradlePluginPortal() }
    dependencies {
        classpath("de.fayard.refreshVersions:refreshVersions:0.9.7")
    }
}

bootstrapRefreshVersions()

// If you want the root project name to be different from the directory name
// rootProject.name = "your-project-name"
include(":gradlePlugin")
include(":gradlePluginAndroid")
include(":gradlePluginBasic")
include(":testKit")
include(":utils")

project(":gradlePlugin").apply {
    name = "jarbird-base"
}

project(":utils").apply {
    name = "jarbird-utils"
}
