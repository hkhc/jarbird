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

import io.hkhc.gradle.internal.DefaultProjectInfo
import io.hkhc.gradle.internal.JarbirdExtensionImpl
import io.hkhc.gradle.internal.JarbirdLogger
import io.hkhc.gradle.internal.LOG_PREFIX
import io.hkhc.gradle.internal.PomResolver
import io.hkhc.gradle.internal.PomResolverImpl
import io.hkhc.gradle.pom.PluginInfo
import io.hkhc.gradle.pom.Pom
import io.hkhc.utils.test.MockProjectProperty
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.File

class ArtifactoryConfigModelTest : FunSpec({

    val project = mockk<Project>(relaxed = true).also {
        every { it.rootDir } returns File("project")
        every { it.projectDir } returns File("project/app")
        every { it.parent } returns null
    }
    val projectInfo = DefaultProjectInfo(project)

    beforeSpec {
        JarbirdLogger.logger = project.logger
    }

    test("empty repos") {

        // WHEN
        shouldThrow<GradleException> {
            ArtifactoryConfigModel(listOf())
        }.message shouldBe "$LOG_PREFIX Artifactory repo spec is not found unexpectedly. Probably a bug of Jarbird"

    }

    test("one repo with no artifactory repos") {

        // GIVEN
        val projectProperty = MockProjectProperty(mapOf())
        val pomResolver = PomResolverImpl(projectInfo)
        val ext = JarbirdExtensionImpl(project, projectProperty, PomResolverImpl(projectInfo))

        // WHEN
        val pub = ext.pub {}

        shouldThrow<GradleException> {
            ArtifactoryConfigModel(listOf(pub))
        }.message shouldBe "$LOG_PREFIX Artifactory repo spec is not found unexpectedly. Probably a bug of Jarbird"

    }

    test("one artifactory repos") {
        // GIVEN
        val projectProperty = MockProjectProperty(
            mapOf(
                "repository.artifactory.mock.release" to "https://release",
                "repository.artifactory.mock.snapshot" to "https://snapshot",
                "repository.artifactory.mock.repoKey" to "oss-snapshot-local",
                "repository.artifactory.mock.username" to "username",
                "repository.artifactory.mock.password" to "password"
            )
        )

        val pomResolver = mockk<PomResolver>()
        every { pomResolver.resolve(any()) } returns Pom(
            group = "mygroup",
            artifactId = "mylib",
            version = "1.0"
        )

        val ext = JarbirdExtensionImpl(project, projectProperty, pomResolver)

        // WHEN
        val pub = ext.pub {
            artifactory("mock")
        }
        val model = ArtifactoryConfigModel(listOf(pub))

        // THEN
        model.needsArtifactory() shouldBe true
        model.contextUrl shouldBe projectProperty.property("repository.artifactory.mock.release")
        model.publications.shouldHaveSize(1)
        model.publications[0] shouldBe "mylib"
        model.repoSpec shouldNotBe null
        with(model.repoSpec) {
            repoKey shouldBe projectProperty.property("repository.artifactory.mock.repoKey")
            username shouldBe projectProperty.property("repository.artifactory.mock.username")
            password shouldBe projectProperty.property("repository.artifactory.mock.password")
        }
    }

    test("one other repo and one artifactory repos") {

        // GIVEN
        val projectProperty = MockProjectProperty(
            mapOf(
                "repository.artifactory.mock.release" to "https://release",
                "repository.artifactory.mock.snapshot" to "https://snapshot",
                "repository.artifactory.mock.repoKey" to "oss-snapshot-local",
                "repository.artifactory.mock.username" to "username",
                "repository.artifactory.mock.password" to "password"
            )
        )

        val pomResolver = mockk<PomResolver>()
        every { pomResolver.resolve(any()) } returns Pom(
            group = "mygroup",
            artifactId = "mylib",
            version = "1.0"
        )

        val ext = JarbirdExtensionImpl(project, projectProperty, pomResolver)

        // WHEN
        val pub = ext.pub {
            mavenLocal()
            artifactory("mock")
        }
        val model = ArtifactoryConfigModel(listOf(pub))

        // THEN
        model.needsArtifactory() shouldBe true
        model.contextUrl shouldBe projectProperty.property("repository.artifactory.mock.release")
        model.publications.shouldHaveSize(1)
        model.publications[0] shouldBe "mylib"
        model.repoSpec shouldNotBe null
        with(model.repoSpec) {
            repoKey shouldBe projectProperty.property("repository.artifactory.mock.repoKey")
            username shouldBe projectProperty.property("repository.artifactory.mock.username")
            password shouldBe projectProperty.property("repository.artifactory.mock.password")
        }
    }

    test("one artifactory repo declare twice") {

        // GIVEN
        val projectProperty = MockProjectProperty(
            mapOf(
                "repository.artifactory.mock.release" to "https://release",
                "repository.artifactory.mock.snapshot" to "https://snapshot",
                "repository.artifactory.mock.repoKey" to "oss-snapshot-local",
                "repository.artifactory.mock.username" to "username",
                "repository.artifactory.mock.password" to "password"
            )
        )

        val pomResolver = mockk<PomResolver>()
        every { pomResolver.resolve(any()) } returns Pom(
            group = "mygroup",
            artifactId = "mylib",
            version = "1.0"
        )

        val ext = JarbirdExtensionImpl(project, projectProperty, pomResolver)

        // WHEN
        val pub = ext.pub {
            artifactory("mock")
            artifactory("mock")
        }
        val model = ArtifactoryConfigModel(listOf(pub))

        // THEN
        model.needsArtifactory() shouldBe true
        model.contextUrl shouldBe projectProperty.property("repository.artifactory.mock.release")
        model.publications.shouldHaveSize(1)
        model.publications[0] shouldBe "mylib"
        model.repoSpec shouldNotBe null
        with(model.repoSpec) {
            repoKey shouldBe projectProperty.property("repository.artifactory.mock.repoKey")
            username shouldBe projectProperty.property("repository.artifactory.mock.username")
            password shouldBe projectProperty.property("repository.artifactory.mock.password")
        }
    }

    test("two artifactory repos for one publication") {

        // GIVEN
        val projectProperty = MockProjectProperty(
            mapOf(
                "repository.artifactory.mock1.release" to "https://release1",
                "repository.artifactory.mock1.snapshot" to "https://snapshot1",
                "repository.artifactory.mock1.repoKey" to "oss-snapshot-local1",
                "repository.artifactory.mock1.username" to "username1",
                "repository.artifactory.mock1.password" to "password1",
                "repository.artifactory.mock2.release" to "https://release2",
                "repository.artifactory.mock2.snapshot" to "https://snapshot2",
                "repository.artifactory.mock2.repoKey" to "oss-snapshot-local2",
                "repository.artifactory.mock2.username" to "username2",
                "repository.artifactory.mock2.password" to "password2"
            )
        )

        val pomResolver = mockk<PomResolver>()
        every { pomResolver.resolve(any()) } returns Pom(
            group = "mygroup",
            artifactId = "mylib",
            version = "1.0"
        )

        val ext = JarbirdExtensionImpl(project, projectProperty, PomResolverImpl(projectInfo))

        // WHEN
        val pub = ext.pub {
            artifactory("mock1")
            artifactory("mock2")
        }

        shouldThrow<GradleException> {
            ArtifactoryConfigModel(listOf(pub))
        }
    }

    test("one artifactory repos with all release GAV") {

        // GIVEN
        val projectProperty = MockProjectProperty(
            mapOf(
                "repository.artifactory.mock.release" to "https://release",
                "repository.artifactory.mock.snapshot" to "https://snapshot",
                "repository.artifactory.mock.repoKey" to "oss-snapshot-local",
                "repository.artifactory.mock.username" to "username",
                "repository.artifactory.mock.password" to "password"
            )
        )

        val pomResolver = mockk<PomResolver>()
        every { pomResolver.resolve("variant1") } returns Pom(
            group = "mygroup1",
            artifactId = "mylib1",
            version = "1.0"
        )
        every { pomResolver.resolve("variant2") } returns Pom(
            group = "mygroup2",
            artifactId = "mylib2",
            version = "1.0"
        )

        val ext = JarbirdExtensionImpl(project, projectProperty, pomResolver)

        // WHEN
        val pub1 = ext.pub("variant1") {
            artifactory("mock")
        }
        val pub2 = ext.pub("variant2") {
            artifactory("mock")
        }

        val model = ArtifactoryConfigModel(listOf(pub1, pub2))

        // THEN
        model.needsArtifactory() shouldBe true
        model.contextUrl shouldBe projectProperty.property("repository.artifactory.mock.release")
        model.publications shouldBe listOf("mylib1Variant1", "mylib2Variant2")
        model.repoSpec shouldNotBe null
        with(model.repoSpec) {
            repoKey shouldBe projectProperty.property("repository.artifactory.mock.repoKey")
            username shouldBe projectProperty.property("repository.artifactory.mock.username")
            password shouldBe projectProperty.property("repository.artifactory.mock.password")
        }
    }

    test("one artifactory repos with mixed release and snapshot GAV") {
        // GIVEN
        val projectProperty = MockProjectProperty(
            mapOf(
                "repository.artifactory.mock.release" to "https://release",
                "repository.artifactory.mock.snapshot" to "https://snapshot",
                "repository.artifactory.mock.repoKey" to "oss-snapshot-local",
                "repository.artifactory.mock.username" to "username",
                "repository.artifactory.mock.password" to "password"
            )
        )

        val pomResolver = mockk<PomResolver>()
        every { pomResolver.resolve("variant1") } returns Pom(
            group = "mygroup1",
            artifactId = "mylib1",
            version = "1.0"
        )
        every { pomResolver.resolve("variant2") } returns Pom(
            group = "mygroup2",
            artifactId = "mylib2",
            version = "1.0-SNAPSHOT"
        )

        val ext = JarbirdExtensionImpl(project, projectProperty, pomResolver)

        // WHEN
        val pub1 = ext.pub("variant1") {
            artifactory("mock")
        }
        val pub2 = ext.pub("variant2") {
            artifactory("mock")
        }

        shouldThrow<GradleException> {
            ArtifactoryConfigModel(listOf(pub1, pub2))
        }
    }

    test("one artifactory repo for gradle plugin publishing") {

        // GIVEN
        val projectProperty = MockProjectProperty(
            mapOf(
                "repository.artifactory.mock.release" to "https://release",
                "repository.artifactory.mock.snapshot" to "https://snapshot",
                "repository.artifactory.mock.repoKey" to "oss-snapshot-local",
                "repository.artifactory.mock.username" to "username",
                "repository.artifactory.mock.password" to "password"
            )
        )

        val pomResolver = mockk<PomResolver>()
        every { pomResolver.resolve(any()) } returns Pom(
            group = "mygroup",
            artifactId = "mylib",
            version = "1.0",
            plugin = PluginInfo(
                id = "myPlugin",
                displayName = "my plugin",
                implementationClass = "myClass"
            )
        )

        val ext = JarbirdExtensionImpl(project, projectProperty, pomResolver)

        // WHEN
        val pub = ext.pub {
            mavenLocal()
            artifactory("mock")
        }
        val model = ArtifactoryConfigModel(listOf(pub))

        // THEN
        model.needsArtifactory() shouldBe true
        model.contextUrl shouldBe projectProperty.property("repository.artifactory.mock.release")
        model.publications shouldBe listOf("mylib", "mylibPluginMarkerMaven")
        model.repoSpec shouldNotBe null
        with(model.repoSpec) {
            repoKey shouldBe projectProperty.property("repository.artifactory.mock.repoKey")
            username shouldBe projectProperty.property("repository.artifactory.mock.username")
            password shouldBe projectProperty.property("repository.artifactory.mock.password")
        }
    }
})
