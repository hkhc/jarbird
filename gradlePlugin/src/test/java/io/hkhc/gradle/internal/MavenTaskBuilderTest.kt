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
import io.hkhc.gradle.internal.maven.MavenTaskBuilder
import io.hkhc.gradle.internal.repo.MavenRepoSpecImpl
import io.hkhc.gradle.pom.PluginInfo
import io.hkhc.gradle.pom.Pom
import io.hkhc.utils.tree.RoundTheme
import io.hkhc.utils.tree.stringTreeOf
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.Task
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.specs.Spec

class MavenTaskBuilderTest : FunSpec( {

    context("maven local") {

        test("register maven local tasks for single project") {

            val projectMap = createMockProjectTree(stringTreeOf(RoundTheme) {
                "app"()
            })
            val project = projectMap.get("app")!!

            TaskInfo.eagar = true

            val pubs = List<JarbirdPub>(2) {
                mockk {
                    every { variant } returns ""
                    every { pubName } returns "pub${it + 1}"
                    every { pom } returns Pom()
                }
            }
            val builder = MavenTaskBuilder(project, pubs)

            builder.registerMavenLocalTask(project.tasks)

            val mockTaskContainer = project.tasks as MockTaskContainer
            mockTaskContainer.size shouldBe 3
            assertSoftly(mockTaskContainer.mockTasks[0] as MockTask) {
                name shouldBe "jbPublishToMavenLocal"
                dependsOn shouldBe setOf(mockTaskContainer.mockTasks[1].name, mockTaskContainer.mockTasks[2].name)
            }
            assertSoftly(mockTaskContainer.mockTasks[1] as MockTask) {
                name shouldBe "jbPublishPub1ToMavenLocal"
                dependsOn shouldBe setOf("publishPub1PublicationToMavenLocal")
            }
            assertSoftly(mockTaskContainer.mockTasks[2] as MockTask) {
                name shouldBe "jbPublishPub2ToMavenLocal"
                dependsOn shouldBe setOf("publishPub2PublicationToMavenLocal")
            }

        }

        test("register maven local tasks for single project with gradle plugin pub") {

            val projectMap = createMockProjectTree(stringTreeOf(RoundTheme) {
                "app"()
            })
            val project = projectMap.get("app")!!

            TaskInfo.eagar = true

            val pubs = MutableList<JarbirdPub>(2) {
                mockk {
                    every { variant } returns ""
                    every { pubName } returns "pub${it + 1}"
                    every { pom } returns Pom()
                }
            }
            pubs.add(
                mockk {
                    every { variant } returns ""
                    every { pubName } returns "pubPlugin"
                    every { pom } returns Pom(plugin = PluginInfo())
                }
            )

            val builder = MavenTaskBuilder(project, pubs)

            builder.registerMavenLocalTask(project.tasks)

            val mockTaskContainer = project.tasks as MockTaskContainer
            mockTaskContainer.size shouldBe 4
            assertSoftly(mockTaskContainer.mockTasks[0] as MockTask) {
                name shouldBe "jbPublishToMavenLocal"
                dependsOn shouldBe setOf(
                    mockTaskContainer.mockTasks[1].name,
                    mockTaskContainer.mockTasks[2].name,
                    mockTaskContainer.mockTasks[3].name
                )
            }
            assertSoftly(mockTaskContainer.mockTasks[1] as MockTask) {
                name shouldBe "jbPublishPub1ToMavenLocal"
                dependsOn shouldBe setOf("publishPub1PublicationToMavenLocal")
            }
            assertSoftly(mockTaskContainer.mockTasks[2] as MockTask) {
                name shouldBe "jbPublishPub2ToMavenLocal"
                dependsOn shouldBe setOf("publishPub2PublicationToMavenLocal")
            }
            assertSoftly(mockTaskContainer.mockTasks[3] as MockTask) {
                name shouldBe "jbPublishPubPluginToMavenLocal"
                dependsOn shouldBe setOf(
                    "publishPubPluginPublicationToMavenLocal",
                    "publishPubPluginPluginMarkerMavenPublicationToMavenLocal"
                )
            }

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
            val rootProject = projectMap.get("root")!!
            val childProject = projectMap.get("app")!!

            TaskInfo.eagar = true

            val pubs = List<JarbirdPub>(2) {
                mockk {
                    every { variant } returns ""
                    every { pubName } returns "pub${it + 1}"
                    every { pom } returns Pom()
                }
            }
            val childBuilder = MavenTaskBuilder(childProject, pubs)
            childBuilder.registerMavenLocalTask(childProject.tasks)

            val rootBuilder = MavenTaskBuilder(rootProject, listOf())
            rootBuilder.registerMavenLocalTask(rootProject.tasks)

            val mockTaskContainer = childProject.tasks as MockTaskContainer
            mockTaskContainer.size shouldBe 3
            assertSoftly(mockTaskContainer.mockTasks[0] as MockTask) {
                name shouldBe "jbPublishToMavenLocal"
                dependsOn shouldBe setOf(
                    mockTaskContainer.mockTasks[1].name,
                    mockTaskContainer.mockTasks[2].name
                )
            }
            assertSoftly(mockTaskContainer.mockTasks[1] as MockTask) {
                name shouldBe "jbPublishPub1ToMavenLocal"
                dependsOn shouldBe setOf("publishPub1PublicationToMavenLocal")
            }
            assertSoftly(mockTaskContainer.mockTasks[2] as MockTask) {
                name shouldBe "jbPublishPub2ToMavenLocal"
                dependsOn shouldBe setOf("publishPub2PublicationToMavenLocal")
            }

            val mockRootTaskContainer = rootProject.tasks as MockTaskContainer

            assertSoftly(mockRootTaskContainer.mockTasks[0] as MockTask) {
                name shouldBe "jbPublishToMavenLocal"
                dependsOn shouldBe setOf(mockTaskContainer.mockTasks[0].path)
            }

        }

    }

    context("Maven repo") {

        test("register maven repo tasks for single project") {

            val projectMap = createMockProjectTree(stringTreeOf(RoundTheme) {
                "app"()
            })
            val project = projectMap.get("app")!!

            (project.tasks as MockTaskContainer).mockWithTypeTask = mockk<PublishToMavenRepository>(relaxed = true) {
                val task = this
                every { onlyIf(any<Spec<Task>>()) } answers {
                    val action = firstArg<Spec<Task>>()
                    action.isSatisfiedBy(task)
                }
            }

            TaskInfo.eagar = true

            // The first pub is normal pub and the second is for gradle plugin
            val pubs = List<JarbirdPubImpl>(2) {
                mockk {
                    every { variant } returns ""
                    every { pubName } returns "pub${it + 1}"
                    every { pom } returns if (it==0) Pom() else Pom(plugin = PluginInfo())
                    every { getRepos() } returns setOf(MavenRepoSpecImpl(
                        releaseUrl = "https://release",
                        snapshotUrl = "https://snapshot",
                        username = "username",
                        password = "password",
                        description = "description",
                        id = "mock"
                    ))
                }
            }
            val builder = MavenTaskBuilder(project, pubs)

            builder.registerMavenRepositoryTask(project.tasks)

            val mockTaskContainer = project.tasks as MockTaskContainer
            mockTaskContainer.size shouldBe 6
            assertSoftly(mockTaskContainer.mockTasks[0] as MockTask) {
                name shouldBe "jbPublishPub1ToMock"
                dependsOn shouldBe setOf("publishPub1PublicationToMockRepository")
            }
            assertSoftly(mockTaskContainer.mockTasks[1] as MockTask) {
                name shouldBe "jbPublishPub1ToMavenRepositories"
                dependsOn shouldBe setOf("jbPublishPub1ToMock")
            }
            assertSoftly(mockTaskContainer.mockTasks[2] as MockTask) {
                name shouldBe "jbPublishPub2ToMock"
                dependsOn shouldBe setOf(
                    "publishPub2PublicationToMockRepository",
                    "publishPub2PluginMarkerMavenPublicationToMockRepository"
                )
            }
            assertSoftly(mockTaskContainer.mockTasks[3] as MockTask) {
                name shouldBe "jbPublishPub2ToMavenRepositories"
                dependsOn shouldBe setOf("jbPublishPub2ToMock")
            }
            assertSoftly(mockTaskContainer.mockTasks[4] as MockTask) {
                name shouldBe "jbPublishToMavenRepositories"
                dependsOn shouldBe setOf("jbPublishPub1ToMavenRepositories", "jbPublishPub2ToMavenRepositories")
            }
            assertSoftly(mockTaskContainer.mockTasks[5] as MockTask) {
                name shouldBe "jbPublishToMock"
                dependsOn shouldBe setOf("jbPublishPub1ToMock", "jbPublishPub2ToMock")
            }
        }
    }

    xtest("register maven repo tasks for single project") {

        val projectMap = createMockProjectTree(stringTreeOf(RoundTheme) {
            "root" {
                "app"()
            }
        })
        val rootProject = projectMap.get("root")!!
        val childProject = projectMap.get("app")!!

        TaskInfo.eagar = true

        // The first pub is normal pub and the second is for gradle plugin
        val pubs = List<JarbirdPubImpl>(2) {
            mockk {
                every { variant } returns ""
                every { pubName } returns "pub${it + 1}"
                every { pom } returns if (it==0) Pom() else Pom(plugin = PluginInfo())
                every { getRepos() } returns setOf(MavenRepoSpecImpl(
                    releaseUrl = "https://release",
                    snapshotUrl = "https://snapshot",
                    username = "username",
                    password = "password",
                    description = "description",
                    id = "mock"
                ))
            }
        }
        val childBuilder = MavenTaskBuilder(childProject, pubs)
        childBuilder.registerMavenLocalTask(childProject.tasks)

        val rootBuilder = MavenTaskBuilder(rootProject, listOf())
        rootBuilder.registerMavenLocalTask(rootProject.tasks)

        val mockChildTaskContainer = childProject.tasks as MockTaskContainer
        mockChildTaskContainer.size shouldBe 6
        assertSoftly(mockChildTaskContainer.mockTasks[0] as MockTask) {
            name shouldBe "jbPublishPub1ToMock"
            dependsOn shouldBe setOf("publishPub1PublicationToMockRepository")
        }
        assertSoftly(mockChildTaskContainer.mockTasks[1] as MockTask) {
            name shouldBe "jbPublishPub1ToMavenRepositories"
            dependsOn shouldBe setOf("jbPublishPub1ToMock")
        }
        assertSoftly(mockChildTaskContainer.mockTasks[2] as MockTask) {
            name shouldBe "jbPublishPub2ToMock"
            dependsOn shouldBe setOf(
                "publishPub2PublicationToMockRepository",
                "publishPub2PluginMarkerMavenPublicationToMockRepository"
            )
        }
        assertSoftly(mockChildTaskContainer.mockTasks[3] as MockTask) {
            name shouldBe "jbPublishPub2ToMavenRepositories"
            dependsOn shouldBe setOf("jbPublishPub2ToMock")
        }
        assertSoftly(mockChildTaskContainer.mockTasks[4] as MockTask) {
            name shouldBe "jbPublishToMavenRepositories"
            dependsOn shouldBe setOf("jbPublishPub1ToMavenRepositories", "jbPublishPub2ToMavenRepositories")
        }
        assertSoftly(mockChildTaskContainer.mockTasks[5] as MockTask) {
            name shouldBe "jbPublishToMock"
            dependsOn shouldBe setOf("jbPublishPub1ToMock", "jbPublishPub2ToMock")
        }

        val mockRootTaskContainer = rootProject.tasks as MockTaskContainer
        mockRootTaskContainer.size shouldBe 1
        assertSoftly(mockRootTaskContainer.mockTasks[0] as MockTask) {
            name shouldBe "jbPublishPub1ToMock"
            dependsOn shouldBe setOf("publishPub1PublicationToMockRepository")
        }
    }
})
