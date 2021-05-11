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

package io.hkhc.utils.tree

interface TreeTheme {
    fun lastChild(): String
    fun child(): String
    fun space(): String
    fun midpath(): String
}

// Box drawing characters https://en.wikipedia.org/wiki/Box-drawing_character
@Suppress("unused")
object RoundTheme : TreeTheme {
    override fun lastChild() = "\u2570\u2500\u2500\u2500 " /* L--- */
    override fun child() = "\u251C\u2500\u2500\u2500 " /* +--- */
    override fun space() = "     "
    override fun midpath() = "\u2502    "
}

@Suppress("unused")
object SharpTheme : TreeTheme {
    override fun lastChild() = "\u2514\u2500\u2500\u2500 " /* L--- */
    override fun child() = "\u251C\u2500\u2500\u2500 " /* +--- */
    override fun space() = "     "
    override fun midpath() = "\u2502    "
}

@Suppress("unused")
object DoubleTheme : TreeTheme {
    override fun lastChild() = "\u255a\u2550\u2550\u2550 " /* L--- */
    override fun child() = "\u2560\u2550\u2550\u2550 " /* +--- */
    override fun space() = "     "
    override fun midpath() = "\u2551    "
}

@Suppress("unused")
/**
 * Mimic the style of task-tree plugin
 */
object TaskTreeTheme : TreeTheme {
    override fun lastChild() = "\\--- "
    override fun child() = "+--- "
    override fun space() = "     "
    override fun midpath() = "|    "
}

@Suppress("unused")
object NoBarTheme : TreeTheme {
    override fun lastChild() = " - "
    override fun child() = " - "
    override fun space() = " - "
    override fun midpath() = " - "
}
