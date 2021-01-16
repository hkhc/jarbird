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

import groovy.lang.Closure
import io.hkhc.gradle.JarbirdExtension
import io.hkhc.gradle.JarbirdPlugin
import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.RepoSpec
import io.hkhc.gradle.internal.repo.BintrayRepoSpec
import io.hkhc.gradle.internal.repo.GradlePortalSpec
import io.hkhc.gradle.internal.repo.MavenCentralRepoSpec
import io.hkhc.gradle.internal.repo.MavenLocalRepoSpec
import io.hkhc.gradle.internal.repo.PropertyRepoSpecBuilder
import io.hkhc.gradle.pom.PomGroup
import org.gradle.api.GradleException
import org.gradle.api.Project

open class JarbirdExtensionImpl(
    private val project: Project,
    private val projectProperty: ProjectProperty,
    private val projectInfo: ProjectInfo,
    private val pomGroup: PomGroup
) : JarbirdExtension {

    val pubList = mutableListOf<JarbirdPub>()
    internal val repos = mutableSetOf<RepoSpec>()
    private var isDefaultRepos = true
    private var repoSpecBuilder = PropertyRepoSpecBuilder(projectProperty)

    private var implicited: JarbirdPub? = null

    init {
        project.logger.debug("$LOG_PREFIX Aggregated POM configuration: $pomGroup")
    }

    private fun initPub(pub: JarbirdPub) {

        pomGroup[pub.variant]?.let { pub.pom = it } ?: throw GradleException("Variant '${pub.variant}' is not found")

        // TODO we ignore that pom overwrite some project properties in the mean time.
        // need to properly take care of it.
        pub.pom.syncWith(projectInfo)

        // TODO handle two publications of same artifactaId in the same module.
        // check across the whole pubList, and generate alternate pubName if there is colliding of artifactId
        pub.pubName = JarbirdPlugin.normalizePubName(pub.pom.artifactId ?: "Lib")

        // pre-check of final data, for child project
        // TODO handle multiple level child project?
//        if (!project.isMultiProjectRoot()) {
//            precheck(pub.pom, project)
//        }
    }

    private fun newPub(project: Project): JarbirdPubImpl {
        return JarbirdPubImpl(project, projectProperty).apply {
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
        newPub.parentRepos = this
        newPub.configure(action)
        initPub(newPub)
    }

    override fun pub(variant: String, action: Closure<JarbirdPub>) {
        val newPub = newPub(project)
        newPub.parentRepos = this
        newPub.variant = variant
        initPub(newPub)
        newPub.configure(action)
    }

    /* to be invoked by Kotlin Gradle script */
    override fun pub(action: JarbirdPub.() -> Unit) {
        val newPub = newPub(project)
        newPub.parentRepos = this
        newPub.configure(action)
        initPub(newPub)
    }

    override fun pub(variant: String, action: JarbirdPub.() -> Unit) {
        val newPub = newPub(project)
        newPub.parentRepos = this
        newPub.variant = variant
        initPub(newPub)
        newPub.configure(action)
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

    override fun mavenCentral(): RepoSpec {
        if (isDefaultRepos) {
            repos.clear()
            isDefaultRepos = false
        }
        return repos.find { it is MavenCentralRepoSpec } ?: repoSpecBuilder.buildMavenCentral().also {
            repos.add(it)
        }
    }

    override fun mavenRepo(key: String): RepoSpec {
        if (isDefaultRepos) {
            repos.clear()
            isDefaultRepos = false
        }
        val repo = repoSpecBuilder.buildMavenRepo(key)
        if (!repos.contains(repo)) repos.add(repo)
        return repo
    }

    override fun mavenLocal(): RepoSpec {
        if (isDefaultRepos) {
            repos.clear()
            isDefaultRepos = false
        }
        return repos.find { it is MavenLocalRepoSpec } ?: repoSpecBuilder.buildMavenLocalRepo().also {
            repos.add(it)
        }
    }

    override fun gradlePortal(): RepoSpec {
        if (isDefaultRepos) {
            repos.clear()
            isDefaultRepos = false
        }
        return repos.find { it is GradlePortalSpec } ?: repoSpecBuilder.buildGradlePluginRepo().also {
            repos.add(it)
        }
    }

    override fun bintray(): RepoSpec {

        if (repos.any { it is BintrayRepoSpec }) {
            throw GradleException("Bintray repository has been declared more than once at global level.")
        }

        if (isDefaultRepos) {
            repos.clear()
            isDefaultRepos = false
        }
        val repo = repoSpecBuilder.buildBintrayRepo()
        if (!repos.contains(repo)) repos.add(repo)
        return repo
    }

    override fun artifactory(): RepoSpec {
        if (isDefaultRepos) {
            repos.clear()
            isDefaultRepos = false
        }
        val repo = repoSpecBuilder.buildArtifactoryRepo()
        if (!repos.contains(repo)) repos.add(repo)
        return repo
    }

    override fun getRepos(): Set<RepoSpec> {
        return repos
    }

    fun finalizeRepos() {
        mavenLocal()
        pubList.forEach { (it as JarbirdPubImpl).finalizeRepos() }
    }
}
