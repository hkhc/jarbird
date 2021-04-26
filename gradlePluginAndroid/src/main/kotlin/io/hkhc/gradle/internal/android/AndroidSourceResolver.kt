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

import com.android.build.gradle.api.LibraryVariant
import com.android.builder.model.SourceProvider
import io.hkhc.gradle.internal.DefaultSourceResolver
import org.gradle.api.Project
import java.io.File

class AndroidSourceResolver(project: Project) : DefaultSourceResolver(project) {

    private fun files(sourceProvider: SourceProvider): Collection<File> {
        return sourceProvider.javaDirectories + sourceProvider.renderscriptDirectories
    }

    override fun getSourceJarSource(source: Any): Array<out Any> {
        return when (source) {
            is LibraryVariant -> {
                source.sourceSets.fold(
                    mutableListOf<File>(),
                    { list, sourceProvider ->
                        list += files(sourceProvider)
                        list
                    }
                ).toTypedArray()
            }
            is SourceProvider -> {
                arrayOf(source.javaDirectories, source.resourcesDirectories)
            }
            is List<*> -> {
                source.fold(
                    mutableListOf<Any>(),
                    { list, files ->
                        list += when (files) {
                            is SourceProvider -> files(files)
                            else -> files
                        } as Any
                        list
                    }
                ).toTypedArray()
            }
            else -> {
                super.getSourceJarSource(source)
            }
        }
    }
}
