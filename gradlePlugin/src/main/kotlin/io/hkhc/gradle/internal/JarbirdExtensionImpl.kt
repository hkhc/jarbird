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
import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.PomGroupCallback
import io.hkhc.gradle.RepoSpec
import io.hkhc.gradle.internal.repo.BintraySpec
import io.hkhc.gradle.internal.repo.GradlePortalSpec
import io.hkhc.gradle.internal.repo.MavenCentralSpec
import io.hkhc.gradle.internal.repo.MavenLocalSpec
import io.hkhc.gradle.internal.repo.MavenRepoSpec
import org.gradle.api.Project

open class JarbirdExtensionImpl(private val project: Project) : JarbirdExtension {

    val pubList = mutableListOf<JarbirdPub>()
    private val repos = mutableSetOf<RepoSpec>()
    private var isDefaultRepos = true

    lateinit var pomGroupCallback: PomGroupCallback
    private var implicited: JarbirdPub? = null

    private fun newPub(project: Project): JarbirdPubImpl {
        return JarbirdPubImpl(project).apply {
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
        pomGroupCallback.initPub(newPub)
    }

    override fun pub(variant: String, action: Closure<JarbirdPub>) {
        val newPub = newPub(project)
        newPub.parentRepos = this
        newPub.variant = variant
        pomGroupCallback.initPub(newPub)
        newPub.configure(action)
    }

    /* to be invoked by Kotlin Gradle script */
    override fun pub(action: JarbirdPub.() -> Unit) {
        val newPub = newPub(project)
        newPub.parentRepos = this
        newPub.configure(action)
        pomGroupCallback.initPub(newPub)
    }

    override fun pub(variant: String, action: JarbirdPub.() -> Unit) {
        val newPub = newPub(project)
        newPub.parentRepos = this
        newPub.variant = variant
        pomGroupCallback.initPub(newPub)
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
        return repos.find { it is MavenCentralSpec } ?: MavenCentralSpec(project).also {
            repos.add(it)
        }
    }

    override fun mavenRepo(key: String): RepoSpec {
        if (isDefaultRepos) {
            repos.clear()
            isDefaultRepos = false
        }
        val repo = MavenRepoSpec(project, key)
        if (!repos.contains(repo)) repos.add(repo)
        return repo
    }

    override fun mavenLocal(): RepoSpec {
        if (isDefaultRepos) {
            repos.clear()
            isDefaultRepos = false
        }
        return repos.find { it is MavenLocalSpec } ?: MavenLocalSpec().also {
            repos.add(it)
        }
    }

    override fun gradlePortal(): RepoSpec {
        if (isDefaultRepos) {
            repos.clear()
            isDefaultRepos = false
        }
        return repos.find { it is GradlePortalSpec } ?: GradlePortalSpec().also {
            repos.add(it)
        }
    }

    override fun bintray(): RepoSpec {

        // validation: only one bintray spec is allowed

        if (isDefaultRepos) {
            repos.clear()
            isDefaultRepos = false
        }
        return repos.find { it is BintraySpec } ?: BintraySpec(project).also {
            repos.add(it)
        }
    }

    override fun getRepos(): Set<RepoSpec> {
        return repos
    }

    fun finalizeRepos() {
        if (repos.find { it is BintraySpec } != null) {
            mavenLocal()
        }
        pubList.forEach { (it as JarbirdPubImpl).finalizeRepos() }
    }
}
