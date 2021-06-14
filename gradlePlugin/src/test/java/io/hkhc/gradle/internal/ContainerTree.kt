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

package io.hkhc.gradle.internal

import io.hkhc.utils.test.MockTaskContainer
import io.hkhc.utils.tree.NoBarTheme
import io.hkhc.utils.tree.Node
import io.hkhc.utils.tree.StringNode
import io.hkhc.utils.tree.Tree
import io.hkhc.utils.tree.TreeTheme

fun MockTaskContainer.convertToTrees(theme: TreeTheme = NoBarTheme): List<Tree<String>> {

    val nodeMap = mutableMapOf<String, Node<String>>()

    mockTasks.forEach { task ->
        val node = nodeMap[task.name]
        if (node == null) {
            nodeMap[task.name] = StringNode(task.name)
        }
        task.getDependsOn().forEach {
            val dependsName = it as String
            val dependsNode = nodeMap[dependsName] ?: StringNode(dependsName).apply {
                nodeMap[dependsName] = this
            }
            nodeMap[task.name]?.addChild(dependsNode)
        }
    }

    // find out all node that is a child of others, so it is not root
    val allChilds = nodeMap
        .flatMap { entry -> entry.value.children() }
        .map { it.value() }
        .toSet()

    // remove these nodes from the map
    allChilds.forEach {nodeMap.remove(it) }

    // create a tree for each of remaining nodes in map
    return nodeMap.map { entry -> Tree(entry.value, theme) }

}
