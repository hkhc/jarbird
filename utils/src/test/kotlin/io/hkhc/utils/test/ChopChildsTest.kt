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

import io.hkhc.utils.tree.TreePrinter
import io.hkhc.utils.tree.chopChilds
import io.hkhc.utils.tree.stringTreeOf
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ChopChildsTest : FunSpec({

    test("single node") {

        TreePrinter().dumpToString(
            stringTreeOf {
                "Hello"()
            }.chopChilds { it.text() == "Hello" }
        ) shouldBe
        TreePrinter().dumpToString(
            stringTreeOf {
                "Hello"()
            }
        )
    }

    test("two node and chop nothing") {

        TreePrinter().dumpToString(
            stringTreeOf {
                "Hello" {
                    "World"()
                }
            }.chopChilds { it.text() == "Hellox" }
        ) shouldBe
            TreePrinter().dumpToString(
                stringTreeOf {
                    "Hello" {
                        "World"()
                    }
                }
            )
    }
})
