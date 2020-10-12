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

class ArtifactoryRepoPatterns(
    val coordinate: Coordinate,
    val username: String,
    val repo: String
) {

    val isSnapshot = coordinate.version.endsWith("-SNAPSHOT")
    val METADATA_FILE = "maven-metadata.xml"

    fun artifactTypes(path: String) =
        listOf(".jar", "-javadoc.jar", "-sources.jar", ".module", ".pom")
            .map { suffix -> "$path$suffix" }

    fun list(versionTransformer: (String) -> String) = with(coordinate) {
        (
            listOf(
                "/base/oss-snapshot-local/" +
                    "${group.replace('.', '/')}/" +
                    "$artifactId/$version/" +
                    "$artifactId-$version"
            )
                .flatMap(::artifactTypes)
                .flatMap { if (isSnapshot) listOf(it) else listOf(it, "$it.asc") }
                .map { "$it;.*" }
            )
            .map { Regex(it) }
    }
}
