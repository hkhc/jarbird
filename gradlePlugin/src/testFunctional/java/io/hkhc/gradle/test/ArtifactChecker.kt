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
import org.junit.Assert
import java.io.File

class ArtifactChecker {

    fun verifyRepository(repoDir: File, coordinate: Coordinate, packaging: String) {

        Assert.assertTrue(repoDir.exists())
        val artifactPath =
            coordinate.group.replace('.', '/') + "/" +
                coordinate.artifactId + "/" +
                coordinate.versionWithVariant

        val artifactDir = File(repoDir, artifactPath)

        Assert.assertTrue("The artifact directory $artifactDir in local repo shall exist", artifactDir.exists())

        val fileSet = artifactDir.listFiles().toMutableSet()

        fileSet.forEach {
            System.out.println("file set = ${it.absolutePath}")
        }

        withClue("In local repository $repoDir") {
            with(coordinate) {
                listOf(".module", ".pom", ".$packaging", "-javadoc.jar", "-sources.jar")
                    .map { "$artifactId-$versionWithVariant$it" }
                    .forEach {
                        withClue("The generated file $it should presents ") {
                            System.out.println("check file ${File(artifactDir, it).absolutePath}")
                            verifyArtifact(fileSet, File(artifactDir, it))
                        }
                    }
            }
        }

        withClue("With no extra file left out") {
            Assert.assertTrue(fileSet.isEmpty())
        }
    }

    fun verifyArtifact(fileList: MutableSet<File>, file: File) {

        Assert.assertTrue("File ${file.absolutePath} should exists", file.exists())
        val signature = File(file.absolutePath + ".asc")
        Assert.assertTrue("Signature file ${signature.absolutePath} should exists", signature.exists())

        fileList.remove(file)
        fileList.remove(signature)
    }
}
