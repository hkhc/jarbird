plugins {
    kotlin("jvm")
    `kotlin-dsl`
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
    id("com.dorongold.task-tree")
    id("io.kotest")
}

tasks {
    withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
        }
//        shouldRunAfter("test")
    }
}

dependencies {

    implementation(project(":gradlePluginBasic"))
    implementation(project(":gradlePluginAndroid"))

    implementation(kotlin("stdlib"))

    Testing.junit

    kotest()

    testImplementation(gradleTestKit())
    testImplementation(Square.OkHttp3.mockWebServer)

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:_")

}

