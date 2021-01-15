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

class MavenRepoPatterns(
    private val baseUrl: String,
    val coordinates: List<Coordinate>,
    packaging: String,
    private val releaseVersionTransformer: (String) -> String,
    private val snapshotVersionTransformer: (String) -> String,
    withMetadata: Boolean = true
) {

    private val hashExts = listOf("", ".md5", ".sha1", ".sha256", ".sha512")
    private val artifactClassifier = if (withMetadata) {
        listOf(".$packaging", "-javadoc.jar", "-sources.jar", ".module", ".pom")
    } else {
        listOf(".$packaging", "-javadoc.jar", "-sources.jar", ".pom")
    }

    private val metafileName = "maven-metadata.xml"

    private fun isSnapshot(coordinate: Coordinate) = coordinate.versionWithVariant.isSnapshot()

    private fun metafile(base: String, coordinate: Coordinate): List<String> {
        return mutableListOf<String>().apply {
            add("$base/$metafileName")
            if (isSnapshot(coordinate)) add("$base/${coordinate.versionWithVariant}/$metafileName")
        }
    }

    private fun listPluginRepo(coordinate: Coordinate, versionTransformer: (String) -> String) =
        coordinate.pluginId?.let {
            listOf("$baseUrl/${coordinate.pluginId.replace('.', '/')}/${coordinate.pluginId}.gradle.plugin")
                .flatMap {
                    metafile(it, coordinate) +
                        "$it/${coordinate.versionWithVariant}/" +
                        "${coordinate.pluginId}.gradle.plugin-${versionTransformer(coordinate.versionWithVariant)}.pom"
                }
                .flatMap(::hashedPaths)
        } ?: listOf()

    private fun hashedPaths(path: String) = hashExts.map { hash -> "$path$hash" }

    private fun artifactTypes(path: String) = artifactClassifier.map { suffix -> "$path$suffix" }

    private fun versionTransformer(coordinate: Coordinate): (String) -> String {
        return if (coordinate.version.isSnapshot()) {
            snapshotVersionTransformer
        } else {
            releaseVersionTransformer
        }
    }

    fun list() = coordinates.flatMap { coordinate ->
        (
            listPluginRepo(coordinate, versionTransformer(coordinate)) +
                listOf("$baseUrl/${coordinate.group.replace('.', '/')}/${coordinate.artifactIdWithVariant}")
                    .flatMap { path ->
                        metafile(path, coordinate) +
                            listOf(
                                "$path/${coordinate.versionWithVariant}/" +
                                    "${coordinate.artifactIdWithVariant}-" +
                                    versionTransformer(coordinate)(coordinate.versionWithVariant)
                            )
                                .flatMap(::artifactTypes)
                                .flatMap { if (isSnapshot(coordinate)) listOf(it) else listOf(it, "$it.asc") }
                    }
                    .flatMap(::hashedPaths)
            )
            .map { Regex("$it.*") }
    }
}
