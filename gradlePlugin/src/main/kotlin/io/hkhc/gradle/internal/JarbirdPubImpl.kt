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

import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.RepoSpec
import io.hkhc.gradle.SourceDirs
import io.hkhc.gradle.SourceSetNames
import io.hkhc.gradle.VariantMode
import io.hkhc.gradle.internal.repo.BintrayRepoSpec
import io.hkhc.gradle.internal.repo.GradlePortalSpec
import io.hkhc.gradle.internal.repo.MavenCentralRepoSpec
import io.hkhc.gradle.internal.repo.MavenLocalRepoSpec
import io.hkhc.gradle.internal.repo.PropertyRepoSpecBuilder
import io.hkhc.gradle.internal.utils.detailMessageWarning
import io.hkhc.gradle.pom.internal.appendBeforeSnapshot
import io.hkhc.gradle.pom.internal.isSnapshot
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.tasks.SourceSet

open class JarbirdPubImpl(
    val project: Project,
    val ext: JarbirdExtensionImpl,
    projectProperty: ProjectProperty
) : JarbirdPub() {

    private var variantMode: VariantMode = VariantMode.Invisible
    private val repos = mutableSetOf<RepoSpec>()
    private val repoSpecBuilder = PropertyRepoSpecBuilder(projectProperty)
    var component: SoftwareComponent? = null
    var sourceSet: SourceSet? = null

    private var variantImpl: String = ""

    override var variant: String
        get() {
            return variantImpl
        }
        set(value) {
            variantImpl = value
        }

    /**
     * Specify maven repository for publishing.
     */
//    var mavenRepo: RepoEndpoint =
//        MavenCentralEndpoint(project)

    override fun variantWithVersion() {
        variantMode = VariantMode.WithVersion
    }
    override fun variantWithArtifactId() {
        variantMode = VariantMode.WithArtifactId
    }
    override fun variantInvisible() {
        variantMode = VariantMode.Invisible
    }

    /**
     * if set, the deployed version will be suffixed with the variant name, delimited by '-'.
     * if version is a SNAPSHOT, the variant is added before SNAPSHOT.
     * if variant is empty, the version is not altered
     *
     * e.g.
     * version = "1.0", variant = "" -> "1.0"
     * version = "1.0", variant = "debug" -> "1.0-debug"
     * version = "1.0-SNAPSHOT", variant = "debug" -> "1.0-debug-SNAPSHOT"
     */
    override fun variantArtifactId(): String? {
        return pom.artifactId?.let { id ->
            when {
                variantMode != VariantMode.WithArtifactId -> {
                    id
                }
                variant == "" -> {
                    id
                }
                else -> {
                    "$id-$variant"
                }
            }
        }
    }

    override fun variantVersion(): String? {
        return pom.version?.let { ver ->
            when {
                variantMode != VariantMode.WithVersion -> ver
                variant == "" -> ver
                ver.isSnapshot() -> ver.appendBeforeSnapshot(variant)
                else -> "$ver-$variant"
            }
        }
    }

    override fun getGAV(): String {
        return "${pom.group}:${variantArtifactId()}:${variantVersion()}"
    }

    override fun pluginCoordinate(): String {
        return if (pom.isGradlePlugin()) "${pom.plugin?.id}" else "NOT-A-PLUGIN"
    }

    override fun from(source: Any) {
        when (source) {
            is SoftwareComponent -> this.component = source
            is SourceSet -> {
                this.sourceSet = source
                this.docSourceSets = source
            }
        }
    }

    override fun sourceSetNames(vararg names: String): Any = SourceSetNames(project, names)
    override fun sourceSetNames(names: List<String>): Any = SourceSetNames(project, names.toTypedArray())
    override fun sourceDirs(dirs: Any): Any = SourceDirs(dirs)

    override fun mavenCentral(): RepoSpec {
        return repos.find { it is MavenCentralRepoSpec } ?: repoSpecBuilder.buildMavenCentral().also {
            repos.add(it)
        }
    }

    override fun mavenRepo(key: String): RepoSpec {
        val repo = repoSpecBuilder.buildMavenRepo(key)
        repos.add(repo)
        return repo
    }

    override fun mavenLocal(): RepoSpec {
        return repos.find { it is MavenLocalRepoSpec } ?: repoSpecBuilder.buildMavenLocalRepo().also {
            repos.add(it)
        }
    }

    override fun gradlePortal(): RepoSpec {
        return repos.find { it is GradlePortalSpec } ?: repoSpecBuilder.buildGradlePluginRepo().also {
            repos.add(it)
        }
    }

    override fun bintray(): RepoSpec {

        if (repos.any { it is BintrayRepoSpec }) {
            throw GradleException("Bintray repository has been declared at 'pub' level.")
        }

        ext.getRepos().let { repos ->
            if (repos.any { it is BintrayRepoSpec }) {
                throw GradleException("Bintray repository has been declared at global level.")
            }
        }

        val repo = repoSpecBuilder.buildBintrayRepo()
        if (!repos.contains(repo)) {
            detailMessageWarning(
                JarbirdLogger.logger,
                "Bintray repo will be treated as child project level declaration.",
                "Because bintray plugin publish components at child project level."
            )
            repos.add(repo)
            ext.bintray()
        }
        return repo
    }

    override fun artifactory(): RepoSpec {
        val repo = repoSpecBuilder.buildArtifactoryRepo()
        repos.add(repo)
        return repo
    }

    override fun getRepos(): Set<RepoSpec> {
        return (
            repos +
                ext.getRepos() +
                (ext.getParentExt()?.getRepos() ?: setOf())
            )
    }

    fun finalizeRepos() {

        if ((pom.group ?: "").isEmpty())
            throw GradleException("Group is missed in POM for pub($variant). May be the variant name or POM file is not correct.")
        if ((pom.artifactId ?: "").isEmpty())
            throw GradleException("ArtifactID is missed in POM for pub($variant). May be the variant name or POM file is not correct.")
        if ((pom.version ?: "").isEmpty())
            throw GradleException("Version is missed in POM for pub($variant). May be the variant name or POM file is not correct.")

        if (pom.isSnapshot() && needsBintray()) {
            val bintraySpec = getRepos().firstOrNull { it is BintrayRepoSpec }
            bintraySpec?.let { repos.add(repoSpecBuilder.buildBintraySnapshotRepoSpec(it as BintrayRepoSpec)) }
        }
        if (pom.isGradlePlugin()) {
            gradlePortal()
        }
    }

    fun needsBintray(): Boolean {
        return getRepos().find { it is BintrayRepoSpec } != null
    }
}
