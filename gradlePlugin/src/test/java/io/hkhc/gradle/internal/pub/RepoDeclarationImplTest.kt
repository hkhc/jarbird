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

package io.hkhc.gradle.internal.pub

import io.hkhc.gradle.internal.DefaultProjectInfo
import io.hkhc.gradle.internal.JarbirdLogger
import io.hkhc.gradle.internal.ProjectInfo
import io.hkhc.gradle.internal.ProjectProperty
import io.hkhc.gradle.internal.repo.ArtifactoryRepoSpec
import io.hkhc.gradle.internal.repo.GradlePortalSpec
import io.hkhc.gradle.internal.repo.MavenCentralRepoSpec
import io.hkhc.gradle.internal.repo.MavenLocalRepoSpec
import io.hkhc.gradle.internal.repo.MavenRepoSpec
import io.hkhc.utils.test.MockProjectProperty
import io.hkhc.utils.test.tempDirectory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import java.io.File

class RepoDeclarationImplTest : FunSpec({

    lateinit var project: Project
    lateinit var projectProperty: ProjectProperty

    beforeTest {
        val projectDir = tempDirectory()
        project = mockk(relaxed = true)
        every { project.rootDir } returns projectDir
        every { project.projectDir } returns File(projectDir, "module")

        every { project.getRootDir() } returns projectDir
        every { project.getProjectDir() } returns File(projectDir, "module")
        every { project.property(any()) } returns ""
        // we need to rerun test for child project
        every { project.getRootProject() } returns project

        JarbirdLogger.logger = project.logger

        projectProperty = MockProjectProperty(
            mapOf(
                "repository.artifactory.mock.release" to "https://release",
                "repository.artifactory.mock.snapshot" to "https://snapshot",
                "repository.artifactory.mock.repoKey" to "oss-snapshot-local"
            )
        )

    }

    context("single RepoDeclaration") {

        test("default value") {
            RepoDeclarationsImpl(project, projectProperty, null).apply {
                getRepos().shouldBeEmpty()
            }
        }

        test("add mavenLocal") {
            RepoDeclarationsImpl(project, projectProperty, null).apply {
                mavenLocal()
                getRepos().apply {
                    shouldHaveSize(1)
                    toList()[0].shouldBeInstanceOf<MavenLocalRepoSpec>()
                }
            }
        }

        test("add double mavenLocal") {
            RepoDeclarationsImpl(project, projectProperty, null).apply {
                mavenLocal()
                mavenLocal()
                getRepos().apply {
                    shouldHaveSize(1)
                    toList()[0].shouldBeInstanceOf<MavenLocalRepoSpec>()
                }
            }
        }

        test("add mavenCentral") {
            RepoDeclarationsImpl(project, projectProperty, null).apply {
                mavenCentral()
                getRepos().apply {
                    shouldHaveSize(1)
                    toList()[0].shouldBeInstanceOf<MavenCentralRepoSpec>()
                }
            }
        }

        test("add double mavenCentral") {
            RepoDeclarationsImpl(project, projectProperty, null).apply {
                mavenCentral()
                mavenCentral()
                getRepos().apply {
                    shouldHaveSize(1)
                    toList()[0].shouldBeInstanceOf<MavenCentralRepoSpec>()
                }
            }
        }

        test("add mavenRepo") {
            RepoDeclarationsImpl(project, projectProperty, null).apply {
                mavenRepo("mock")
                getRepos().apply {
                    shouldHaveSize(1)
                    toList()[0].shouldBeInstanceOf<MavenRepoSpec>()
                }
            }
        }

        test("add double mavenRepo") {
            RepoDeclarationsImpl(project, projectProperty, null).apply {
                mavenRepo("mock")
                mavenRepo("mock")
                getRepos().apply {
                    shouldHaveSize(1)
                    toList()[0].shouldBeInstanceOf<MavenRepoSpec>()
                }
            }
        }

        test("add two different mavenRepo") {
            RepoDeclarationsImpl(project, projectProperty, null).apply {
                mavenRepo("mock1")
                mavenRepo("mock2")
                getRepos().apply {
                    shouldHaveSize(2)
                    toList()[0].shouldBeInstanceOf<MavenRepoSpec>()
                    toList()[1].shouldBeInstanceOf<MavenRepoSpec>()
                }
            }
        }

        test("add artifactory") {
            RepoDeclarationsImpl(project, projectProperty, null).apply {
                artifactory("mock")
                getRepos().apply {
                    shouldHaveSize(1)
                    toList()[0].shouldBeInstanceOf<ArtifactoryRepoSpec>()
                }
            }
        }

        test("add double artifactory") {
            RepoDeclarationsImpl(project, projectProperty, null).apply {
                artifactory("mock")
                artifactory("mock")
                getRepos().apply {
                    shouldHaveSize(1)
                    toList()[0].shouldBeInstanceOf<ArtifactoryRepoSpec>()
                }
            }
        }

        test("add gradlePortal") {
            RepoDeclarationsImpl(project, projectProperty, null).apply {
                gradlePortal()
                getRepos().apply {
                    shouldHaveSize(1)
                    toList()[0].shouldBeInstanceOf<GradlePortalSpec>()
                }
            }
        }

        test("add double gradlePortal") {
            RepoDeclarationsImpl(project, projectProperty, null).apply {
                gradlePortal()
                gradlePortal()
                getRepos().apply {
                    shouldHaveSize(1)
                    toList()[0].shouldBeInstanceOf<GradlePortalSpec>()
                }
            }
        }

        test("add two different repo spec") {
            RepoDeclarationsImpl(project, projectProperty, null).apply {
                mavenRepo("mock1")
                artifactory("mock")
                getRepos().apply {
                    shouldHaveSize(2)
                    count { it is MavenRepoSpec } shouldBe 1
                    count { it is ArtifactoryRepoSpec } shouldBe 1
                }
            }
        }
    }

    context("RepoDeclaration with parent") {

        test("parent repo available in child") {

            // for unit test sake, we don't need the project reference to the two RepoDeclaration to have root/child
            // relationship.
            val parentRepoDeclaration = RepoDeclarationsImpl(mockk(), projectProperty, null).apply {
                mavenRepo("mock")
            }

            RepoDeclarationsImpl(project, projectProperty, parentRepoDeclaration).apply {
                getRepos().apply {
                    shouldHaveSize(1)
                    toList()[0].shouldBeInstanceOf<MavenRepoSpec>()
                }
            }
        }

        test("parent repo available in child and overlapping is resolved") {

            // for unit test sake, we don't need the project reference to the two RepoDeclaration to have root/child
            // relationship.
            val parentRepoDeclaration = RepoDeclarationsImpl(mockk(), projectProperty, null).apply {
                mavenRepo("mock")
            }

            RepoDeclarationsImpl(project, projectProperty, parentRepoDeclaration).apply {
                mavenRepo("mock")
                getRepos().apply {
                    shouldHaveSize(1)
                    toList()[0].shouldBeInstanceOf<MavenRepoSpec>()
                }
            }
        }

        test("parent repo available in child when they are not overlapped") {

            // for unit test sake, we don't need the project reference to the two RepoDeclaration to have root/child
            // relationship.
            val parentRepoDeclaration = RepoDeclarationsImpl(mockk(), projectProperty, null).apply {
                mavenRepo("mock")
            }

            RepoDeclarationsImpl(project, projectProperty, parentRepoDeclaration).apply {
                artifactory("mock")
                getRepos().apply {
                    shouldHaveSize(2)
                    count { it is MavenRepoSpec } shouldBe 1
                    count { it is ArtifactoryRepoSpec } shouldBe 1
                }
            }
        }
    }

})
