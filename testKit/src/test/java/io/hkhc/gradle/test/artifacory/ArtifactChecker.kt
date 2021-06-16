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

package io.hkhc.gradle.test.artifacory

import io.hkhc.gradle.pom.internal.isSnapshot
import io.hkhc.gradle.test.Coordinate
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.nulls.shouldNotBeNull
import java.io.File

class ArtifactChecker {

    fun verifyPluginMarkerRepository(repoDir: File, pluginMarkerCoordinate: Coordinate) {

        withClue("Repository directory '$repoDir' shall exists") {
            repoDir.shouldExist()
        }

        val artifactPath = pluginMarkerCoordinate.getPluginPath()
        val artifactDir = File(repoDir, artifactPath)

        withClue("Artifact directory '$artifactDir' in Local repository shall exists") {
            artifactDir.shouldExist()
        }

        withClue("Local repository '$repoDir' + '$artifactDir' shall contains deployed files, and not more") {
            with(pluginMarkerCoordinate) {
                val files = artifactDir.listFiles()?.map { it.relativeTo(artifactDir.parentFile) } ?: listOf()

                var expectedFileList = (
                    listOf(".pom")
                        .flatMap {
                            if (pluginMarkerCoordinate.version.isSnapshot()) {
                                listOf(
                                    File("$versionWithVariant/$pluginId.gradle.plugin-$versionWithVariant$it")
                                )
                            } else {
                                listOf(
                                    File("$versionWithVariant/$pluginId.gradle.plugin-$versionWithVariant$it"),
                                    File("$versionWithVariant/$pluginId.gradle.plugin-$versionWithVariant$it.asc")
                                )
                            }
                        }
                    )

                if (version.isSnapshot()) {
                    expectedFileList += listOf(File("$versionWithVariant/maven-metadata-local.xml"))
                }

                files.shouldNotBeNull()
                files shouldContainExactlyInAnyOrder expectedFileList
            }
        }
    }

    fun verifyRepository(repoDir: File, coordinate: Coordinate, packaging: String) {

        withClue("Repository directory '$repoDir' shall exists") {
            repoDir.shouldExist()
        }

        val artifactPath = coordinate.getPath()
        val artifactDir = File(repoDir, artifactPath)

        withClue("Artifact directory '$artifactDir' in Local repository shall exists") {
            artifactDir.shouldExist()
        }

        withClue("Local repository '$repoDir' shall contains deployed files, and not more") {
            with(coordinate) {
                val files = artifactDir.listFiles()?.map { it.relativeTo(artifactDir.parentFile) } ?: listOf()

                var expectedFileList = (
                    listOf(".module", ".pom", ".$packaging", "-javadoc.jar", "-sources.jar")
                        .flatMap {
                            if (coordinate.version.isSnapshot()) {
                                listOf(
                                    File("$versionWithVariant/${getFilenameBase()}$it")
                                )
                            } else {
                                listOf(
                                    File("$versionWithVariant/${getFilenameBase()}$it"),
                                    File("$versionWithVariant/${getFilenameBase()}$it.asc")
                                )
                            }
                        }
                    )

                if (version.isSnapshot()) {
                    expectedFileList += listOf(File("$versionWithVariant/maven-metadata-local.xml"))
                }

                files.shouldNotBeNull()
                files shouldContainExactlyInAnyOrder expectedFileList
            }
        }
    }
}
