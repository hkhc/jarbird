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

package io.hkhc.gradle

import groovy.lang.Closure
import io.hkhc.gradle.maven.PropertyRepoEndpoint
import io.hkhc.gradle.maven.RepoEndpoint
import io.hkhc.gradle.pom.PomGroup
import org.gradle.api.Project

// Gradle plugin extensions must be open classes so that Gradle system can "decorate" it.
open class JarbirdExtension(@Suppress("unused") private val project: Project) {

    var pubList = mutableListOf<JarbirdPub>()

    var bintrayRepository: RepoEndpoint? = null



    lateinit var pomGroupCallback: PomGroupCallback
    private var implicited: JarbirdPub? = null

    /* to be invoked by Groovy Gradle script */
    fun pub(action: Closure<JarbirdPub>) {
        System.out.println("add new pub by closure")
        val newPub = JarbirdPub(project)
        pubList.add(newPub)
        action.delegate = newPub
        action.call()
        pomGroupCallback.initPub(newPub)
    }

    /* to be invoked by Kotlin Gradle script */
    fun pub(action: JarbirdPub.() -> Unit) {
        val newPub = JarbirdPub(project)
        pubList.add(newPub)
        action.invoke(newPub)
        pomGroupCallback.initPub(newPub)
    }

    fun createImplicit() {
        /*
        if implicit != null, we have alrady got an implicit, no need to create another
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
        if (implicited == null || pubList.size == 1 ) return
        pubList.remove(implicited)
        implicited = null
    }

}
