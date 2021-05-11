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

package io.hkhc.gradle.internal.android

import io.hkhc.gradle.internal.DefaultSourceResolver
import org.gradle.api.Project
import java.io.File

class AndroidSourceResolver(project: Project) : DefaultSourceResolver(project) {

    private fun files(sourceProvider: SourceProvider): Collection<File> {
        return sourceProvider.getJavaDirectories() + sourceProvider.getResourcesDirectories()
    }

    override fun getSourceJarSource(source: Any): Array<out Any> {

        return LibraryVariant.implemented(source)?.getSourceSets()?.fold(mutableListOf<File>()) { list, sp ->
            SourceProvider.implemented(sp)?.let { sourceProvider -> list += files(sourceProvider) }
            list
        }?.toTypedArray() ?: SourceProvider.implemented(source)?.let {
            arrayOf(it.getJavaDirectories(), it.getResourcesDirectories())
        } ?: (source as? List<*>)?.let {
            source
                .map { it?.let { SourceProvider.implemented(it) ?: it } }
                .fold(mutableListOf<Any>()) { list, sp ->
                    sp?.let { list += if (sp is SourceProvider) files(sp) else sp }
                    list
                }
                .toTypedArray()
        } ?: super.getSourceJarSource(source)
    }
}
