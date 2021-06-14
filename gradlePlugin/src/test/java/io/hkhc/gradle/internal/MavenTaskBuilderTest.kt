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
import io.hkhc.gradle.internal.maven.MavenTaskBuilder
import io.hkhc.gradle.internal.repo.MavenLocalRepoSpecImpl
import io.hkhc.gradle.internal.repo.MavenRepoSpecImpl
import io.hkhc.gradle.pom.PluginInfo
import io.hkhc.gradle.pom.Pom
import io.hkhc.utils.test.MockTaskContainer
import io.hkhc.utils.test.createMockProjectTree
import io.hkhc.utils.tree.NoBarTheme
import io.hkhc.utils.tree.RoundTheme
import io.hkhc.utils.tree.Tree
import io.hkhc.utils.tree.defaultTreeTheme
import io.hkhc.utils.tree.stringTreeOf
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class MavenTaskBuilderTest : FunSpec( {

    beforeSpec {
        defaultTreeTheme = NoBarTheme
    }

    context("maven local") {

        test("single project without maven local repo") {
            val projectMap = createMockProjectTree(stringTreeOf(RoundTheme) {
                "app"()
            })
            val project = projectMap["app"]!!

            TaskInfo.eagar = true

            val pubs = List<JarbirdPub>(2) {
                mockk {
                    every { variant } returns ""
                    every { pubName } returns "pub${it + 1}"
                    every { pom } returns Pom()
                    every { getRepos() } returns setOf()
                }
            }
            val builder = MavenTaskBuilder(project, pubs)

            builder.registerMavenLocalTask(project.tasks)

            val mockTaskContainer = project.tasks as MockTaskContainer
            mockTaskContainer.mockTasks.shouldBeEmpty()
        }

        test("register maven local tasks for single project") {

            val projectMap = createMockProjectTree(stringTreeOf(RoundTheme) {
                "app"()
            })
            val project = projectMap["app"]!!

            TaskInfo.eagar = true

            val pubs = List<JarbirdPub>(2) {
                mockk {
                    every { variant } returns ""
                    every { pubName } returns "pub${it + 1}"
                    every { pom } returns Pom()
                    every { getRepos() } returns setOf<RepoSpec>(MavenLocalRepoSpecImpl())
                }
            }
            val builder = MavenTaskBuilder(project, pubs)

            builder.registerMavenLocalTask(project.tasks)

            (project.tasks as MockTaskContainer).convertToTrees() shouldBe
                listOf(stringTreeOf {
                    "jbPublishToMavenLocal" {
                        "jbPublishPub1ToMavenLocal" {
                            "publishPub1PublicationToMavenLocal"()
                        }
                        "jbPublishPub2ToMavenLocal" {
                            "publishPub2PublicationToMavenLocal"()
                        }
                    }
                })
        }

        test("register maven local tasks for single project with gradle plugin pub") {

            val projectMap = createMockProjectTree(stringTreeOf {
                "app"()
            })
            val project = projectMap["app"]!!

            TaskInfo.eagar = true

            val pubs = MutableList<JarbirdPub>(2) {
                mockk {
                    every { variant } returns ""
                    every { pubName } returns "pub${it + 1}"
                    every { pom } returns Pom()
                    every { getRepos() } returns setOf<RepoSpec>(MavenLocalRepoSpecImpl())
                }
            }
            pubs.add(
                mockk {
                    every { variant } returns ""
                    every { pubName } returns "pubPlugin"
                    every { pom } returns Pom(plugin = PluginInfo())
                    every { getRepos() } returns setOf<RepoSpec>(MavenLocalRepoSpecImpl())
                }
            )

            val builder = MavenTaskBuilder(project, pubs)

            builder.registerMavenLocalTask(project.tasks)

            (project.tasks as MockTaskContainer).convertToTrees() shouldBe
                listOf(stringTreeOf {
                    "jbPublishToMavenLocal" {
                        "jbPublishPub1ToMavenLocal" {
                            "publishPub1PublicationToMavenLocal"()
                        }
                        "jbPublishPub2ToMavenLocal" {
                            "publishPub2PublicationToMavenLocal"()
                        }
                        "jbPublishPubPluginToMavenLocal" {
                            "publishPubPluginPublicationToMavenLocal"()
                            "publishPubPluginPluginMarkerMavenPublicationToMavenLocal"()
                        }
                    }
                })

        }

        test("register maven local tasks for root project") {

            val projectMap = createMockProjectTree(stringTreeOf(RoundTheme) {
                "root" {
                    "app"()
                }
            })
            projectMap.keys.forEach {
                println("project key $it")
            }
            val rootProject = projectMap["root"]!!
            val childProject = projectMap["app"]!!

            TaskInfo.eagar = true

            val childPubs = List<JarbirdPub>(2) {
                mockk {
                    every { variant } returns ""
                    every { pubName } returns "pub${it + 1}"
                    every { pom } returns Pom()
                    every { getRepos() } returns setOf<RepoSpec>(MavenLocalRepoSpecImpl())
                }
            }

            val rootPubs = List<JarbirdPub>(1) {
                mockk {
                    every { variant } returns ""
                    every { pubName } returns "pub1"
                    every { pom } returns Pom()
                    every { getRepos() } returns setOf<RepoSpec>(MavenLocalRepoSpecImpl())
                }
            }

            val childBuilder = MavenTaskBuilder(childProject, childPubs)
            childBuilder.registerMavenLocalTask(childProject.tasks)

            val rootBuilder = MavenTaskBuilder(rootProject, rootPubs)
            rootBuilder.registerMavenLocalTask(rootProject.tasks)

            (childProject.tasks as MockTaskContainer).convertToTrees() shouldBe
                listOf(stringTreeOf {
                    "jbPublishToMavenLocal" {
                        "jbPublishPub1ToMavenLocal" {
                            "publishPub1PublicationToMavenLocal"()
                        }
                        "jbPublishPub2ToMavenLocal" {
                            "publishPub2PublicationToMavenLocal"()
                        }
                    }
                })

            (rootProject.tasks as MockTaskContainer).convertToTrees() shouldBe
                listOf(stringTreeOf {
                    "jbPublishToMavenLocal" {
                        "jbPublishPub1ToMavenLocal" {
                            "publishPub1PublicationToMavenLocal"()
                        }
                    }
                })
        }

    }

    context("Maven repo") {

        test("register maven repo tasks for single project") {

            val projectMap = createMockProjectTree(stringTreeOf(RoundTheme) {
                "app"()
            })
            val project = projectMap.get("app")!!

            TaskInfo.eagar = true

            // The first pub is normal pub and the second is for gradle plugin
            val pubs = List<JarbirdPubImpl>(2) {
                mockk {
                    every { variant } returns ""
                    every { pubName } returns "pub${it + 1}"
                    every { pom } returns if (it==0) Pom() else Pom(plugin = PluginInfo())
                    every { getRepos() } returns setOf(MavenRepoSpecImpl(
                        description = "description",
                        id = "mock"
                    ))
                }
            }
            val builder = MavenTaskBuilder(project, pubs)

            builder.registerMavenRepositoryTask(project.tasks)

            (project.tasks as MockTaskContainer).convertToTrees() shouldBe
                listOf(
                    stringTreeOf {
                        "jbPublishPub1ToMavenRepositories" {
                            "jbPublishPub1ToMock" {
                                "publishPub1PublicationToMockRepository"()
                            }
                        }
                    },
                    stringTreeOf {
                        "jbPublishPub2ToMavenRepositories" {
                            "jbPublishPub2ToMock" {
                                "publishPub2PublicationToMockRepository"()
                                "publishPub2PluginMarkerMavenPublicationToMockRepository"()
                            }
                        }
                    },
                    stringTreeOf {
                        "jbPublishToMavenRepositories" {
                            "jbPublishToMock" {
                                "jbPublishPub1ToMock" {
                                    "publishPub1PublicationToMockRepository"()
                                }
                                "jbPublishPub2ToMock" {
                                    "publishPub2PublicationToMockRepository"()
                                    "publishPub2PluginMarkerMavenPublicationToMockRepository"()
                                }
                            }
                        }
                    }
                )
        }

        test("register maven local tasks for single project with gradle plugin pub") {

            val projectMap = createMockProjectTree(stringTreeOf(RoundTheme) {
                "app"()
            })
            val project = projectMap["app"]!!

            TaskInfo.eagar = true

            val pubs = MutableList<JarbirdPub>(1) {
                mockk {
                    every { variant } returns ""
                    every { pubName } returns "pub"
                    every { pom } returns Pom(plugin = PluginInfo())
                    every { getRepos() } returns setOf<RepoSpec>(MavenRepoSpecImpl(
                        description = "description",
                        id = "mock"
                    ))
                }
            }

            val builder = MavenTaskBuilder(project, pubs)

            builder.registerMavenRepositoryTask(project.tasks)

            (project.tasks as MockTaskContainer).convertToTrees() shouldBe
                listOf(
                    stringTreeOf {
                        "jbPublishPubToMavenRepositories" {
                            "jbPublishPubToMock" {
                                "publishPubPublicationToMockRepository"()
                                "publishPubPluginMarkerMavenPublicationToMockRepository"()
                            }
                        }
                    },
                    stringTreeOf {
                        "jbPublishToMavenRepositories" {
                            "jbPublishToMock" {
                                "jbPublishPubToMock" {
                                    "publishPubPublicationToMockRepository"()
                                    "publishPubPluginMarkerMavenPublicationToMockRepository"()
                                }
                            }
                        }
                    }
                )
        }

        test("register two maven repo tasks for single project") {

            val projectMap = createMockProjectTree(stringTreeOf {
                "app"()
            })
            val project = projectMap.get("app")!!

            TaskInfo.eagar = true

            // The first pub is normal pub and the second is for gradle plugin
            val pubs = List<JarbirdPubImpl>(2) {
                mockk {
                    every { variant } returns ""
                    every { pubName } returns "pub${it + 1}"
                    every { pom } returns Pom()
                    every { getRepos() } returns setOf(
                        // mock0 is for all pubs
                        MavenRepoSpecImpl(
                            description = "description",
                            id = "mock0"
                        ),
                        // this make publication filter come to play that repo mock1 is for pub1 only, and mock2 is for
                        // pub2 only.
                        MavenRepoSpecImpl(
                            description = "description",
                            id = "mock${it+1}"
                        )
                    )
                }
            }
            val builder = MavenTaskBuilder(project, pubs)

            builder.registerMavenRepositoryTask(project.tasks)

            (project.tasks as MockTaskContainer).convertToTrees() shouldBe
                listOf(
                    stringTreeOf {
                        "jbPublishPub1ToMavenRepositories" {
                            "jbPublishPub1ToMock0" {
                                "publishPub1PublicationToMock0Repository"()
                            }
                            "jbPublishPub1ToMock1" {
                                "publishPub1PublicationToMock1Repository"()
                            }
                        }
                    },
                    stringTreeOf {
                        "jbPublishPub2ToMavenRepositories" {
                            "jbPublishPub2ToMock0" {
                                "publishPub2PublicationToMock0Repository"()
                            }
                            "jbPublishPub2ToMock2" {
                                "publishPub2PublicationToMock2Repository"()
                            }
                        }
                    },
                    stringTreeOf {
                        "jbPublishToMavenRepositories" {
                            "jbPublishToMock0" {
                                "jbPublishPub1ToMock0" {
                                    "publishPub1PublicationToMock0Repository"()
                                }
                                "jbPublishPub2ToMock0" {
                                    "publishPub2PublicationToMock0Repository"()
                                }
                            }
                            "jbPublishToMock1" {
                                "jbPublishPub1ToMock1" {
                                    "publishPub1PublicationToMock1Repository"()
                                }
                            }
                            "jbPublishToMock2" {
                                "jbPublishPub2ToMock2" {
                                    "publishPub2PublicationToMock2Repository"()
                                }
                            }
                        }
                    }
                )
        }

        test("register maven repo tasks for multi project") {

            val projectMap = createMockProjectTree(stringTreeOf {
                "root" {
                    "app"()
                }
            })
            val rootProject = projectMap["root"]!!
            val childProject = projectMap["app"]!!

            TaskInfo.eagar = true

            // The first pub is normal pub and the second is for gradle plugin
            val childPubs = List<JarbirdPubImpl>(2) {
                mockk {
                    every { variant } returns ""
                    every { pubName } returns "pub${it + 1}"
                    every { pom } returns if (it==0) Pom() else Pom(plugin = PluginInfo())
                    every { getRepos() } returns setOf<RepoSpec>(MavenRepoSpecImpl(
                        description = "description",
                        id = "mock"
                    ))
                }
            }
            val childBuilder = MavenTaskBuilder(childProject, childPubs)
            childBuilder.registerMavenRepositoryTask(childProject.tasks)

            val rootPubs = List<JarbirdPubImpl>(1) {
                mockk {
                    every { variant } returns ""
                    every { pubName } returns "pub1"
                    every { pom } returns Pom()
                    every { getRepos() } returns setOf<RepoSpec>(MavenRepoSpecImpl(
                        description = "description",
                        id = "mock"
                    ))
                }
            }
            val rootBuilder = MavenTaskBuilder(rootProject, rootPubs)
            rootBuilder.registerMavenRepositoryTask(rootProject.tasks)

            (childProject.tasks as MockTaskContainer).convertToTrees() shouldBe
                listOf(
                    stringTreeOf {
                        "jbPublishPub1ToMavenRepositories" {
                            "jbPublishPub1ToMock" {
                                "publishPub1PublicationToMockRepository"()
                            }
                        }
                    },
                    stringTreeOf {
                        "jbPublishPub2ToMavenRepositories" {
                            "jbPublishPub2ToMock" {
                                "publishPub2PublicationToMockRepository"()
                                "publishPub2PluginMarkerMavenPublicationToMockRepository"()
                            }
                        }
                    },
                    stringTreeOf {
                        "jbPublishToMavenRepositories" {
                            "jbPublishToMock" {
                                "jbPublishPub1ToMock" {
                                    "publishPub1PublicationToMockRepository"()
                                }
                                "jbPublishPub2ToMock" {
                                    "publishPub2PublicationToMockRepository"()
                                    "publishPub2PluginMarkerMavenPublicationToMockRepository"()
                                }
                            }
                        }
                    }
                )

            (rootProject.tasks as MockTaskContainer).convertToTrees() shouldBe
                listOf(
                    stringTreeOf {
                        "jbPublishPub1ToMavenRepositories" {
                            "jbPublishPub1ToMock" {
                                "publishPub1PublicationToMockRepository"()
                            }
                        }
                    },
                    stringTreeOf {
                        "jbPublishToMavenRepositories" {
                            "jbPublishToMock" {
                                "jbPublishPub1ToMock" {
                                    "publishPub1PublicationToMockRepository"()
                                }
                            }
                        }
                    }
                )
        }
    }
})
