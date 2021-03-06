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

import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSetContainer

class SourceSetNames(private val project: Project, private val names: Array<out String>) {

    private val extensions = (project as ExtensionAware).extensions

    private val sourceSets: SourceSetContainer
        get() =
            extensions.getByName("sourceSets") as SourceSetContainer

    fun getDirs(): Array<out Any> {
        return names.flatMap {
            val sourceSet = sourceSets.getByName(it)
            sourceSet.allSource.srcDirs
        }.toTypedArray()
    }
}
