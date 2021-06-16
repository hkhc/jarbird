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
    id("io.gitlab.arturbosch.detekt")
    id("io.hkhc.jarbird.bootstrap") version "1.0.0"
}

buildscript {
    repositories {
    }
}

jarbird {
    mavenLocal()
    mavenCentral()
    artifactory("hc")
    signWithKeybox()
    dokkaConfig {
        dokkaSourceSets.forEach {
            it.externalDocumentationLink("https://docs.gradle.org/current/javadoc/")
        }
    }
}

allprojects {
    repositories {
        mavenCentral()
        /* We need this to be in repositories block and not only the pluginManagement block,
         because our plugin code applys other plugins, so that make those dependent plugins
         part of the dependenciies */
        gradlePluginPortal()
        google()
        mavenLocal()
    }
}

dependencies {
}
