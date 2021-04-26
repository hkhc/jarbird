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

package io.hkhc.gradle

import io.hkhc.gradle.internal.JarbirdExtensionImpl
import io.hkhc.gradle.internal.ProjectInfo
import io.hkhc.gradle.internal.ProjectProperty
import io.hkhc.gradle.internal.SourceResolver
import io.hkhc.gradle.pom.PomGroup
import org.gradle.api.Project

interface PluginConfig {

    fun getSourceResolver(project: Project): SourceResolver
    fun newExtension(
        project: Project,
        projectProperty: ProjectProperty,
        projectInfo: ProjectInfo,
        pomGroup: PomGroup
    ): JarbirdExtensionImpl
    fun shallCreateImplicit(): Boolean
    fun pluginId(): String

}
