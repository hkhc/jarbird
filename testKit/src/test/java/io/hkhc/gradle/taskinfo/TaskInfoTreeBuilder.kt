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

package io.hkhc.gradle.taskinfo

import com.google.gson.Gson
import io.hkhc.utils.tree.Tree
import io.hkhc.utils.tree.TreeBuilder
import io.hkhc.utils.tree.visit
import org.gradle.testkit.runner.BuildTask
import java.io.FileReader

/**
 * Load the executed task graph from the output of tiJson task
 */
fun TreeBuilder<TaskInfo>.load(taskInfoFilePath: String, taskResults: List<BuildTask>): Tree<TaskInfo> {

    val gson = Gson()
    val rawTree = gson.fromJson(FileReader(taskInfoFilePath), TaskInfo::class.java)
    val tree = build(null) {
        addChild(TaskInfoNode(rawTree).load())
    }
    val statuses = readTaskStatuses(taskResults)
    tree.getRoot().visit {
        it.value().status = statuses[it.value().path]
    }

    return tree
}

/**
 * Load the execution result of tasks from the BuildTask, so they can combine
 */
private fun readTaskStatuses(taskResults: List<BuildTask>): Map<String, String> {
    return taskResults.fold(mutableMapOf()) { map, buildTask ->
        map.apply { put(buildTask.path, buildTask.outcome.toString()) }
    }
}
