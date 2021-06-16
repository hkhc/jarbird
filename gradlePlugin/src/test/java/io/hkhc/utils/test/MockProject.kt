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

package io.hkhc.utils.test

import io.hkhc.utils.tree.Node
import io.hkhc.utils.tree.StringNode
import io.hkhc.utils.tree.Tree
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.specs.Spec

fun createSingleMockProject(name: String): Project {
    return createMockProjectTree(StringNode("name"), mutableMapOf(), null, null)
}

fun createMockProjectTree(
    stringNode: Node<String>,
    map: MutableMap<String, Project>,
    rootProj: Project?,
    parentProj: Project?
): Project {
    val p = mockk<Project> {
        every { name } returns stringNode.text()
        every { project } returns this
        every { rootProject } returns (rootProj ?: this)
        every { parent } returns parentProj

        every { childProjects } returns stringNode.children().fold(mutableMapOf<String, Project>()) { childMap, item ->
            childMap[item.text()] = createMockProjectTree(item, map, rootProj ?: this, this)
            childMap
        }
        every { tasks } returns MockTaskContainer(this).apply {
            mockWithTypeTasks = mapOf(
                PublishToMavenRepository::class.java to mockk<PublishToMavenRepository>(relaxed = true) {
                    val task = this
                    every { onlyIf(any<Spec<Task>>()) } answers {
                        val action = firstArg<Spec<Task>>()
                        action.isSatisfiedBy(task)
                    }
                }
            )
        }

        every { extensions } returns MockExtensionContainer()
        every { logger } returns mockk {
            every { logger.warn(any()) } returns Unit
            every { logger.debug(any()) } returns Unit
            every { logger.error(any()) } returns Unit
            every { logger.info(any()) } returns Unit
        }

        map[stringNode.text()] = this
    }

    return p
}

fun createMockProjectTree(stringTree: Tree<String>): Map<String, Project> {
    val map = mutableMapOf<String, Project>()
    createMockProjectTree(stringTree.getRoot(), map, null, null)
    return map
}
