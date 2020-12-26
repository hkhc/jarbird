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

import io.hkhc.gradle.internal.repo.BintraySpec
import io.hkhc.gradle.internal.repo.MavenLocalSpec
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

    beforeTest {
        val projectDir = tempDirectory()
        project = mockk()
        every { project.rootDir } returns projectDir
        every { project.projectDir } returns File(projectDir, "module")
        every { project.logger } returns mockk<Logger>().apply {
            every { debug(any()) } returns Unit
        }

        every { project.group } returns "io.hkhc"
        every { project.name } returns "test.artifact"
        every { project.version } returns "0.1"
        every { project.description } returns "This is description"

        every { project.property("repository.bintray.username") } returns "username"
        every { project.property("repository.bintray.password") } returns "password"
        every { project.property(any()) } returns ""
    }

    context("initialization with no explicit pub declaration") {

        test("Plain extension should have some default repo") {
            val ext = JarbirdExtensionImpl(project)
            ext.pubList.shouldBeEmpty()

            ext.createImplicit()
            ext.pubList.shouldHaveSize(1)

            ext.finalizeRepos()
            ext.repos.shouldContainExactly(setOf(MavenLocalSpec()))

            ext.removeImplicit()
            ext.repos.shouldHaveSize(1)
        }

        test("Implicit pub will be removed if there are explicitly declared pub") {
            val ext = JarbirdExtensionImpl(project)
            ext.pubList.shouldBeEmpty()

            ext.createImplicit()
            ext.pubList.shouldHaveSize(1)

            ext.pub { }

            ext.finalizeRepos()
            ext.repos.shouldContainExactly(setOf(MavenLocalSpec()))

            ext.removeImplicit()
            ext.repos.shouldHaveSize(1)
        }

        test("Explicitly declared repo") {

            val ext = JarbirdExtensionImpl(project)
            ext.pubList.shouldBeEmpty()

            ext.createImplicit()
            ext.pubList.shouldHaveSize(1)

            ext.bintray()

            ext.pub { }

            ext.finalizeRepos()
            ext.repos.shouldContainExactlyInAnyOrder(setOf(MavenLocalSpec(), BintraySpec(DefaultProjectProperty(project))))

            ext.pubList[0].needsBintray() shouldBe true
            ext.pubList.needsBintray() shouldBe true
        }

        test("Publish gradle plugin to bintray is supported with release version") {

            listOf("0.1", "0.1-SNAPSHOT").forEach { version ->

                File(project.projectDir.also { it.mkdirs() }, "pom.yml").writeText(
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
                )

                val ext = JarbirdExtensionImpl(project)
                ext.pubList.shouldBeEmpty()

                ext.createImplicit()
                ext.pubList.shouldHaveSize(1)

                ext.bintray()

                ext.pub { }

                ext.finalizeRepos()
                ext.repos.shouldContainExactlyInAnyOrder(
                    setOf(MavenLocalSpec(), BintraySpec(DefaultProjectProperty(project)))
                )

                if (ext.pubList[0].pom.isSnapshot()) {
                    ext.pubList[0].needsBintray() shouldBe false
                    ext.pubList.needsBintray() shouldBe false
                }
            }
        }
    }
})
