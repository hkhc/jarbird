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

import io.kotest.assertions.withClue
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.io.File

class ArtifactChecker {

    fun verifyRepository(repoDir: File, coordinate: Coordinate, packaging: String = "jar") {

        withClue("Repository directory $repoDir shall exists") {
            repoDir.exists() shouldBe true
        }

        val artifactPath = coordinate.getPath()
        val artifactDir = File(repoDir, artifactPath)

        withClue("Artifactory directory $artifactDir in Local repository shall exists") {
            artifactDir.exists() shouldBe true
        }

        withClue("All expected are in local repository $repoDir, and not more") {
            with(coordinate) {
                val files = artifactDir.listFiles()

                files.shouldNotBeNull()
                files.toList() shouldContainExactlyInAnyOrder
                    listOf(".module", ".pom", ".$packaging", "-javadoc.jar", "-sources.jar")
                        .flatMap {
                            listOf(
                                File(artifactDir, "${getFilenameBase()}$it"),
                                File(artifactDir, "${getFilenameBase()}$it.asc")
                            )
                        }
            }
        }
    }
}

