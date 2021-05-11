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

@Suppress("TooManyFunctions", "UNCHECKED_CAST")
class SourceProviderImpl(private val obj: Any) : SourceProvider {

    override fun getName() =
        obj.getter("name") as String

    override fun getManifestFile() =
        obj.getter("manifestFile") as File

    override fun getJavaDirectories() =
        obj.getter("javaDirectories") as Collection<File>

    override fun getResourcesDirectories() =
        obj.getter("resourcesDirectories") as Collection<File>

    override fun getAidlDirectories() =
        obj.getter("aidlDirectories") as Collection<File>

    override fun getRenderscriptDirectories() =
        obj.getter("renderscriptDirectories") as Collection<File>

    override fun getCDirectories() =
        obj.getter("CDirectories") as Collection<File>

    override fun getCppDirectories() =
        obj.getter("cppDirectories") as Collection<File>

    override fun getResDirectories() =
        obj.getter("resDirectories") as Collection<File>

    override fun getAssetsDirectories() =
        obj.getter("assetsDirectories") as Collection<File>

    override fun getJniLibsDirectories() =
        obj.getter("jniLibsDirectories") as Collection<File>

    override fun getShadersDirectories() =
        obj.getter("shadersDirectories") as Collection<File>

    override fun getMlModelsDirectories() =
        obj.getter("mlModelsDirectories") as Collection<File>
}
