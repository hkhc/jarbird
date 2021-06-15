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

open class AbstractNode<T>(private val value: T) : Node<T> {

    protected val children = mutableListOf<Node<T>>()

    override fun equals(other: Any?): Boolean {

        if (other == null) return false
        if (other is Node<*>) {
            if (value() != other.value()) {
                return false
            } else {
                val otherIterator = other.children().iterator()
                children().forEach {
                    val otherChild = if (otherIterator.hasNext()) otherIterator.next() else null
                    if (otherChild != null) {
                        if (it != otherChild) return false
                    } else {
                        return false
                    }
                }
            }
        } else {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        return children.fold(value.hashCode()) { h, item -> 31 * h + item.value().hashCode() }
    }

    override fun toString(): String {
        return "Node(${value()})${if (children.size != 0) " " + children.joinToString(prefix = "[", postfix = "]") else ""}"
    }

    override fun addChild(node: Node<T>) { children.add(node) }

    override fun newInstance(value: T): Node<T> {
        this::class.constructors.forEach {
            if (it.parameters.size == 1) return it.call(value)
        }
        throw IllegalStateException("Appropriate constructor is not found")
    }

    override fun isLeaf() = children.isEmpty()
    override fun children() = children
    override fun text() = value().toString()
    override fun value() = value
}
