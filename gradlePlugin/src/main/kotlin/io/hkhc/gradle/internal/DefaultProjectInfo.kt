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

import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginConvention
import java.io.File

class DefaultProjectInfo(private val project: Project) : ProjectInfo {

    override var group: String
        get() = project.group.toString()
        set(value) {
            project.group = value
        }
    override var artifactId: String
        get() = project.name
        set(_) {
            throw UnsupportedOperationException("Project name is immutable")
        }
    override var version: String
        get() = project.version.toString()
        set(value) {
            project.version = value
        }
    override var description: String
        get() = project.description ?: ""
        set(value) {
            project.description = value
        }
    override var archiveBaseName: String
        get() {
            val convention = project.convention.plugins["base"] as BasePluginConvention?
            return convention?.archivesBaseName ?: ""
        }
        set(value) {
            val convention = project.convention.plugins["base"] as BasePluginConvention?
            convention?.let { c ->
                c.archivesBaseName = value
            }
        }

    override val rootDir: File
        get() = project.rootDir

    override val projectDir: File
        get() = project.projectDir
}
