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

import com.gradle.publish.MavenCoordinates
import com.gradle.publish.PluginBundleExtension
import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.RepoSpec
import io.hkhc.gradle.internal.repo.GradlePortalSpecImpl
import io.hkhc.gradle.internal.repo.MavenLocalRepoSpecImpl
import io.hkhc.gradle.pom.PluginInfo
import io.hkhc.gradle.pom.Pom
import io.hkhc.gradle.pom.Scm
import io.hkhc.gradle.pom.Web
import io.hkhc.utils.removeLineBreak
import io.hkhc.utils.test.MockExtensionContainer
import io.hkhc.utils.test.createMockProjectTree
import io.hkhc.utils.test.createSingleMockProject
import io.hkhc.utils.tree.NoBarTheme
import io.hkhc.utils.tree.defaultTreeTheme
import io.hkhc.utils.tree.stringTreeOf
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.kotlin.dsl.invoke
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.gradle.plugin.devel.PluginDeclaration

class PluginPublishingConfigTest : FunSpec({

    beforeSpec {
        defaultTreeTheme = NoBarTheme
    }

    lateinit var project: Project

    beforeTest {
        project = createSingleMockProject("app")
    }

    context("PluginPublishingModel.filterGradlePluginPubs") {

        test("empty pub list") {
            PluginPublishingModel.filterGradlePluginPub(project, listOf()) shouldBe listOf()
        }

        test("pub list with no gradle plugin portal declaration") {

            val pubs = List<JarbirdPub>(1) {
                mockk {
                    every { variant } returns ""
                    every { pubName } returns "pub"
                    every { pom } returns Pom()
                    every { getRepos() } returns setOf<RepoSpec>()
                }
            }

            PluginPublishingModel.filterGradlePluginPub(project, pubs) shouldBe listOf()
        }

        test("pub list with two gradle plugin portal declarations") {

            var pubs = List<JarbirdPub>(2) {
                mockk {
                    every { variant } returns ""
                    every { pubName } returns "pub${it + 1}"
                    every { pom } returns Pom(plugin = PluginInfo())
                    every { getRepos() } returns setOf<RepoSpec>(GradlePortalSpecImpl())
                }
            }

            shouldThrowExactly<IllegalArgumentException> {
                PluginPublishingModel.filterGradlePluginPub(project, pubs)
            }.message shouldBe "Plugin ID is not specified for pub pub1"

            pubs = List<JarbirdPub>(2) {
                mockk {
                    every { variant } returns ""
                    every { pubName } returns "pub${it + 1}"
                    every { pom } returns Pom(plugin = PluginInfo(id = "plugin${it + 1}"))
                    every { getRepos() } returns setOf<RepoSpec>(GradlePortalSpecImpl())
                }
            }

            shouldThrowExactly<IllegalArgumentException> {
                PluginPublishingModel.filterGradlePluginPub(project, pubs)
            }.message shouldBe "Plugin implementation class is not specified for pub pub1"
        }

        test("one of the pubs with gradle plugin portal declaration") {

            var pubs = List<JarbirdPub>(2) {
                mockk {
                    every { variant } returns ""
                    every { pubName } returns "pub${it + 1}"
                    every { pom } returns if (it == 0) {
                        Pom()
                    } else {
                        Pom(plugin = PluginInfo(id = "plugin${it + 1}", implementationClass = "myClass${it + 1}"))
                    }
                    every { getRepos() } returns
                        if (it == 0) setOf<RepoSpec>(MavenLocalRepoSpecImpl())
                        else setOf<RepoSpec>(GradlePortalSpecImpl())
                }
            }

            PluginPublishingModel.filterGradlePluginPub(project, pubs) shouldBe listOf(pubs[1])
        }

        test("two pubs with gradle plugin portal declaration") {

            var pubs = List<JarbirdPub>(2) {
                mockk {
                    every { variant } returns ""
                    every { pubName } returns "pub${it + 1}"
                    every { pom } returns Pom(
                        plugin = PluginInfo(
                            id = "plugin${it + 1}",
                            implementationClass = "myClass${it + 1}"
                        )
                    )
                    every { getRepos() } returns setOf<RepoSpec>(GradlePortalSpecImpl())
                }
            }

            PluginPublishingModel.filterGradlePluginPub(project, pubs) shouldBe pubs

            verify {
                project.logger.warn(
                    """
                    $LOG_PREFIX More than one pub are declared to perform gradlePluginPublishing 
                    (pub1, pub2). 
                    Only the first one will be published to Gradle Plugin Portal.
                    """.trimIndent().removeLineBreak(ensureSpaceWithMerge = true)
//                    """
//                        $LOG_PREFIX More than one pub are declared to perform gradlePluginPublishing
//                        (pub1, pub2). Only the first one will be published to Gradle Plugin Portal.
//                    """.trimIndent().removeLineBreak(ensureSpaceWithMerge = true)
                )
            }
        }

        test("create plugin entries") {

            var pubs = List<JarbirdPub>(2) {
                mockk {
                    every { variant } returns ""
                    every { pubName } returns "pub${it + 1}"
                    every { pom } returns if (it == 0) Pom() else Pom(
                        group = "myGroup",
                        artifactId = "mylib",
                        version = "1.0",
                        plugin = PluginInfo(
                            id = "plugin${it + 1}",
                            implementationClass = "myClass${it + 1}",
                            displayName = "displayName"
                        ),
                        description = "description"
                    )
                    every { getRepos() } returns
                        if (it == 0) setOf<RepoSpec>(MavenLocalRepoSpecImpl())
                        else setOf<RepoSpec>(GradlePortalSpecImpl())
                }
            }

            val pluginPubs = PluginPublishingModel.filterGradlePluginPub(project, pubs)
            val entries = PluginPublishingModel.createPluginEntries(pluginPubs)

            entries shouldBe listOf(
                PluginPublishingModel.PluginEntry(
                    id = "plugin2",
                    pubName = "pub2",
                    implementationClass = "myClass2",
                    group = "myGroup",
                    artifactId = "mylib",
                    version = "1.0",
                    displayName = "displayName",
                    description = "description"
                )
            )
        }
    }

    context("plugin publishing model") {

        test("non gradle plugin declaration") {

            val pubs = List<JarbirdPub>(1) {
                mockk {
                    every { variant } returns ""
                    every { pubName } returns "pub"
                    every { pom } returns Pom()
                    every { getRepos() } returns setOf<RepoSpec>()
                }
            }

            shouldThrowExactly<AssertionError> {
                PluginPublishingModel(project, pubs)
            }
        }

        test("with gradle plugin declaration and no plugin info") {

            val pubs = List<JarbirdPub>(1) {
                mockk {
                    every { variant } returns ""
                    every { pubName } returns "pub"
                    every { pom } returns Pom()
                    every { getRepos() } returns setOf<RepoSpec>(GradlePortalSpecImpl())
                }
            }

            shouldThrowExactly<AssertionError> {
                PluginPublishingModel(project, pubs)
            }
        }
    }

    context("plugin publishing config") {

        val mockPluginDeclaration: PluginDeclaration = mockk(relaxUnitFun = true)
        val mockPluginDeclarationCollection: NamedDomainObjectContainer<PluginDeclaration> = mockk {
            every { maybeCreate(any()) } returns mockPluginDeclaration
        }

        val mockGradlePluginDevelopmentExtension =
            mockk<GradlePluginDevelopmentExtension>(relaxUnitFun = true) {
                every { plugins(any()) } answers {
                    val block = firstArg<Action<NamedDomainObjectContainer<PluginDeclaration>>>()
                    block.invoke(mockPluginDeclarationCollection)
                }
            }

        val mockMavenCoordinates: MavenCoordinates = mockk(relaxUnitFun = true)
        val mockPluginConfig: com.gradle.publish.PluginConfig = mockk(relaxUnitFun = true) { }
        val mockPluginConfigCollection: NamedDomainObjectContainer<com.gradle.publish.PluginConfig> = mockk {
            every { maybeCreate(any()) } returns mockPluginConfig
        }

        val mockPluginBundleExtension =
            mockk<PluginBundleExtension>(relaxUnitFun = true) {
                every {
                    mavenCoordinates(any<Action<MavenCoordinates>>())
                } answers {
                    val block = firstArg<Action<MavenCoordinates>>()
                    block.invoke(mockMavenCoordinates)
                }
                every {
                    plugins(any<Action<NamedDomainObjectContainer<com.gradle.publish.PluginConfig>>>())
                } answers {
                    val block = firstArg<Action<NamedDomainObjectContainer<com.gradle.publish.PluginConfig>>>()
                    block.invoke(mockPluginConfigCollection)
                }
            }

        test("GradlePluginDevelopmentExtension") {

            (project.extensions as MockExtensionContainer).mockExtensions = mapOf(
                GradlePluginDevelopmentExtension::class.java to mockGradlePluginDevelopmentExtension,
                PluginBundleExtension::class.java to mockPluginBundleExtension
            )

            TaskInfo.eagar = true

            val pubs = List<JarbirdPub>(1) {
                mockk {
                    every { variant } returns ""
                    every { pubName } returns "mypub"
                    val mockKAdditionalAnswerScope = every { pom } returns Pom(
                        group = "group",
                        artifactId = "mylib",
                        version = "1.0",
                        description = "description",
                        web = Web(
                            url = "http://web"
                        ),
                        scm = Scm(
                            url = "http://scm"
                        ),
                        plugin = PluginInfo(
                            id = "plugin.id",
                            implementationClass = "myClass",
                            displayName = "plugin display name",
                            tags = mutableListOf("publish")
                        )
                    )
                    every { getRepos() } returns setOf<RepoSpec>(GradlePortalSpecImpl())
                }
            }

            val builder = PluginPublishingConfig(project, pubs)
            builder.config()

            val gradlePluginExt = (project.extensions as MockExtensionContainer)
                .mockExtensions[GradlePluginDevelopmentExtension::class.java] as GradlePluginDevelopmentExtension

            verify { gradlePluginExt.isAutomatedPublishing = false }
            verify { mockPluginDeclarationCollection.maybeCreate("mypub") }
            verify { mockPluginDeclaration.id = "plugin.id" }
            verify { mockPluginDeclaration.implementationClass = "myClass" }

            verify { mockMavenCoordinates.group = "group" }
            verify { mockMavenCoordinates.artifactId = "mylib" }
            verify { mockMavenCoordinates.version = "1.0" }
            verify { mockMavenCoordinates.group = "group" }

            verify { mockPluginBundleExtension.website = "http://web" }
            verify { mockPluginBundleExtension.vcsUrl = "http://scm" }
            verify {
                mockPluginBundleExtension.description =
                    "description" /* pom.plugin.description fallback to pom.description */
            }
            verify { mockPluginBundleExtension.tags = listOf("publish") }

            verify { mockPluginConfigCollection.maybeCreate("mypub") }
        }
    }
})
