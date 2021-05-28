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

package io.hkhc.gradle.android

import io.hkhc.gradle.JarbirdPlugin
import io.hkhc.gradle.JarbirdPlugin.Companion.EXT_PLUGIN_CONFIG
import io.hkhc.gradle.internal.PluginConfig
import io.hkhc.gradle.internal.JarbirdExtensionImpl
import io.hkhc.gradle.internal.ProjectInfo
import io.hkhc.gradle.internal.ProjectProperty
import io.hkhc.gradle.internal.SourceResolver
import io.hkhc.gradle.internal.android.AndroidJarbirdExtensionImpl
import io.hkhc.gradle.internal.android.AndroidSourceResolver
import io.hkhc.gradle.pom.PomGroup
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra

@Suppress("unused")
class AndroidJarbirdPlugin : Plugin<Project> {

    companion object {
        const val PLUGIN_ID = "io.hkhc.jarbird-android"
        // TODO use IHLog to handle LOG_PREFIX
        const val LOG_PREFIX = "[$PLUGIN_ID]"
    }

    val pluginConfig = object : PluginConfig {
        override fun getSourceResolver(project: Project): SourceResolver {
            return AndroidSourceResolver(project)
        }

        override fun newExtension(
            project: Project,
            projectProperty: ProjectProperty,
            projectInfo: ProjectInfo,
            pomGroup: PomGroup
        ): JarbirdExtensionImpl {
            return AndroidJarbirdExtensionImpl(
                project, projectProperty, projectInfo, pomGroup
            )
        }

        override fun shallCreateImplicit(): Boolean {
            return false
        }

        override fun pluginId(): String {
            return PLUGIN_ID
        }
    }

    override fun apply(p: Project) {

        p.extra.set(EXT_PLUGIN_CONFIG, pluginConfig)
        p.pluginManager.apply(JarbirdPlugin::class.java)
    }
}
