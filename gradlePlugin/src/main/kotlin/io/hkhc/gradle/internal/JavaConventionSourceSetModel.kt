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
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet

class JavaConventionSourceSetModel(
    private val project: Project,
    private val sourceSetName: String = "main"
) : SourceSetModel {

    /*
     TODO JavaPluginConvention is deprecated in Gradle 7 and will be removed in Gradle 8. So enhance this class to
     support both JavaPluginConvention and the new JavaPluginExtensioon class.
     https://docs.gradle.org/current/javadoc/org/gradle/api/plugins/JavaPluginConvention.html
     */
    private fun getSourceSet(name: String): SourceSet {

        val javaPlugin: JavaPluginConvention = project.convention.getPlugin(
            JavaPluginConvention::class.java
        )
        return javaPlugin.sourceSets.getByName(name)
    }

    override val sourceFolders: Set<Any>
        get() {
            return getSourceSet(sourceSetName).allJava.srcDirs
        }
    override val classpath: Set<Any>
        get() {
            return getSourceSet(sourceSetName).compileClasspath.files
        }
}
