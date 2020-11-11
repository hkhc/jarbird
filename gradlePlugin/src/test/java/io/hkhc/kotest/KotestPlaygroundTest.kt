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

package io.hkhc.kotest

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class FunSpecTest : FunSpec({

    /* Both context and test trigger beforeTest */
    beforeTest {
        println("before test ${it.description.name}")
    }

    /* Only test trigger beforeEach */
    beforeEach {
        println("before each ${it.description.name}")
    }

    /* Only container trigger beforeContainer */
    beforeContainer {
        println("before container ${it.description.name}")
    }

    beforeSpec {
        println("before spec $it")
    }

    context("Context One") {

        val param = 1

        beforeTest {
            println("before test in Context $param ${it.description.name}")
        }

        test("Test One") {
            withClue("String concatenation") {
                "1" + "0" shouldBe "10"
            }
        }

        test("Test Two") {
            withClue("String concatenation") {
                "1" + "0" shouldBe "10"
            }
        }
    }

    context("Context Two") {

        val param = 2

        beforeTest {
            println("before test in Context $param ${it.description.name}")
        }

        test("Test Three") {
            withClue("String concatenation") {
                "1" + "0" shouldBe "10"
            }
        }
    }
})

class StringSpecTest : StringSpec({

    "Test One" {
        withClue("String concatenation") {
            "1" + "0" shouldBe "10"
        }
    }

    "Test Two" {
        withClue("String concatenation") {
            "1" + "1" shouldBe "10"
        }
    }
})
