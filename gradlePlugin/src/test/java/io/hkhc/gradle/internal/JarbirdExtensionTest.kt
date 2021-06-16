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

package io.hkhc.gradle.internal

import io.hkhc.gradle.internal.repo.ArtifactoryRepoSpec
import io.hkhc.gradle.internal.repo.MavenLocalRepoSpecImpl
import io.hkhc.gradle.internal.repo.MavenRepoSpec
import io.hkhc.gradle.internal.repo.PropertyRepoSpecBuilder
import io.hkhc.utils.test.MockProjectProperty
import io.hkhc.utils.test.tempDirectory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import java.io.File

class JarbirdExtensionTest : FunSpec({

    lateinit var project: Project
    lateinit var projectProperty: ProjectProperty
    lateinit var projectInfo: ProjectInfo

    fun commonPom(version: String) =
        """
            group: mygroup
            artifactId: myplugin
            version: $version
            description: Test artifact
            packaging: jar
        """.trimIndent()

    fun commonGradlePluginPom(version: String) =
        """
            group: mygroup
            artifactId: myplugin
            version: $version
            description: Test artifact
            packaging: jar
            plugin:
                id: mygroup.myplugin
                displayName: Testing Plugin
                implementationClass: plugin.class
        """.trimIndent()

    beforeTest {
        val projectDir = tempDirectory()
        project = mockk(relaxed = true)
        every { project.rootDir } returns projectDir
        every { project.projectDir } returns File(projectDir, "module")
        every { project.logger } returns mockk<Logger>().apply {
            every { debug(any()) } returns Unit
            every { warn(any()) } returns Unit
            every { info(any()) } returns Unit
        }

        every { project.group } returns "io.hkhc"
        every { project.name } returns "test.artifact"
        every { project.version } returns "0.1"
        every { project.description } returns "This is description"

        every { project.rootDir } returns projectDir
        every { project.projectDir } returns File(projectDir, "module")
        every { project.property(any()) } returns ""
        // we need to rerun test for child project
        every { project.getRootProject() } returns project

        JarbirdLogger.logger = project.logger

        projectProperty = MockProjectProperty(
            mapOf(
                "repository.artifactory.mock.release" to "https://release",
                "repository.artifactory.mock.snapshot" to "https://snapshot",
                "repository.artifactory.mock.repoKey" to "oss-snapshot-local",
                "repository.artifactory.mock.username" to "username",
                "repository.artifactory.mock.password" to "password"
            )
        )
//        projectInfo = MockProjectInfo(
//            rootDir = projectDir,
//            projectDir = File(projectDir, "module")
//        )
        projectInfo = DefaultProjectInfo(project)
        File(project.projectDir.also { it.mkdirs() }, "pom.yml").writeText(commonPom("0.1"))
//        pomGroup = PomGroupFactory.resolvePomGroup(projectDir, File(projectDir, "module"))
    }

    context("initialization with no explicit pub declaration") {

        test("Plain extension should have some default repo") {
            val ext = JarbirdExtensionImpl(project, projectProperty, PomResolverImpl(projectInfo))
            ext.pubList.shouldBeEmpty()

            ext.finalizeRepos()
            ext.getRepos().shouldContainExactly(setOf(MavenLocalRepoSpecImpl()))
        }

        test("Implicit pub will be removed if there are explicitly declared pub") {
            val ext = JarbirdExtensionImpl(project, projectProperty, PomResolverImpl(projectInfo))
            ext.pubList.shouldBeEmpty()

            ext.finalizeRepos()
            ext.getRepos().shouldContainExactly(setOf(MavenLocalRepoSpecImpl()))
        }

        test("Explicitly declared repo") {

            val ext = JarbirdExtensionImpl(project, projectProperty, PomResolverImpl(projectInfo))
            ext.pubList.shouldBeEmpty()

            ext.artifactory("mock")

            ext.pub { }

            ext.pubList[0].needsReposWithType<ArtifactoryRepoSpec>() shouldBe true
            ext.pubList.needReposWithType<ArtifactoryRepoSpec>() shouldBe true

            ext.finalizeRepos()
            ext.getRepos().shouldContainExactlyInAnyOrder(
                setOf(
                    MavenLocalRepoSpecImpl(),
                    PropertyRepoSpecBuilder(projectProperty).buildArtifactoryRepo("mock")
                )
            )
        }

        test("Publish to maven repo") {

            listOf("0.1", "0.1-SNAPSHOT").forEach { version ->

                File(project.projectDir.also { it.mkdirs() }, "pom.yml").writeText(commonPom(version))

                val ext = JarbirdExtensionImpl(project, projectProperty, PomResolverImpl(projectInfo))
                ext.pubList.shouldBeEmpty()

                ext.mavenRepo("mock")

                ext.pub { }

                ext.finalizeRepos()

                ext.getRepos().shouldContainExactlyInAnyOrder(
                    setOf(MavenLocalRepoSpecImpl(), PropertyRepoSpecBuilder(projectProperty).buildMavenRepo("mock"))
                )

                if (ext.pubList[0].pom.isSnapshot()) {
                    ext.pubList.needReposWithType<ArtifactoryRepoSpec>() shouldBe false
                    ext.pubList.needReposWithType<MavenRepoSpec>() shouldBe true
                } else {
                    ext.pubList.needReposWithType<ArtifactoryRepoSpec>() shouldBe false
                    ext.pubList.needReposWithType<MavenRepoSpec>() shouldBe true
                }
            }
        }

        test("Publish to maven repo and artifactory") {

            listOf("0.1", "0.1-SNAPSHOT").forEach { version ->

                File(project.projectDir.also { it.mkdirs() }, "pom.yml").writeText(commonPom(version))

                val ext = JarbirdExtensionImpl(project, projectProperty, PomResolverImpl(projectInfo))
                ext.pubList.shouldBeEmpty()

                ext.mavenRepo("mock")
                ext.artifactory("mock")

                ext.pub { }

                ext.finalizeRepos()

                ext.getRepos().shouldContainExactlyInAnyOrder(
                    setOf(
                        MavenLocalRepoSpecImpl(),
                        PropertyRepoSpecBuilder(projectProperty).buildArtifactoryRepo("mock"),
                        PropertyRepoSpecBuilder(projectProperty).buildMavenRepo("mock")
                    )
                )

                if (ext.pubList[0].pom.isSnapshot()) {
                    ext.pubList.needReposWithType<ArtifactoryRepoSpec>() shouldBe true
                    ext.pubList.needReposWithType<MavenRepoSpec>() shouldBe true
                } else {
                    ext.pubList.needReposWithType<ArtifactoryRepoSpec>() shouldBe true
                    ext.pubList.needReposWithType<MavenRepoSpec>() shouldBe true
                }
            }
        }
        test("Publish to artifactory") {

            listOf("0.1", "0.1-SNAPSHOT").forEach { version ->

                File(project.projectDir.also { it.mkdirs() }, "pom.yml").writeText(commonPom(version))

                val ext = JarbirdExtensionImpl(project, projectProperty, PomResolverImpl(projectInfo))
                ext.pubList.shouldBeEmpty()

                ext.artifactory("mock")

                ext.pub { }

                ext.finalizeRepos()

                ext.getRepos().shouldContainExactlyInAnyOrder(
                    setOf(
                        MavenLocalRepoSpecImpl(),
                        PropertyRepoSpecBuilder(projectProperty).buildArtifactoryRepo("mock")
                    )
                )

                if (ext.pubList[0].pom.isSnapshot()) {
                    ext.pubList.needReposWithType<ArtifactoryRepoSpec>() shouldBe true
                    ext.pubList.needReposWithType<MavenRepoSpec>() shouldBe false
                } else {
                    ext.pubList.needReposWithType<ArtifactoryRepoSpec>() shouldBe true
                    ext.pubList.needReposWithType<MavenRepoSpec>() shouldBe false
                }
            }
        }

        test("Publish to maven repo and artifactory 2") {

            listOf("0.1", "0.1-SNAPSHOT").forEach { version ->

                File(project.projectDir.also { it.mkdirs() }, "pom.yml").writeText(commonPom(version))

                val ext = JarbirdExtensionImpl(project, projectProperty, PomResolverImpl(projectInfo))
                ext.pubList.shouldBeEmpty()

                ext.mavenRepo("mock")
                ext.artifactory("mock")

                ext.pub { }

                ext.finalizeRepos()

                ext.getRepos().shouldContainExactlyInAnyOrder(
                    setOf(
                        MavenLocalRepoSpecImpl(),
                        PropertyRepoSpecBuilder(projectProperty).buildArtifactoryRepo("mock"),
                        PropertyRepoSpecBuilder(projectProperty).buildMavenRepo("mock")
                    )
                )

                if (ext.pubList[0].pom.isSnapshot()) {
                    ext.pubList.needReposWithType<ArtifactoryRepoSpec>() shouldBe true
                    ext.pubList.needReposWithType<MavenRepoSpec>() shouldBe true
                } else {
                    ext.pubList.needReposWithType<ArtifactoryRepoSpec>() shouldBe true
                    ext.pubList.needReposWithType<MavenRepoSpec>() shouldBe true
                }
            }
        }
        test("Publish gradle plugin to bintray is supported with release version") {

            listOf("0.1", "0.1-SNAPSHOT").forEach { version ->

                File(project.projectDir.also { it.mkdirs() }, "pom.yml").writeText(commonGradlePluginPom(version))

                val ext = JarbirdExtensionImpl(project, projectProperty, PomResolverImpl(projectInfo))
                ext.pubList.shouldBeEmpty()

                ext.artifactory("mock")

                ext.pub { }

                ext.finalizeRepos()

                ext.getRepos().shouldContainExactlyInAnyOrder(
                    setOf(
                        MavenLocalRepoSpecImpl(),
                        PropertyRepoSpecBuilder(projectProperty).buildArtifactoryRepo("mock")
                    )
                )

                if (ext.pubList[0].pom.isSnapshot()) {
                    ext.pubList.needReposWithType<ArtifactoryRepoSpec>() shouldBe true
                    ext.pubList.needReposWithType<MavenRepoSpec>() shouldBe false
                } else {
                    ext.pubList.needReposWithType<ArtifactoryRepoSpec>() shouldBe true
                    ext.pubList.needReposWithType<MavenRepoSpec>() shouldBe false
                }
            }
        }
    }

    context("validation of JarbirdPubs") {

        test("single artifactory is allowed only") {

            File(project.projectDir.also { it.mkdirs() }, "pom.yml").writeText(commonGradlePluginPom("0.1"))

            val ext = JarbirdExtensionImpl(project, projectProperty, PomResolverImpl(projectInfo))
            ext.getRepos().shouldBeEmpty()

            ext.artifactory("mock")
            ext.getRepos().shouldHaveSize(1)
            ext.artifactory("mock")
            ext.getRepos().shouldHaveSize(1)
        }
    }

    context("Jarbird extension and JarbirdPub") {

        test("one pub in ext") {

            val ext = JarbirdExtensionImpl(project, projectProperty, PomResolverImpl(projectInfo))

            val pub = ext.pub {}

            ext.artifactory("mock")

            // the pub inherit the repository from extension
            pub.getRepos().map { it.id } shouldBe listOf("ArtifactoryMock")
        }
    }
})
