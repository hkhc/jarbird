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

package io.hkhc.gradle.internal

import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.RepoSpec
import io.hkhc.gradle.internal.repo.ArtifactoryRepoSpec
import io.hkhc.gradle.internal.repo.MavenCentralRepoSpec
import io.hkhc.gradle.internal.repo.MavenRepoSpec
import io.hkhc.gradle.pom.Pom
import io.hkhc.utils.test.MockTaskContainer
import io.hkhc.utils.test.createSingleMockProject
import io.hkhc.utils.tree.NoBarTheme
import io.hkhc.utils.tree.defaultTreeTheme
import io.hkhc.utils.tree.stringTreeOf
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class TaskBuilderTest : FunSpec({

    beforeSpec {
        defaultTreeTheme = NoBarTheme
    }

    test("jbPublish") {

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
                    object : ArtifactoryRepoSpec {
                        override val description: String = "artifactory description"
                        override val id: String = "mock"
                        override val username: String = "username"
                        override val password: String = "password"
                        override val releaseUrl: String = "https://artifactory_release"
                        override val snapshotUrl: String = "https://artifactory_snapshot"
                        override val repoKey: String = "artifactory-repo"
                    },
                    object : MavenRepoSpec {
                        override val description: String = "maven description"
                        override val id: String = "mock"
                        override val username: String = "username"
                        override val password: String = "password"
                        override val isAllowInsecureProtocol: Boolean = false
                        override val releaseUrl: String = "https://maven_release"
                        override val snapshotUrl: String = "https://maven_snapshot"
                    },
                    object : MavenCentralRepoSpec {
                        override val description: String = "maven central description"
                        override val id: String = "mavencentral"
                        override val username: String = "username"
                        override val password: String = "password"
                        override val isAllowInsecureProtocol: Boolean = false
                        override val releaseUrl: String = "https://maven_central_release"
                        override val snapshotUrl: String = "https://maven_central_snapshot"
                        override val newUser: Boolean = true
                    }
                )
            }
        }

        val builder = TaskBuilder(project, pubs)
        builder.build()

        (project.tasks as MockTaskContainer).convertToTrees() shouldBe
            listOf(
                stringTreeOf {
                    "jbPublishPub" {
                        "jbPublishPubToMavenLocal"()
                        "jbPublishPubToMavenRepositories" {
                            "jbPublishPubToMock" {
                                "publishPubPublicationToMockRepository"()
                            }
                            "jbPublishPubToMavencentral" {
                                "publishPubPublicationToMavencentralRepository"()
                            }
                        }
                    }
                },
                stringTreeOf {
                    "jbPublish" {
                        "jbPublishToMavenLocal"()
                        "jbPublishToMavenRepositories" {
                            "jbPublishToMock" {
                                "jbPublishPubToMock" {
                                    "publishPubPublicationToMockRepository"()
                                }
                            }
                            "jbPublishToMavencentral" {
                                "jbPublishPubToMavencentral" {
                                    "publishPubPublicationToMavencentralRepository"()
                                }
                            }
                        }
                    }
                    "jbPublishToArtifactory" {
                        "artifactoryPublish"()
                    }
                }
            )
    }
})
