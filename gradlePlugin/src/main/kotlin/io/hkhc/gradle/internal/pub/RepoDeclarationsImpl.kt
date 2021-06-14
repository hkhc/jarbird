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

import io.hkhc.gradle.RepoDeclaration
import io.hkhc.gradle.RepoSpec
import io.hkhc.gradle.internal.ProjectProperty
import io.hkhc.gradle.internal.repo.ArtifactoryRepoSpecImpl
import io.hkhc.gradle.internal.repo.GradlePortalSpec
import io.hkhc.gradle.internal.repo.MavenCentralRepoSpec
import io.hkhc.gradle.internal.repo.MavenLocalRepoSpec
import io.hkhc.gradle.internal.repo.MavenRepoSpecImpl
import io.hkhc.gradle.internal.repo.PropertyRepoSpecBuilder
import org.gradle.api.Project

open class RepoDeclarationsImpl(
    private val project: Project,
    projectProperty: ProjectProperty,
    private val parentRepoDeclaration: RepoDeclaration? = null
) : RepoDeclaration {

    protected val mRepos = mutableSetOf<RepoSpec>()
    private val repoSpecBuilder = PropertyRepoSpecBuilder(projectProperty)

    // Ensure one instance per pub
    override fun mavenCentral(): RepoSpec {
        return mRepos.find { it is MavenCentralRepoSpec } ?: repoSpecBuilder.buildMavenCentral(project).also {
            mRepos.add(it)
        }
    }

    override fun mavenRepo(key: String): RepoSpec {
        val repo = repoSpecBuilder.buildMavenRepo(key)
        val existingRepo = getRepos().filterIsInstance(MavenRepoSpecImpl::class.java).find { it.id == repo.id }
        return if (existingRepo == null) {
            mRepos.add(repo)
            repo
        } else {
            existingRepo
        }
    }

    // Ensure one instance per pub
    override fun mavenLocal(): RepoSpec {
        return mRepos.find { it is MavenLocalRepoSpec } ?: repoSpecBuilder.buildMavenLocalRepo().also {
            mRepos.add(it)
        }
    }

    // Ensure one instance per pub
    override fun gradlePortal(): RepoSpec {
        return mRepos.find { it is GradlePortalSpec } ?: repoSpecBuilder.buildGradlePluginRepo().also {
            mRepos.add(it)
        }
    }

    /*
    existingRepo    existingParentRepo      action
    null            null                    repos.add(repo)
    not null        null                    existingRepo
    null            not null
    not null        not null
     */

    override fun artifactory(key: String): RepoSpec {
        val repo = repoSpecBuilder.buildArtifactoryRepo(key)
        val existingRepo = getRepos().filterIsInstance(ArtifactoryRepoSpecImpl::class.java).find { it.id == repo.id }
        return if (existingRepo == null) {
            mRepos.add(repo)
            repo
        } else {
            existingRepo
        }
    }

    override fun getRepos(): Set<RepoSpec> {
        return mRepos + (parentRepoDeclaration?.getRepos() ?: setOf())
    }

}
