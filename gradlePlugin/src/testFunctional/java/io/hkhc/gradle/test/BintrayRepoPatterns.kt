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

import io.hkhc.gradle.pom.internal.isSnapshot

class BintrayRepoPatterns(
    val coordinate: Coordinate,
    val username: String,
    val repo: String,
    private val packaging: String
) {

    private val isSnapshot = coordinate.versionWithVariant.isSnapshot()
    private val metafileName = "maven-metadata.xml"

    private fun isSnapshot(coordinate: Coordinate) = coordinate.versionWithVariant.isSnapshot()

//    fun metafile(base: String): List<String> {
//        return mutableListOf<String>().apply {
//            add("$base/$metafileName")
//            if (isSnapshot) add("$base/${coordinates.versionWithVariant}/$metafileName")
//        }
//    }

    private fun listPluginRepo(pluginId: String?, versionTransformer: (String) -> String): List<String> {
        return coordinates.flatMap { coordinate ->
            with(coordinate) {
                pluginId?.let {
                    listOf(
                        "/content/$username/$repo/$artifactIdWithVariant/$versionWithVariant/" +
                            "${pluginId.replace('.', '/')}/$pluginId.gradle.plugin"
                    )
                        .flatMap {
                            listOf(
                                "$it/$versionWithVariant/" +
                                    "$pluginId.gradle.plugin-${versionTransformer(versionWithVariant)}.pom"
                            )
                        }
                } ?: listOf()
            }
        }
    }

    private fun artifactTypes(path: String) =
        listOf(".$packaging", "-javadoc.jar", "-sources.jar", ".pom")
            .map { suffix -> "$path$suffix" }

    fun list(versionTransformer: (String) -> String) = with(coordinate) {
        (
            listPluginRepo(pluginId, versionTransformer)
                .map { "$it\\?override=1" } +
                listOf(
                    "/content/$username/$repo/" +
                        "$artifactIdWithVariant/$versionWithVariant/${getPath()}/" +
                        "$artifactIdWithVariant-${versionTransformer(versionWithVariant)}"
                )
                    .flatMap(::artifactTypes)
                    /* no signature for snapshot publishing */
                    .flatMap { if (isSnapshot) listOf(it) else listOf(it, "$it.asc") }
                    .map { "$it\\?override=1" }
            )
            .map { Regex(it) }
    }
}
