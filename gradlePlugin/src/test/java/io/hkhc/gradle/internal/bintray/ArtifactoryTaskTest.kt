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

package io.hkhc.gradle.internal.bintray

import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.RepoSpec
import io.hkhc.gradle.internal.TaskInfo
import io.hkhc.gradle.internal.convertToTrees
import io.hkhc.gradle.internal.repo.ArtifactoryRepoSpec
import io.hkhc.gradle.pom.Pom
import io.hkhc.utils.test.MockTaskContainer
import io.hkhc.utils.test.createSingleMockProject
import io.hkhc.utils.tree.stringTreeOf
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class ArtifactoryTaskTest : FunSpec({

    context("publish to artifactory") {

        test("single project one pub to gradle portal") {
            val project = createSingleMockProject("app")

            TaskInfo.eagar = true

            val pubs = List<JarbirdPub>(1) {
                mockk {
                    every { variant } returns ""
                    every { pubName } returns "pub"
                    every { pom } returns Pom(
                        group = "group",
                        artifactId = "mylib",
                        version = "1.0"
                    )
                    every { getGAV() } returns "group:mylib:1.0"
                    every { getRepos() } returns setOf<RepoSpec>(
                        mockk<ArtifactoryRepoSpec> {
                            every { id } returns "mock"
                        }
                    )
                }
            }

            val builder = ArtifactoryTaskBuilder(project, pubs)
            builder.registerArtifactoryTask()

            (project.tasks as MockTaskContainer).convertToTrees() shouldBe
                listOf(
                    stringTreeOf {
                        "jbPublishToArtifactory" {
                            "artifactoryPublish"()
                        }
                    }
                )
        }
    }
})
