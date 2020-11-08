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

import java.io.File

class TextTree<T>(val theme: Theme) {

    interface Node<T> {
        fun isLeaf(): Boolean
        fun forEachIndexed(action: (count: Int, index: Int, child: Node<T>) -> Unit)
        fun childCount(): Int
        fun text(): String
    }

    interface Theme {
        fun lastChild(): String
        fun child(): String
        fun space(): String
        fun midpath(): String
    }

    fun <T> dump(node: Node<T>, block: (String) -> Unit) {
        if (!node.isLeaf()) {
            block.invoke(node.text())
            node.forEachIndexed { count, idx, child ->
                val headPrefix = if (idx == count - 1) {
                    theme.lastChild()
                } else {
                    theme.child()
                }
                val tailPrefix = if (idx == count - 1) theme.space() else theme.midpath()
                var first = true
                dump<T>(child) {
                    if (first) {
                        block.invoke(headPrefix + it)
                        first = false
                    } else {
                        block.invoke(tailPrefix + it)
                    }
                }
            }
        } else {
            block.invoke(node.text())
        }
    }

    class FileNode(val file: File) : Node<File> {
        private var child: Array<File>? = null
        private fun fillList() {
            if (child == null) child = file.listFiles()
        }
        override fun isLeaf() = !file.isDirectory()
        override fun forEachIndexed(action: (count: Int, index: Int, child: Node<File>) -> Unit) {
            fillList()
            child?.let { list ->
                val c = childCount()
                list.forEachIndexed { idx, item ->
                    action.invoke(c, idx, FileNode(item))
                }
            }
        }
        override fun text(): String {
            return if (isLeaf()) {
                "${file.name} (${file.length()})"
            } else {
                file.name
            }
        }

        override fun childCount(): Int {
            fillList()
            return child?.size ?: 0
        }
    }

    // Box drawing characters https://en.wikipedia.org/wiki/Box-drawing_character
    class RoundTheme : Theme {
        override fun lastChild(): String {
            return "\u2570\u2500\u2500\u2500 " /* L--- */
        }

        override fun child(): String {
            return "\u251C\u2500\u2500\u2500 " /* +--- */
        }

        override fun space(): String {
            return "     "
        }

        override fun midpath(): String {
            return "\u2502    "
        }
    }

    fun filedump(file: File, block: (String) -> Unit) {
        dump(FileNode(file), block)
    }

    class StringNode(val str: String) : Node<String> {
        private val childs = mutableListOf<Node<String>>()
        override fun isLeaf() = childs.isEmpty()
        override fun forEachIndexed(action: (count: Int, index: Int, child: Node<String>) -> Unit) {
            childs.forEachIndexed { index, item -> action.invoke(childCount(), index, item) }
        }
        override fun childCount() = childs.size
        override fun text() = str
        fun addChild(node: StringNode) { childs.add(node) }
    }

    class TaskTreeTheme : Theme {
        override fun lastChild() = "\\--- "
        override fun child() = "+--- "
        override fun space() = "     "
        override fun midpath() = "|    "
    }
}

class StringNodeBuilder(val root: String) {
    private lateinit var node: TextTree.StringNode
    fun build(block: StringNodeBuilder.() -> Unit): TextTree.StringNode {
        node = TextTree.StringNode(root)
        block.invoke(this)
        return node
    }
    operator fun String.unaryPlus() {
        val s = this
        node.addChild(TextTree.StringNode(s))
    }
    operator fun TextTree.StringNode.unaryPlus() {
        val s = this
        node.addChild(s)
    }
    operator fun String.invoke(block: StringNodeBuilder.() -> Unit): TextTree.StringNode {
        return StringNodeBuilder(this).build(block)
    }
}
