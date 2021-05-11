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

interface LibraryVariant : BaseVariant {

    companion object {
        private const val interfaceName = "com.android.build.gradle.api.LibraryVariant"
        fun implemented(obj: Any): LibraryVariant? {
            return if (obj.hasImplemented(interfaceName)) LibraryVariantImpl(obj) else null
        }
    }

    fun getPackageLibraryProvider(): TaskProvider<Zip>?
}
