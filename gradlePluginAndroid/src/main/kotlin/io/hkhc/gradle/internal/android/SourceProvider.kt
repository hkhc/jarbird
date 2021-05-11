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

import java.io.File

@Suppress("TooManyFunctions")
interface SourceProvider {

    companion object {
        const val interfaceName = "com.android.builder.model.SourceProvider"
        fun implemented(obj: Any): SourceProvider? {
            return if (obj.hasImplemented(interfaceName)) SourceProviderImpl(obj) else null
        }
    }

    /**
     * Returns the name of this source set.
     *
     * @return The name. Never returns null.
     */
    fun getName(): String

    /**
     * Returns the manifest file.
     *
     * @return the manifest file. It may not exist.
     */
    fun getManifestFile(): File

    /**
     * Returns the java source folders.
     *
     * @return a list of folders. They may not all exist.
     */
    fun getJavaDirectories(): Collection<File>

    /**
     * Returns the java resources folders.
     *
     * @return a list of folders. They may not all exist.
     */
    fun getResourcesDirectories(): Collection<File>

    /**
     * Returns the aidl source folders.
     *
     * @return a list of folders. They may not all exist.
     */
    fun getAidlDirectories(): Collection<File>

    /**
     * Returns the renderscript source folders.
     *
     * @return a list of folders. They may not all exist.
     */
    fun getRenderscriptDirectories(): Collection<File>

    /**
     * Returns the C source folders.
     *
     * @return a list of folders. They may not all exist.
     */
    fun getCDirectories(): Collection<File>

    /**
     * Returns the C++ source folders.
     *
     * @return a list of folders. They may not all exist.
     */
    fun getCppDirectories(): Collection<File>

    /**
     * Returns the android resources folders.
     *
     * @return a list of folders. They may not all exist.
     */
    fun getResDirectories(): Collection<File>

    /**
     * Returns the android assets folders.
     *
     * @return a list of folders. They may not all exist.
     */
    fun getAssetsDirectories(): Collection<File>

    /**
     * Returns the native libs folders.
     *
     * @return a list of folders. They may not all exist.
     */
    fun getJniLibsDirectories(): Collection<File>

    /**
     * Returns the shader folders.
     *
     * @return a list of folders. They may not all exist.
     */
    fun getShadersDirectories(): Collection<File>

    /**
     * Returns the machine learning models folders.
     *
     * @return a list of folders. They may not all exist.
     */
    fun getMlModelsDirectories(): Collection<File>
}
