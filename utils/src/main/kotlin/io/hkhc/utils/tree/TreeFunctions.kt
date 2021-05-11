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

fun Tree<String>.isSubtreeOf(otherTree: Tree<String>): Boolean {
    return getRoot().isSubtreeOf(otherTree.getRoot())
}

fun Node<String>.isSubtreeOf(otherNode: Node<String>): Boolean {

    return when {

        text() != otherNode.text() -> false

        isLeaf() -> true

        /*
        if anh child that cannot find matching node,
        or matching node is found which is not subtree, than we know 'this' is not subtree of otherNode
         */
        children().find { child ->
                otherNode.children().find { it.text() == child.text() }
                    ?.let { n -> child.isSubtreeOf(n) } != true
            } != null -> false

        else -> true
    }
}

fun <T> Node<T>.visit(block: (Node<T>) -> Unit) {
    block(this)
    children().forEach { it.visit(block) }
}

fun <T> Tree<T>.clone(): Tree<T> {
    return newInstance(getRoot().clone())
}

fun <T> Node<T>.clone(): Node<T> {
    return newInstance(value()).apply {
        children().forEach{ addChild(it.clone()) }
    }
}

/**
 * Create another tree that have the childrens of nodes with specified text be chopped
 * e.g.
 * var tree = Hello
 * +--- World
 * \--- Apple
 *      +---Orange
 *      \---Banana
 *
 * Calling tree.chipChilds(Apple) becomes
 *
 * Hello
 * +--- World
 * \--- Apple
 */

fun <T> Tree<T>.chopChilds(block: (Node<T>) -> Boolean): Tree<T> {
    return newInstance(getRoot().chopChilds(block))
}

fun <T> Node<T>.chopChilds(block: (Node<T>) -> Boolean): Node<T> {

    return newInstance(value()).apply {
        this@chopChilds.children().forEach {
            if (block(it)) {
                addChild(newInstance(it.value()))
            } else {
                addChild(it.chopChilds(block))
            }
        }
    }

}

fun <T> Tree<T>.toStringTree() =
    stringTreeOf(theme) { addChild(getRoot().toStringTree()) }

fun <T> Node<T>.toStringTree() =
    toStringTreeItrn(this)

private fun <T> toStringTreeItrn(node: Node<T>): Node<String> {
    return TreeBuilder<String>().buildNodes(node.text()) {
        node.children().forEach { child ->
            addChild(toStringTreeItrn(child))
        }
    }
}

