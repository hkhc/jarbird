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

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.StringWriter

class TextTreeTest : StringSpec({

    "Single Node" {
        val nodes = StringNodeBuilder("Hello").build {}
        val result = StringWriter().also { writer ->
            TextTree<String>(TextTree.TaskTreeTheme()).dump(nodes) {
                writer.write(it + "\n")
            }
        }
        result.toString() shouldBe """
            Hello
        """.trimIndent()+"\n"
    }

    "One Child" {
        val nodes = StringNodeBuilder("Hello").build {
            +"World"
        }
        val result = StringWriter().also { writer ->
            TextTree<String>(TextTree.TaskTreeTheme()).dump(nodes) {
                writer.write(it + "\n")
            }
        }
        result.toString() shouldBe """
            Hello
            \--- World
        """.trimIndent()+"\n"
    }


})
