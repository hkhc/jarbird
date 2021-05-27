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

package io.hkhc.gradle.endpoint

import io.hkhc.gradle.JarbirdPlugin
import io.hkhc.gradle.internal.ProjectProperty
import io.hkhc.gradle.internal.utils.normalizePubName

class PropertyRepoEndpoint(private val projectProperty: ProjectProperty, private val key: String) :
    AbstractRepoEndpoint() {

    private val keyPrefix = "repository"

    override val releaseUrl: String
        get() {
//            Exception("PropertyMavenEndpoint release get").printStackTrace()
            return resolveProperty(projectProperty, "$keyPrefix.$key.release")
        }

    override val snapshotUrl: String
        get() = resolveProperty(projectProperty, "$keyPrefix.$key.snapshot")

    override val username: String
        get() = resolveProperty(projectProperty, "$keyPrefix.$key.username")

    override val password: String
        get() = resolveProperty(projectProperty, "$keyPrefix.$key.password")

    override val apikey: String
        get() = resolveProperty(projectProperty, "$keyPrefix.$key.apikey")

    override val description: String
        get() {
            val customDescription = resolveProperty(projectProperty, "$keyPrefix.$key.description")
            return if (customDescription == "") {
                "Maven Repository specified by repository.${key.replace(".", "")}.* in gradle.properties"
            } else {
                customDescription
            }
        }

    override val id: String
        get() = normalizePubName(key).capitalize()
//        get() = "${key.replace(".", "").capitalize()}"
}
