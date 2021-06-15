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

package io.hkhc.utils.test

import io.hkhc.utils.tree.stringTreeOf
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class NodeTest : FunSpec({

    test("one node equality") {

        (stringTreeOf { "Hello"() } == stringTreeOf { "Hello"() }) shouldBe true
        (stringTreeOf { "Hello"() } == stringTreeOf { "World"() }) shouldBe false
    }

    test("two node equality") {

        (stringTreeOf { "Hello" { "World"() } } == stringTreeOf { "Hello" { "World"() } }) shouldBe true
        (stringTreeOf { "Hello" { "World"() } } == stringTreeOf { "Hello" { "Banana"() } }) shouldBe false
    }

    test("three node equality") {

        val n1 = stringTreeOf {
            "Hello" {
                "World" {
                    "Apple"()
                }
            }
        }

        val n2 = stringTreeOf {
            "Hello" {
                "World" {
                    "Apple"()
                }
            }
        }

        val n3 = stringTreeOf {
            "Hello" {
                "World" {
                    "Banana"()
                }
            }
        }

        (n1 == n2) shouldBe true
        (n1 == n3) shouldBe false
    }
})
