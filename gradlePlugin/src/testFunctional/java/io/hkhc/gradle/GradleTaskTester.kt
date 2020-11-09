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

package io.hkhc.gradle

import io.kotest.assertions.withClue
import io.kotest.inspectors.forAll
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File

class GradleTaskTester(
    private val projectDir: File,
    private val envs: Map<String, String> = defaultEnvs(projectDir)
) {

    private fun defaultEnvs(projectDir: File) = mutableMapOf(getTestGradleHomePair(projectDir))

    fun runTask(task: String) = runTasks(arrayOf(task))

    @Suppress("SpreadOperator")
    fun runTasks(tasks: Array<String>): BuildResult {

        withClue("Project directory '$projectDir' shall exist") {
            projectDir.exists() shouldBe true
        }

        envs.entries.forAll {
            withClue("Environment Variable '${it.key}' should have non null value") {
                it.value.shouldNotBeNull()
            }
        }
        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withEnvironment(envs)
            .withArguments("--stacktrace", "tasks", "--all", *tasks)
            .withPluginClasspath()
//        .forwardOutput()
            .build()

        return result
    }
}
