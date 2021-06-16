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

package io.hkhc.gradle.test.gradlePortal

import io.hkhc.gradle.pom.internal.isSnapshot
import io.hkhc.gradle.test.Coordinate
import io.hkhc.utils.Path.Companion.relativePath

class GradlePortalRepoPatterns(
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

//    private val metafileName = "maven-metadata.xml"

    private fun isSnapshot(coordinate: Coordinate) = coordinate.versionWithVariant.isSnapshot()

//    private fun metafile(base: String, coordinate: Coordinate): List<String> {
//        return mutableListOf<String>().apply {
//            add("$base/$metafileName")
//            if (isSnapshot(coordinate)) add("$base/${coordinate.versionWithVariant}/$metafileName")
//        }
//    }

    private fun listPluginRepo(coor: Coordinate, versionTransformer: (String) -> String) =
        coor.pluginId?.let {
            listOf(
                relativePath(
                    baseUrl,
                    coor.pluginId.replace('.', '/'),
                    "${coor.pluginId}.gradle.plugin"
                ).toString()
            )
                .flatMap {
                    listOf(relativePath(
                        it,
                        coor.versionWithVariant,
                        "${coor.pluginId}.gradle.plugin-${versionTransformer(coor.versionWithVariant)}.pom"
                    ).toString()).flatMap {
                        if (isSnapshot(coor))
                            listOf(it)
                        else
                            listOf(it, "$it.asc")
                    }
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

    fun list() = coordinates.flatMap { coor ->
        (
//            listPluginRepo(coor, versionTransformer(coor)) +
                listOf(
                    relativePath(
                        baseUrl,
                        coor.group.replace('.', '/'),
                        coor.artifactIdWithVariant
                    ).toString()
                )
                    .flatMap { path ->
//                        metafile(path, coor) +
                            listOf(
                                relativePath(
                                    path,
                                    coor.versionWithVariant,
                                    "${coor.artifactIdWithVariant}-${versionTransformer(coor)(coor.versionWithVariant)}"
                                ).toString()
                            )
                                .flatMap(::artifactTypes)
                                .flatMap { if (isSnapshot(coor)) listOf(it) else listOf(it, "$it.asc") }
                    }
                    .flatMap(::hashedPaths)
            )
            .map { Regex("$it.*") }
    }
}
