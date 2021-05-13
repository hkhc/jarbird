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

package io.hkhc.gradle.internal.repo

import io.hkhc.gradle.internal.JarbirdLogger
import io.hkhc.utils.test.MockProjectProperty
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.Project
import org.gradle.api.logging.Logging

class RepoTest : FunSpec({

    val project = mockk<Project>()

    beforeTest {

        every { project.logger } returns Logging.getLogger(Project::class.java)
        JarbirdLogger.logger = mockk(relaxed = true)

    }

    test("MavenLocalSpec equality") {
        MavenLocalRepoSpecImpl() shouldBe MavenLocalRepoSpecImpl()
    }

    test("MavenCentralSpec equality") {
        val projectProperty = MockProjectProperty(
            mapOf(
                "repository.mavencentral.username" to "username",
                "repository.mavencentral.password" to "password"
            )
        )
        PropertyRepoSpecBuilder(projectProperty).apply {
            buildMavenCentral(project) shouldBe buildMavenCentral(project)
        }
    }

    test("GradlePortalSpec equality") {
        GradlePortalSpecImpl() shouldBe GradlePortalSpecImpl()
    }

    test("BintraySpec equality") {
        val projectProperty = MockProjectProperty(
            mapOf(
                "repository.bintray.username" to "username",
                "repository.bintray.password" to "password"
            )
        )
        PropertyRepoSpecBuilder(projectProperty).apply {
            buildBintrayRepo() shouldBe buildBintrayRepo()
        }
    }

    test("MavenRepoSpec equality") {
        val projectProperty = MockProjectProperty(
            mapOf(
                "repository.maven.mock.release" to "releaseurl",
                "repository.maven.mock.snapshot" to "snapshoturl",
                "repository.maven.mock.username" to "username",
                "repository.maven.mock.password" to "password",
                "repository.maven.mock.apikey" to "",
                "repository.maven.mock.description" to "description",
                "repository.maven.mock.allowInsecureProtocol" to "true"
            )
        )

        PropertyRepoSpecBuilder(projectProperty).apply {
            buildMavenRepo("mock") shouldBe buildMavenRepo("mock")
        }
    }
})
