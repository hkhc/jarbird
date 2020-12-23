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

package io.hkhc.gradle.internal

import org.gradle.api.Project

fun Project.registerRootProjectTasks(taskPath: String) {

    tasks.register(taskPath) {
        val rootTask = this
        group = SP_GROUP
        description = "TODO..."
        project.childProjects.forEach { (_, child) ->
            child.tasks.findByPath(taskPath)?.let { childTask ->
                rootTask.dependsOn(childTask.path)
            }
        }
    }
}

fun Project.registerRootProjectTasks(taskInfo: TaskInfo) {

    taskInfo.register(project.tasks) {
        val rootTask = this
        project.childProjects.forEach { (_, child) ->
            child.tasks.findByPath(taskInfo.name)?.let { childTask ->
                rootTask.dependsOn(childTask.path)
            }
        }
    }
}
