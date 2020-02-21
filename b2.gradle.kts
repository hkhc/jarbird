import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
        jcenter()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
}

repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
}


plugins {
    kotlin("jvm") version "1.3.61"
    `kotlin-dsl`
    `maven-publish`
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

sourceSets{
    main {
        java {
            srcDirs("src/java")
        }
        resources {
            srcDirs("src/resources")
        }
    }
    test {
        java {
            srcDirs("src/test")
        }
    }
}

val artifactGroup: String by project
val artifactVersion: String by project
val artifactEyeD: String = project.name
val pomDescription: String by project
val pomUrl: String by project

publishing {

    publications {

        create<MavenPublication>("lib") {

            groupId = artifactGroup

            // The default artifactId is project.name
            // artifactId = artifactEyeD
            // version is gotten from an external plugin
            //            version = project.versioning.info.display
            version = artifactVersion
            // This is the main artifact
            from(project.components["java"])

            pom {
                name.set(artifactEyeD)
                description.set(pomDescription)
                url.set(pomUrl)
            }

        }
    }

}

dependencies {

    implementation(kotlin("stdlib-jdk8"))

    implementation(gradleApi())
    implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")

}
