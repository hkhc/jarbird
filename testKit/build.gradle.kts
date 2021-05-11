plugins {
    kotlin("jvm")
    `kotlin-dsl`
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.barfuin.gradle.taskinfo")
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

    implementation(project(":jarbird-utils"))

    implementation(project(":gradlePluginBasic"))
    implementation(project(":gradlePluginAndroid"))

    implementation(kotlin("stdlib"))
    implementation("com.google.code.gson:gson:_")
    implementation("io.github.java-diff-utils:java-diff-utils:_")

    Testing.junit

    kotest()

    testImplementation(gradleTestKit())
    testImplementation(Square.OkHttp3.mockWebServer)
    testImplementation("com.squareup.okhttp3:okhttp-tls:_")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:_")
}
