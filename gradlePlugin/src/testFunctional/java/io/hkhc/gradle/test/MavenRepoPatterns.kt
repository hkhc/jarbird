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

class MavenRepoPatterns(
    val baseUrl: String,
    val coordinate: Coordinate
) {

    val isSnapshot = coordinate.version.endsWith("-SNAPSHOT")
    val METADATA_FILE = "maven-metadata.xml"

    fun metafile(base: String): List<String> {
        return mutableListOf<String>().apply {
            add("$base/$METADATA_FILE")
            if (isSnapshot) add("$base/${coordinate.version}/$METADATA_FILE")
        }
    }

    fun listPluginRepo(pluginId: String?, versionTransformer: (String) -> String) =
        pluginId?.let {
            listOf("$baseUrl/${pluginId.replace('.', '/')}/$pluginId.gradle.plugin")
                .flatMap {
                    metafile(it) +
                        listOf("$it/${coordinate.version}/"+
                            "${coordinate.pluginId}.gradle.plugin-${versionTransformer(coordinate.version)}.pom")
                }
                .flatMap(::hashedPaths)
        } ?: listOf()

    fun hashedPaths(path: String) =
        listOf("", ".md5", ".sha1", ".sha256", ".sha512")
            .map { hash -> "$path$hash" }

    fun artifactTypes(path: String) =
        listOf(".jar", "-javadoc.jar", "-sources.jar", ".module", ".pom")
            .map { suffix -> "$path$suffix" }

    fun list(versionTransformer: (String) -> String) = (
        listPluginRepo(coordinate.pluginId, versionTransformer) +
            listOf("$baseUrl/${coordinate.group.replace('.', '/')}/${coordinate.artifactId}")
                .flatMap {
                    metafile(it) +
                        listOf("$it/${coordinate.version}/"+
                            "${coordinate.artifactId}-${versionTransformer(coordinate.version)}")
                            .flatMap(::artifactTypes)
                            .flatMap { if (isSnapshot) listOf(it) else listOf(it, "$it.asc") }
                }
                .flatMap(::hashedPaths)
        )
        .map { Regex(it) }
}
