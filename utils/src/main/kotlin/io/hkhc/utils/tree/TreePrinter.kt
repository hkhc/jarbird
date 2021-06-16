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

import java.io.PrintWriter
import java.io.StringWriter

var defaultTreeTheme: TreeTheme = RoundTheme

class TreePrinter(private val theme: TreeTheme = defaultTreeTheme) {

    private open class EdgePresenter(open var head: String, open var tail: String) {
        private var first = true
        fun getEdge() = if (first) {
            first = false
            head
        } else {
            tail
        }
    }

    private class FrontChildEdgePresenter(theme: TreeTheme) :
        EdgePresenter(theme.child(), theme.midpath())

    private class LastChildEdgePresenter(theme: TreeTheme) :
        EdgePresenter(theme.lastChild(), theme.space())

    private class RootEdgePresenter : EdgePresenter("", "")

    fun <T> dumpToString(tree: Tree<T>): String {
        return dumpToString(tree.getRoot())
    }

    fun <T> dumpToString(node: Node<T>): String {
        val stringWriter = StringWriter()
        val writer = PrintWriter(stringWriter)
        dump(node, writer::println)
        writer.close()
        return stringWriter.toString()
    }

    fun <T> dump(node: Node<T>, block: (String) -> Unit = ::println) {
        dump(true, true, node, block)
    }

    private fun <T> dump(isRoot: Boolean, isLast: Boolean, node: Node<T>, block: (String) -> Unit) {

        val edgePresenter = if (isRoot) {
            RootEdgePresenter()
        } else {
            if (isLast)
                LastChildEdgePresenter(theme)
            else
                FrontChildEdgePresenter(theme)
        }

        block.invoke(edgePresenter.getEdge() + node.text())
        node.children().forEachIndexed { childIdx, child ->
            dump(false, childIdx == node.children().size - 1, child) {
                block(edgePresenter.getEdge() + it)
            }
        }
    }
}
