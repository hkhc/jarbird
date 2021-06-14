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

open class Tree<T>(private var root: Node<T>? = null, var theme: TreeTheme = defaultTreeTheme) {

    constructor(otherTree: Tree<T>) : this(otherTree.root)

//    abstract fun newNode(value: T): Node<T>
//    abstract fun newTree(rootNode: Node<T>): Tree<T>

    fun getRoot(): Node<T> {
        return root?: throw TreeException("no root node in tree")
    }

    fun getRootOrNull(): Node<T>? {
        return root
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun newInstance(rootNode: Node<T>): Tree<T> {
        this::class.constructors.iterator().forEach {
            if (it.parameters.size == 2 &&
                it.parameters[0].type.classifier == Node::class &&
                it.parameters[1].type.classifier == TreeTheme::class
            ) return it.call(rootNode, theme)
        }
        throw IllegalStateException("Appropriate constructor is not found")
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other is Tree<*>) {
            return getRoot() == other.getRoot()
        } else {
            return false
        }
    }

    override fun hashCode(): Int {
        return 31 * "Tree".hashCode() + getRoot().hashCode()
    }

    override fun toString(): String {
        return TreePrinter(theme).dumpToString(this)
    }
}
