plugins {
    java
    kotlin("jvm") version "1.3.71"
    id("org.jetbrains.dokka") version "0.10.1"
    id("io.hkhc.jarbird")
}

group = "io.hkhc.gradle"
version = "1.0"

repositories {
    mavenCentral()
    jcenter()
}

tasks {
    dokka {
        outputFormat = "html"
        outputDirectory = "$buildDir/dokka"
    }
}


dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
