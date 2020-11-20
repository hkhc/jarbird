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

package io.hkhc.gradle.pom

import io.hkhc.gradle.pom.internal.PomGroupFactory
import io.hkhc.utils.test.tempDirectory
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.gradle.api.Project
import java.io.File

class PomGroupFactoryTest : StringSpec({

    "Load single file" {

        val baseDir = tempDirectory()

        val pomFile = File(baseDir, "pom1.yaml").apply {
            writeText(
                """
                group: mygroup
                artifactId: myArtifact
                version: 1.0
                """.trimIndent()
            )
        }

        val project = mockk<Project>()

        val pomGroup = PomGroupFactory(project).resolvePomGroup(listOf(pomFile))
        val pom = pomGroup.getDefault()

        withClue("Given single POM file") {
            pom.group shouldBe "mygroup"
            pom.artifactId shouldBe "myArtifact"
            pom.version shouldBe "1.0"
        }
    }

    "Load single file with variant" {

        val baseDir = tempDirectory()

        val pomFile = File(baseDir, "pom1.yaml").apply {
            writeText(
                """
                variant: release    
                group: mygroup
                artifactId: myArtifact
                version: 1.0
                """.trimIndent()
            )
        }

        val project = mockk<Project>()

        val pomGroup = PomGroupFactory(project).resolvePomGroup(listOf(pomFile))
        pomGroup.getMap() shouldHaveSize 1

        val pom = pomGroup["release"]

        withClue("Given single POM file") {
            pom.group shouldBe "mygroup"
            pom.artifactId shouldBe "myArtifact"
            pom.version shouldBe "1.0"
        }
    }
})
