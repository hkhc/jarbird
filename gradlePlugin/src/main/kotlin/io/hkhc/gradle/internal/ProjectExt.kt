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
import org.gradle.api.ProjectEvaluationListener
import org.gradle.api.ProjectState

@Suppress("unused")
fun Project.isMultiProjectRoot() =
    rootProject == this && childProjects.isNotEmpty()

fun Project.isSingleProject() =
    rootProject == this && childProjects.isEmpty()

// Shortcut to register ProjectEvaluationListener when we need afterEvaluate only.
fun Project.gradleAfterEvaluate(action: (ProjectState) -> Unit) {
    gradle.addProjectEvaluationListener(
        object : ProjectEvaluationListener {
            override fun afterEvaluate(project: Project, state: ProjectState) {
                if (project == this@gradleAfterEvaluate) {
                    action.invoke(state)
                }
            }
            override fun beforeEvaluate(project: Project) {
                // doing nothing intentionally
            }
        }
    )
}
