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

package io.hkhc.gradle.test

import io.hkhc.gradle.internal.PLUGIN_ID

open class Versions {
    open val kotlin = "1.3.72"
    open val taskInfo = "1.0.5"
    open val pluginId = PLUGIN_ID
}

open class AndroidVersions : Versions() {
    override val pluginId = "io.hkhc.jarbird-android"
    open val androidTool = "4.1.3"
}

fun commonBuildGradleKtsPlugins(versions: Versions = Versions()): String {
    // it is included in another multi-line string, don't do trimIndent() here
    return """
        plugins {
            kotlin("jvm") version "${versions.kotlin}"
            `kotlin-dsl`
            id("${versions.pluginId}")
            id("org.barfuin.gradle.taskinfo") version "${versions.taskInfo}"
        }
    """
}

fun commonBuildGradlePlugins(versions: Versions = Versions()): String {
    // it is included in another multi-line string, don't do trimIndent() here
    return """
        plugins {
            id 'java'
            id 'org.jetbrains.kotlin.jvm' version '${versions.kotlin}'
            id 'io.hkhc.jarbird'
            id 'org.barfuin.gradle.taskinfo' version '${versions.taskInfo}'
        }
    """
}

fun commonAndroidBuildGradleKtsPlugins(versions: Versions = Versions()): String {
    // it is included in another multi-line string, don't do trimIndent() here
    return """
        plugins {
            id 'com.android.library'
            id 'kotlin-android'
            id '${versions.pluginId}'
            id 'org.barfuin.gradle.taskinfo' version "${versions.taskInfo}"
        }
    """
}

fun commonAndroidBuildGradleKtsAndroid(): String {
    // it is included in another multi-line string, don't do trimIndent() here
    return """
        android {
            compileSdkVersion 29
            buildToolsVersion "29.0.3"
        
            defaultConfig {
                minSdkVersion 21
                targetSdkVersion 29
                versionCode 1
                versionName "1.0a"
                testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
                consumerProguardFiles "consumer-rules.pro"
            }
        
            buildTypes {
                release {
                    minifyEnabled false
                    proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
                }
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
            }
            
            sourceSets {
                main.java.srcDirs += 'src/main/kotlin'
                release.java.srcDirs += 'src/release/kotlin'
            }
            
        }        
    """
}

fun buildGradle(maven: Boolean = true, bintray: Boolean = true, versions: Versions = Versions()): String {
    return """
        ${commonBuildGradlePlugins(versions)}
        repositories {
            jcenter()
        }
        dependencies {
            implementation "org.jetbrains.kotlin:kotlin-stdlib"
        }
    """.trimIndent()
}

fun buildGradleKts(maven: Boolean = true, artifactory: Boolean = true, versions: Versions = Versions()): String {
    return """
        ${commonBuildGradleKtsPlugins(versions)}
        repositories {
            jcenter()
        }
        jarbird {
            ${if (maven) "mavenRepo(\"mock\")" else ""}
            ${if (artifactory) "artifactory(\"mock\")" else ""}
        }
    """.trimIndent()
}

fun buildTwoPubGradleKts(maven: Boolean = true, artifactory: Boolean = true, versions: Versions = Versions()): String {
    return """
        ${commonBuildGradleKtsPlugins(versions)}
        repositories {
            jcenter()
        }
        sourceSets {
            // source set main is implicit
            create("main2") {
                compileClasspath += sourceSets.main.get().output 
                runtimeClasspath += sourceSets.main.get().output 
            }
        }
        
        jarbird {
            ${if (maven) "mavenRepo(\"mock\")" else ""}
            ${if (artifactory) "artifactory()" else ""}
            pub("lib1") {
                 from(sourceSets["main"])
            }
            pub("lib2") {
                from(sourceSets["main2"])
            }
        }
    """.trimIndent()
}

fun buildTwoGlobalGradleKts(versions: Versions = Versions()): String {
    return """
        ${commonBuildGradleKtsPlugins(versions)}
        sourceSets {
            create("sourceSet1") { }
            create("sourceSet2") { }
        }
        repositories {
            jcenter()
        }
        jarbird {
            mavenRepo("mock1")
            mavenRepo("mock2")
            pub("lib1") {
                from(sourceSets["sourceSet1"])
            }
            pub("lib2") {
                from(sourceSets["sourceSet2"])
            }
        }
    """.trimIndent()
}

fun buildTwoLocalGradleKts(versions: Versions = Versions()): String {
    return """
        ${commonBuildGradleKtsPlugins(versions)}
        sourceSets {
            create("sourceSet1") { }
            create("sourceSet2") { }
        }
        repositories {
            jcenter()
        }
        jarbird {
            pub("lib1") {
                mavenRepo("mock1")
                from(sourceSets["sourceSet1"])
            }
            pub("lib2") {
                mavenRepo("mock2")
                from(sourceSets["sourceSet2"])
            }
        }
    """.trimIndent()
}

fun buildGradleCustomArtifactoryKts(versions: Versions = Versions()): String {
    return """
        ${commonBuildGradleKtsPlugins(versions)}
        jarbird {
            artifactory("mock")
        }
        repositories {
            jcenter()
        }
    """.trimIndent()
}

// why does org.jetbrains.kotlin:kotlin-compiler-embeddable:1.3.72 cannot be load successfully with mavenCentral() ??
fun buildGradlePluginKts(versions: Versions = Versions()): String {
    return """
        ${commonBuildGradleKtsPlugins(versions)}
        repositories {
            jcenter()
        }
        jarbird {
            pub {
                mavenRepo("mock")
            }
        }
    """.trimIndent()
}

// why does org.jetbrains.kotlin:kotlin-compiler-embeddable:1.3.72 cannot be load successfully with mavenCentral() ??
fun buildGradlePortalPluginKts(versions: Versions = Versions()): String {
    return """
        ${commonBuildGradleKtsPlugins(versions)}
        repositories {
            jcenter()
        }
        jarbird {
            pub {
                gradlePortal()
            }
        }
        dependencies {
            implementation(gradleApi())
        }
    """.trimIndent()
}

fun commonAndroidRootGradle(versions: AndroidVersions = AndroidVersions()): String {

    return """
        buildscript {
            ext.kotlin_version = "${versions.kotlin}"
            repositories {
                mavenLocal()
                google()
                jcenter()
            }
            dependencies {
                classpath "com.android.tools.build:gradle:${versions.androidTool}"
                classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:${'$'}kotlin_version"
        
                // NOTE: Do not place your application dependencies here; they belong
                // in the individual module build.gradle files
            }
        }
        plugins {
            id '${versions.pluginId}'
        }
        jarbird {
            mavenRepo("mock")
        }
        allprojects {
            repositories {
                google()
                jcenter()
            }
        }
    """.trimIndent()
}

fun commonAndroidGradle(
    variantMode: String = "variantInvisible()",
    mavenRepo: Boolean = false,
    versions: AndroidVersions = AndroidVersions()
): String {

    return """
        ${commonAndroidBuildGradleKtsPlugins(versions)}
        ${commonAndroidBuildGradleKtsAndroid()}
        jarbird {
            ${if (mavenRepo) "mavenRepo(\"mock\")" else ""}
            artifactory("mock")
        }

        android.libraryVariants.configureEach { variant ->
            def variantName = variant.name
            if (variantName == "release") {
                jarbird {
                     pub(variantName) { 
                        ${if (variantMode != "") variantMode else "" } 
                        signWithKeybox()
                        from(variant)
                    }
                }
            }
            else if (variantName == "debug") {
            }
            
        }        
        dependencies {
            implementation "org.jetbrains.kotlin:kotlin-stdlib:${versions.kotlin}"
        }
    """.trimIndent()
}

fun commonAndroidExtGradle(variantMode: String = "variantInvisible()", mavenRepo: Boolean = false, versions: AndroidVersions = AndroidVersions()): String {

    return """
        ${commonAndroidBuildGradleKtsPlugins(versions)}
        ${commonAndroidBuildGradleKtsAndroid()}
        jarbird {
            ${if (mavenRepo) "mavenRepo(\"mock\")" else ""}
            artifactory("mock")
        }

        android.libraryVariants.configureEach { variant ->
            def variantName = variant.name
            if (variantName == "release") {
                jarbird {
                     pub(variantName) { 
                        ${if (variantMode != "") variantMode else "" } 
                        signWithKeybox()
                        from(variant)
                    }
                }
            }
            else if (variantName == "debug") {
            }
            
        }        
        dependencies {
            implementation "org.jetbrains.kotlin:kotlin-stdlib:${versions.kotlin}"
        }
    """.trimIndent()
}
