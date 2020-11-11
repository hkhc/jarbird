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

package io.hkhc.gradle.test

import isSnapshot

class MavenRepoPatterns(
    private val baseUrl: String,
    val coordinate: Coordinate,
    packaging: String
) {

    private val hashExts = listOf("", ".md5", ".sha1", ".sha256", ".sha512")
    private val artifactClassifier = listOf(".$packaging", "-javadoc.jar", "-sources.jar", ".module", ".pom")

    private val isSnapshot = coordinate.versionWithVariant.isSnapshot()
    private val metafileName = "maven-metadata.xml"

    private fun metafile(base: String): List<String> {
        return mutableListOf<String>().apply {
            add("$base/$metafileName")
            if (isSnapshot) add("$base/${coordinate.versionWithVariant}/$metafileName")
        }
    }

    private fun listPluginRepo(pluginId: String?, versionTransformer: (String) -> String) = with(coordinate) {
        pluginId?.let {
            listOf("$baseUrl/${pluginId.replace('.', '/')}/$pluginId.gradle.plugin")
                .flatMap {
                    metafile(it) +
                        "$it/$versionWithVariant/$pluginId.gradle.plugin-${versionTransformer(versionWithVariant)}.pom"
                }
                .flatMap(::hashedPaths)
        } ?: listOf()
    }

    private fun hashedPaths(path: String) = hashExts.map { hash -> "$path$hash" }

    private fun artifactTypes(path: String) = artifactClassifier.map { suffix -> "$path$suffix" }

    fun list(versionTransformer: (String) -> String) = with(coordinate) {
        (
            listPluginRepo(pluginId, versionTransformer) +
                listOf("$baseUrl/${group.replace('.', '/')}/$artifactIdWithVariant")
                    .flatMap { path ->
                        metafile(path) +
                            listOf(
                                "$path/$versionWithVariant/" +
                                    "$artifactIdWithVariant-${versionTransformer(versionWithVariant)}"
                            )
                                .flatMap(::artifactTypes)
                                .flatMap { if (isSnapshot) listOf(it) else listOf(it, "$it.asc") }
                    }
                    .flatMap(::hashedPaths)
            )
            .map { Regex("$it.*") }
    }
}
