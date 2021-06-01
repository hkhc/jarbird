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

import groovy.lang.Closure
import io.hkhc.gradle.JarbirdExtension
import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.RepoSpec
import io.hkhc.gradle.internal.repo.ArtifactoryRepoSpec
import io.hkhc.gradle.internal.repo.GradlePortalSpec
import io.hkhc.gradle.internal.repo.MavenCentralRepoSpec
import io.hkhc.gradle.internal.repo.MavenLocalRepoSpec
import io.hkhc.gradle.internal.repo.MavenRepoSpecImpl
import io.hkhc.gradle.internal.repo.PropertyRepoSpecBuilder
import io.hkhc.gradle.internal.utils.normalizePubName
import org.gradle.api.GradleException
import org.gradle.api.Project

open class JarbirdExtensionImpl(
    private val project: Project,
    protected val projectProperty: ProjectProperty,
    private val pomResolver: PomResolver
) : JarbirdExtension {

    val pubList = mutableListOf<JarbirdPub>()
    private val repos = mutableSetOf<RepoSpec>()
    private var isDefaultRepos = true
    private var repoSpecBuilder = PropertyRepoSpecBuilder(projectProperty)

    private var implicited: JarbirdPub? = null

    private fun initPub(pub: JarbirdPubImpl) {

        pub.pom = pomResolver.resolve(pub.variant)

        // TODO handle two publications of same artifactaId in the same module.
        // check across the whole pubList, and generate alternate pubName if there is colliding of artifactId
        pub.pubName = normalizePubName(pub.pom.artifactId ?: "Lib")

        // pre-check of final data, for child project
        // TODO handle multiple level child project?
//        if (!project.isMultiProjectRoot()) {
//            precheck(pub.pom, project)
//        }
    }

    open fun newPub(project: Project, variant: String = ""): JarbirdPubImpl {
        return JarbirdPubImpl(project, this, projectProperty, variant).apply {
            pubList.add(this)
        }
    }

    private fun JarbirdPub.configure(action: Closure<JarbirdPub>) {
        action.delegate = this
        action.resolveStrategy = Closure.DELEGATE_FIRST
        action.call()
    }

    private fun JarbirdPub.configure(action: JarbirdPub.() -> Unit) {
        action.invoke(this)
    }

    /*
    we call initPub in pub method after callback is invoked if variant is not available.
    we call initPub in pub method before callback is invoked if variant is available.
     */

    /* to be invoked by Groovy Gradle script */
    override fun pub(action: Closure<JarbirdPub>) {
        val newPub = newPub(project)
        newPub.configure(action)
        initPub(newPub)
        removeImplicit()
    }

    override fun pub(variant: String, action: Closure<JarbirdPub>) {
        if (pubList.any { it.variant == variant }) {
            throw GradleException("Duplicated pubs with variant '$variant' are found.")
        }
        val newPub = newPub(project, variant)
        initPub(newPub)
        newPub.configure(action)
        removeImplicit()
    }

    /* to be invoked by Kotlin Gradle script */
    override fun pub(action: JarbirdPub.() -> Unit) {
        val newPub = newPub(project)
        newPub.configure(action)
        initPub(newPub)
        removeImplicit()
    }

    override fun pub(variant: String, action: JarbirdPub.() -> Unit) {
        if (pubList.any { it.variant == variant }) {
            throw GradleException("Duplicated pubs with variant '$variant' are found.")
        }
        val newPub = newPub(project, variant)
        initPub(newPub)
        newPub.configure(action)
        removeImplicit()
    }

    fun createImplicit() {
        /*
        if implicit != null, we have already got an implicit, no need to create another
        if pubList.isNotEmpty() and implicit == null, we have an non-implicit pub, no need to create implicit
         */
        if (implicited != null || pubList.isNotEmpty()) return
        pub {}
        implicited = pubList[0]
    }

    fun removeImplicit() {
        /*
        If implicit == null , means we have not created an implicit pub, no need to remove
        If pubList.size == 1 and implicit != null, this means it is the only pub, so we still need it, don't remove
         */
        if (implicited == null || pubList.size == 1) return
        pubList.remove(implicited)
        implicited = null
    }

    /**
     * If user declare a repo explicitly, we don't need the default repo any more.
     */
    private fun disableDefaultRepos() {
        if (isDefaultRepos) {
            repos.clear()
            isDefaultRepos = false
        }
    }

    // There is no effect to declare mavenCentral() more than once, just use the same RepoSpec
    override fun mavenCentral(): RepoSpec {
        disableDefaultRepos()
        return repos.find { it is MavenCentralRepoSpec } ?: repoSpecBuilder.buildMavenCentral(project).also {
            repos.add(it)
        }
    }

    // There is no effect to declare mavenRepo() more than once in the same pub, just use the same RepoSpec
    override fun mavenRepo(key: String): RepoSpec {
        disableDefaultRepos()
        val repo = repoSpecBuilder.buildMavenRepo(key)
        val existingRepo = repos.filterIsInstance(MavenRepoSpecImpl::class.java).find { it.id == repo.id }
        return if (existingRepo == null) {
            repos.add(repo)
            repo
        } else {
            existingRepo
        }
    }

    override fun mavenLocal(): RepoSpec {
        disableDefaultRepos()
        return repos.find { it is MavenLocalRepoSpec } ?: repoSpecBuilder.buildMavenLocalRepo().also {
            repos.add(it)
        }
    }

    override fun gradlePortal(): RepoSpec {
        disableDefaultRepos()
        return repos.find { it is GradlePortalSpec } ?: repoSpecBuilder.buildGradlePluginRepo().also {
            repos.add(it)
        }
    }

    override fun artifactory(): RepoSpec {

        if (repos.filterIsInstance<ArtifactoryRepoSpec>().isNotEmpty()) {
            throw GradleException("There can only be one configuration per sub-project for Artifactory server only.")
        }

        disableDefaultRepos()
        val repo = repoSpecBuilder.buildArtifactoryRepo()
        repos.add(repo)
        return repo
    }

    /**
     * Get the Jarbird extension of the parent project.
     * @return null if current project is root project or no Jarbird extension is found in parent project
     */
    fun getParentExt(): JarbirdExtension? {
        return if (project.isRoot()) {
            null
        } else {
            project.parent?.extensions?.findByName(SP_EXT_NAME)?.let {
                return@getParentExt it as JarbirdExtension
            }
        }
    }

    override fun getRepos(): Set<RepoSpec> {
        return repos
    }

    fun finalizeRepos() {
        mavenLocal()
        pubList.forEach { (it as JarbirdPubImpl).finalizeRepos() }
    }
}
