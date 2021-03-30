plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("de.fayard.refreshVersions:refreshVersions:0.9.7")
}
