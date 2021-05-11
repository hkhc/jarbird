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

package io.hkhc.gradle.test

import io.hkhc.gradle.taskinfo.TaskInfoNode
import io.hkhc.gradle.taskinfo.load
import io.hkhc.utils.tree.FileNode
import io.hkhc.utils.tree.NoBarTheme
import io.hkhc.utils.tree.RoundTheme
import io.hkhc.utils.tree.TreeBuilder
import io.hkhc.utils.tree.TreePrinter
import io.hkhc.utils.tree.load
import org.gradle.testkit.runner.BuildResult
import java.io.File

fun printFileTree(base: File) {
    TreePrinter(RoundTheme).dump(FileNode(base).load(), System.out::println)
}

fun getTaskTree(projectDir: File, targetTask: String, buildResult: BuildResult) =
    TreeBuilder(NoBarTheme, ::TaskInfoNode)
        .load(
            "$projectDir/build/taskInfo/taskinfo-$targetTask.json",
            buildResult.tasks
        )
