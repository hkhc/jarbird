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
import io.hkhc.utils.tree.TreePrinter
import io.hkhc.utils.tree.stringTreeOf
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TextTreeTest : StringSpec({

    "Single Node" {
        TreePrinter(TaskTreeTheme).dumpToString(stringTreeOf {
            +"Hello"
        }) shouldBe """
            Hello
        """.trimIndent() + "\n"
    }

    "One Child" {
        TreePrinter(TaskTreeTheme).dumpToString(stringTreeOf {
            "Hello" {
                +"World"
            }
        }) shouldBe """
            Hello
            \--- World
        """.trimIndent() + "\n"
    }

    "Two Children" {
        TreePrinter(TaskTreeTheme).dumpToString(stringTreeOf {
            "Hello" {
                +"World"
                +"Zero"
            }
        }) shouldBe """
            Hello
            +--- World
            \--- Zero
        """.trimIndent() + "\n"
    }


    "Three generation" {
        TreePrinter(TaskTreeTheme).dumpToString(stringTreeOf {
            "Hello" {
                "World" {
                    + "Leaf"
                }
            }
        }) shouldBe """
            Hello
            \--- World
                 \--- Leaf
        """.trimIndent() + "\n"
    }

    "Two children, Three generation" {
        TreePrinter(TaskTreeTheme).dumpToString(stringTreeOf {
            "Hello" {
                "World" {
                    + "Leaf"
                }
                "Zero" {
                    + "Leaf 2"
                }
            }
        }) shouldBe """
            Hello
            +--- World
            |    \--- Leaf
            \--- Zero
                 \--- Leaf 2
             """.trimIndent() + "\n"
    }
})
