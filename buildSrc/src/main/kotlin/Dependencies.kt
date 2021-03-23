import org.gradle.kotlin.dsl.DependencyHandlerScope

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

fun DependencyHandlerScope.kotest(configuration: String = "testImplementation") {

    add(configuration, "org.junit.jupiter:junit-jupiter-api:$junitVersion")
    add(configuration, "org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    add(configuration, "io.kotest:kotest-runner-junit5:$kotestVersion")
    add(configuration, "io.kotest:kotest-assertions-core:$kotestVersion")
    add(configuration, "io.kotest:kotest-property:$kotestVersion")
    add(configuration, "io.mockk:mockk:$mockkVersion")
    add(configuration, "io.github.java-diff-utils:java-diff-utils:$javaDiffUtilsVersion")
}
