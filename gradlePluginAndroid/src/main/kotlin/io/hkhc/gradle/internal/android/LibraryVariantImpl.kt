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

import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Zip

/**
 * It is a shadow of com.android.build.gradle.api.LibraryVariant in Android gradle build tool.
 * To avoid having module dependency to that module, whcih is so big that cause all kind of memory and classloading
 * problems.
 */
@Suppress("UNCHECKED_CAST")
class LibraryVariantImpl(private val obj: Any) : LibraryVariant {

    // implements LibraryVariant
    override fun getPackageLibraryProvider() =
        obj.getter("packageLibraryProvider") as TaskProvider<Zip>?

    // implements BaseVariant

    override fun getName() =
        obj.getter("name") as String?

    override fun getDescription() =
        obj.getter("description") as String?

    override fun getSourceSets(): List<SourceProvider> {
        val rawList = obj.getter("sourceSets") as List<Any>
        return rawList
            .map { SourceProvider.implemented(it) }
            .filterNotNull()
    }
}
