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

class RemoveLineBreakTest : FunSpec({

    test("empty string") {
        "".removeLineBreak() shouldBe ""
    }

    test("single line") {
        "Hello".removeLineBreak() shouldBe "Hello"
    }

    test("simple two lines") {
        "Hello \nWorld".removeLineBreak() shouldBe "Hello World"
    }

    test("multiline string") {
        /* note spaces at end of each line */
        """
            Hello${' '}
            World${' '}
            This${' '}
            is${' '}
        """.trimIndent().removeLineBreak() shouldBe "Hello World This is "
    }

    test("multiline string ensure space") {
        /* note no space at end of each line */
        """
            Hello
            World
            This
            is
        """.trimIndent().removeLineBreak(ensureSpaceWithMerge = true) shouldBe "Hello World This is"
    }
})
