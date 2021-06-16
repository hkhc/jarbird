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

import io.hkhc.utils.tree.TreeBuilder
import io.hkhc.utils.tree.TreePrinter
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class AbstractNodeTest : FunSpec({

    context("toString") {
        test("single node toString") {
            "${TreeBuilder<String>().build("hello")}" shouldBe
                "hello\n"
        }
        test("two nodes toString") {
            val node = TreeBuilder<String>().build("hello") {
                "world"()
                "apple"()
            }
            "$node" shouldBe TreePrinter().dumpToString(node)
        }
    }

    context("equals test") {
        test("single node equals") {
            (TreeBuilder<String>().build("hello")) shouldBe
                (TreeBuilder<String>().build("hello"))
        }
        test("single node not equals") {
            (TreeBuilder<String>().build("hello")) shouldNotBe
                (TreeBuilder<String>().build("hello world"))
        }
        test("two nodes equals") {
            (TreeBuilder<String>().build("hello") { "world"() }) shouldBe
                (TreeBuilder<String>().build("hello") { "world"() })
        }
        test("two nodes not equals") {
            (TreeBuilder<String>().build("hello") { "world"() }) shouldNotBe
                (TreeBuilder<String>().build("hello") { "Pluto"() })
        }
    }
})
