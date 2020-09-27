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


plugins {
    kotlin("jvm") version kotlinVersion
}

//plugins {
//    id("io.hkhc.jarbird") version "0.3.3.0"
//}

buildscript {

    repositories {
        jcenter()
        gradlePluginPortal()
    }
    dependencies {
        classpath("io.gitlab.arturbosch.detekt:detekt-cli:1.7.0")
    }
}

//plugins {
//    id("io.gitlab.arturbosch.detekt") version "1.7.0"
//}

//subprojects {
//    detekt {
//        toolVersion = "1.7.0"
//    }
//}
dependencies {
    implementation(kotlin("stdlib-jdk8", kotlinVersion))
}
repositories {
    mavenCentral()
}


