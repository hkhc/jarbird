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

/**
 * The implementation of Iterable interfave shall return real node in
 * the tree, and not created just for the returned iterator.
 */
interface Node<T> {
    fun isLeaf(): Boolean
    fun children(): Collection<Node<T>>
    fun text(): String
    fun value(): T
    fun addChild(node: Node<T>)
    fun newInstance(value: T): Node<T>
}
