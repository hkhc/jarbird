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

class BintrayRepoPatterns(
    val coordinate: Coordinate,
    val username: String,
    val repo: String,
    val packaging: String = "jar"
) {

    val isSnapshot = coordinate.version.endsWith("-SNAPSHOT")
    val METADATA_FILE = "maven-metadata.xml"

    fun metafile(base: String): List<String> {
        return mutableListOf<String>().apply {
            add("$base/$METADATA_FILE")
            if (isSnapshot) add("$base/${coordinate.version}/$METADATA_FILE")
        }
    }

    fun listPluginRepo(pluginId: String?, versionTransformer: (String) -> String) = with(coordinate) {
        pluginId?.let {
            listOf(
                "/content/$username/$repo/" +
                    "$artifactId/$version/" +
                    "${pluginId.replace('.', '/')}/$pluginId.gradle.plugin"
            )
                .flatMap {
                    listOf(
                        "$it/$version/" +
                            "$pluginId.gradle.plugin-${versionTransformer(version)}.pom"
                    )
                }
        } ?: listOf()
    }

    fun artifactTypes(path: String) =
        listOf(".$packaging", "-javadoc.jar", "-sources.jar", ".pom")
            .map { suffix -> "$path$suffix" }

    fun list(versionTransformer: (String) -> String) = with(coordinate) {
        (
            listPluginRepo(pluginId, versionTransformer)
                .map { "$it\\?override=1" } +
                listOf(
                    "/content/$username/$repo/" +
                        "$artifactId/$version/" +
                        "${group.replace('.', '/')}/" +
                        "$artifactId/$version/" +
                        "$artifactId-${versionTransformer(version)}"
                )
                    .flatMap(::artifactTypes)
                    .flatMap { if (isSnapshot) listOf(it) else listOf(it, "$it.asc") }
                    .map { "$it\\?override=1" }
            )
            .map { Regex(it) }
    }
}
