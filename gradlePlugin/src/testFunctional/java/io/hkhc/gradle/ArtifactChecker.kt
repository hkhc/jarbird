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

package io.hkhc.gradle

import org.junit.Assert
import java.io.File

class ArtifactChecker {

    fun verifyRepostory(repoDir: File, groupId: String, artifactId: String, version: String, packaging: String) {

        Assert.assertTrue(repoDir.exists())
        val artifactPath =
            groupId.replace('.', '/') + "/" +
                artifactId + "/" +
                version

        val artifactDir = File(repoDir, artifactPath)

        System.out.println("artifactDir $artifactDir")
        Assert.assertTrue(artifactDir.exists())

        val fileSet = artifactDir.listFiles().toMutableSet()

        verifyArtifact(fileSet, File(artifactDir, "$artifactId-$version.module"))
        verifyArtifact(fileSet, File(artifactDir, "$artifactId-$version.pom"))
        verifyArtifact(fileSet, File(artifactDir, "$artifactId-$version.jar"))
        verifyArtifact(fileSet, File(artifactDir, "$artifactId-$version-javadoc.jar"))
        verifyArtifact(fileSet, File(artifactDir, "$artifactId-$version-sources.jar"))

        Assert.assertTrue(fileSet.isEmpty())
    }

    fun verifyArtifact(fileList: MutableSet<File>, file: File) {

        Assert.assertTrue(file.exists())
        val signature = File(file.absolutePath + ".asc")
        Assert.assertTrue(signature.exists())

        fileList.remove(file)
        fileList.remove(signature)
    }
}
