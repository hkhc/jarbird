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

import io.hkhc.utils.tree.Node
import io.hkhc.utils.tree.Tree
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.Project

fun createMockProjectTree(stringNode: Node<String>, map: MutableMap<String, Project>, rootProj: Project?, parentProj: Project?): Project {
    val p = mockk<Project> {
        println("create project ${stringNode.text()}")
        every { name } returns stringNode.text()
        every { project } returns this
        every { rootProject } returns (rootProj?: this)
        every { parent } returns parentProj

        every { childProjects } returns stringNode.children().fold(mutableMapOf<String, Project>()) { childMap, item ->
            childMap[item.text()] = createMockProjectTree(item, map, rootProj?: this, this)
            childMap
        }
        every { tasks } returns MockTaskContainer(this)
        println("save to global map (${stringNode.text()})")
        map[stringNode.text()] = this
    }

    return p
}

fun createMockProjectTree(stringTree: Tree<String>): Map<String, Project> {
    val map = mutableMapOf<String, Project>()
    createMockProjectTree(stringTree.getRoot(), map, null, null)
    return map
}
