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

package io.hkhc.utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.StringWriter

class DiffPresenterTest : FunSpec({

    test("Two same lists") {
        val listA = listOf(
            "Apple"
        )
        val listB = listOf(
            "Apple"
        )

        val stringWriter = StringWriter()
        DiffPresenter<String>().print(listA, listB, stringWriter)

        stringWriter.toString() shouldBe """
            1   1    Apple
        """.trimIndent() + '\n'
    }

    test("Add two lines") {
        val listA = listOf(
            "Apple",
            "Banana"
        )
        val listB = listOf(
            "Zero",
            "Apple",
            "Banana",
            "Orange"
        )

        val stringWriter = StringWriter()
        DiffPresenter<String>().print(listA, listB, stringWriter)

        stringWriter.toString() shouldBe """
                1  + Zero
            1   2    Apple
            2   3    Banana
                4  + Orange
        """.trimIndent() + '\n'
    }

    test("Remove two lines") {
        val listA = listOf(
            "Zero",
            "Apple",
            "Banana",
            "Orange"
        )
        val listB = listOf(
            "Apple",
            "Banana"
        )

        val stringWriter = StringWriter()
        DiffPresenter<String>().print(listA, listB, stringWriter)

        stringWriter.toString() shouldBe """
            1      - Zero
            2   1    Apple
            3   2    Banana
            4      - Orange
        """.trimIndent() + '\n'
    }

    test("Change a line") {
        val listA = listOf(
            "Zero",
            "Apple",
            "Banana",
            "Orange"
        )
        val listB = listOf(
            "Zero",
            "Raw Apple",
            "Banana",
            "Orange"
        )

        val stringWriter = StringWriter()
        DiffPresenter<String>().print(listA, listB, stringWriter)

        stringWriter.toString() shouldBe """
            1   1    Zero
            2      - Apple
                2  + Raw Apple
            3   3    Banana
            4   4    Orange
        """.trimIndent() + '\n'
    }

    test("Change two lines") {
        val listA = listOf(
            "Zero",
            "Apple",
            "Banana",
            "Orange"
        )
        val listB = listOf(
            "Zero",
            "Raw Apple X",
            "Banana Top",
            "Orange"
        )

        val stringWriter = StringWriter()
        DiffPresenter<String>().print(listA, listB, stringWriter)

        stringWriter.toString() shouldBe """
            1   1    Zero
            2      - Apple
            3      - Banana
                2  + Raw Apple X
                3  + Banana Top
            4   4    Orange
        """.trimIndent() + '\n'
    }
})
