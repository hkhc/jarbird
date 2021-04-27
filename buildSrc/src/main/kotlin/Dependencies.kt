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

    with(Testing) {
        add(configuration, junit.api)
        add(configuration, junit.engine)
        add(configuration, kotest.runner.junit5)
        add(configuration, kotest.assertions.core)
        add(configuration, kotest.property)
        add(configuration, "io.kotest:kotest-framework-engine-jvm:_")
        add(configuration, mockK)
        add(configuration, "io.github.java-diff-utils:java-diff-utils:_")
    }
}
