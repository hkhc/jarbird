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

import kotlin.reflect.full.createInstance

open class TreeBuilder<T>(
    val theme: TreeTheme = defaultTreeTheme,
    private val newNode: (T) -> Node<T> = { AbstractNode(it) }
) {

    // the root node
    private var node: Node<T>? = null

    fun build(rootValue: T? = null, block: TreeBuilder<T>.() -> Unit = {}): Tree<T> {
        return Tree(buildNodes(rootValue, block), theme)
    }

    fun buildNodes(rootValue: T?, block: TreeBuilder<T>.() -> Unit = {}): Node<T> {
        if (rootValue != null) node = newNode(rootValue)
        block.invoke(this)
        return node ?: throw IllegalStateException("No node is defined")
    }

    /**
     *  stringTree {
     *      "Hello"()
     *  }
     */
    operator fun T.invoke() {
        addChild(this)
    }

    /**
     *  stringTree {
     *      + "Hello"
     *  }
     */
    operator fun T.unaryPlus() {
        addChild(this)
    }

    fun addChild(childValue: T) {
        val newChildNode = newNode(childValue)
        addChild(newChildNode)
    }

    fun addChild(childNode: Node<T>) {
        node?.addChild(childNode) ?: run { node = childNode }
    }

    private fun newInstance(): TreeBuilder<T> = this::class.createInstance()

    operator fun T.invoke(block: TreeBuilder<T>.() -> Unit): Node<T> {
        val newNode = newInstance().buildNodes(this, block)
        addChild(newNode)
        return newNode
    }
}
