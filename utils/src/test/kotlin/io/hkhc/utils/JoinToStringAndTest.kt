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

package io.hkhc.utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class JoinToStringAndTest : FunSpec({

    test("one item") {
        listOf("Apple").joinToStringAnd() shouldBe "Apple"
        listOf("Apple").joinToStringAnd { it + "X" } shouldBe "AppleX"
    }

    test("two items") {
        listOf("Apple", "Banana").joinToStringAnd() shouldBe "Apple and Banana"
        listOf("Apple", "Banana").joinToStringAnd { it + "X" } shouldBe "AppleX and BananaX"
    }

    test("more items") {
        listOf("Apple", "Banana", "Orange").joinToStringAnd() shouldBe "Apple, Banana and Orange"
        listOf("Apple", "Banana", "Orange").joinToStringAnd { it + "X" } shouldBe "AppleX, BananaX and OrangeX"
    }
})
