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

package io.hkhc.gradle.test.bintray

import io.hkhc.gradle.pom.internal.isSnapshot
import io.hkhc.gradle.test.Coordinate

class BintrayRepoPatterns(
    val coordinates: List<Coordinate>,
    val username: String,
    val repo: String,
    private val packaging: String,
    val withMetaData: Boolean = true
) {

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
                        // TODO Official released bintray Gradle plugin 1.8.5 has a bug that ignore the signature
                        // created by Signing plugin. Tha latest code in GitHub fixed the issue.
                        // So we do not check the .asc file here.
//                        .flatMap {
//                            if (isSnapshot(coordinate))
//                                listOf(it)
//                            else
//                                listOf(it, "${it}.asc")
//                        }
                } ?: listOf()
            }
        }
    }

    private fun artifactTypes(path: String) =
        (
            listOf(".$packaging", "-javadoc.jar", "-sources.jar", ".pom") +
                (if (withMetaData) listOf(".module") else listOf())
            )
            .map { suffix -> "$path$suffix" }

    fun list(versionTransformer: (String) -> String): List<Regex> = coordinates.flatMap { coordinate ->
        with(coordinate) {
            (
                listPluginRepo(pluginId, versionTransformer)
                    .map { "$it\\?override=1" } +
                    listOf(
                        "/content/$username/$repo/" +
                            "${coordinates[0].artifactIdWithVariant}/${coordinates[0].versionWithVariant}/" +
                            "${getPath()}/$artifactIdWithVariant-${versionTransformer(versionWithVariant)}"
                    )
                        .flatMap(::artifactTypes)
                        /* no signature for snapshot publishing */
                        .flatMap { if (isSnapshot(this)) listOf(it) else listOf(it, "$it.asc") }
                        .map { "$it\\?override=1" }
                )
        }
    }.map { Regex(it) }
}
