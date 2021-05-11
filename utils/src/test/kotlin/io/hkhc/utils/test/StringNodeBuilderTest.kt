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

import io.hkhc.utils.tree.TaskTreeTheme
import io.hkhc.utils.tree.TreeBuilder
import io.hkhc.utils.tree.TreePrinter
import io.hkhc.utils.tree.stringTreeOf
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class StringNodeBuilderTest: FunSpec( {

    test("Single Node") {
        val tree = TreeBuilder<String>().build("Hello") {  }
        TreePrinter(TaskTreeTheme).dumpToString(tree) shouldBe """
            Hello
            
        """.trimIndent()
    }

    test("one child") {
        val tree = TreeBuilder<String>().build("Hello") {
            + "World"
        }
        TreePrinter(TaskTreeTheme).dumpToString(tree) shouldBe """
            Hello
            \--- World
            
        """.trimIndent()
    }

    test("many children") {
        val tree = TreeBuilder<String>().build("Hello") {
            + "World"
            + "Apple"
            + "Banana"
            + "Orange"
        }
        TreePrinter(TaskTreeTheme).dumpToString(tree) shouldBe """
            Hello
            +--- World
            +--- Apple
            +--- Banana
            \--- Orange
            
        """.trimIndent()
    }

    test("multi-level children") {
        val tree = TreeBuilder<String>().build("Hello") {
            + "World"
            "Apple" {
                + "Pineapple"
            }
            + "Banana"
            + "Orange"
        }
        TreePrinter(TaskTreeTheme).dumpToString(tree) shouldBe """
            Hello
            +--- World
            +--- Apple
            |    \--- Pineapple
            +--- Banana
            \--- Orange
            
        """.trimIndent()
    }

    test("multi-level children with root in block") {
        val tree = TreeBuilder<String>().build {
            "Hello" {
                + "World"
                "Apple" {
                    + "Pineapple"
                }
                + "Banana"
                + "Orange"
            }
        }
        TreePrinter(TaskTreeTheme).dumpToString(tree) shouldBe """
            Hello
            +--- World
            +--- Apple
            |    \--- Pineapple
            +--- Banana
            \--- Orange
            
        """.trimIndent()
    }

    test("Tree builder helper") {
        val tree = stringTreeOf {
            "Hello" {
                + "World"
                "Apple" {
                    + "Pineapple"
                }
                + "Banana"
                + "Orange"
            }
        }
        TreePrinter(TaskTreeTheme).dumpToString(tree) shouldBe """
            Hello
            +--- World
            +--- Apple
            |    \--- Pineapple
            +--- Banana
            \--- Orange
            
        """.trimIndent()
    }

    test("Tree builder with bracket operator") {
        val tree = stringTreeOf {
            "Hello" {
                "World"()
                "Apple" {
                    "Pineapple"()
                }
                "Banana"()
                "Orange"()
            }
        }
        TreePrinter(TaskTreeTheme).dumpToString(tree) shouldBe """
            Hello
            +--- World
            +--- Apple
            |    \--- Pineapple
            +--- Banana
            \--- Orange
            
        """.trimIndent()
    }
})
