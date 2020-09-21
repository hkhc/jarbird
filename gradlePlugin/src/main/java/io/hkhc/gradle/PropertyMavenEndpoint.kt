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

import org.gradle.api.Project

class PropertyMavenEndpoint(private val project: Project, private val key: String) : MavenEndpoint {

    init {
        Exception("PropertyMavenEndpoint constructor").printStackTrace()
    }

    private val keyPrefix = "repository"

    override val releaseUrl: String
        get() {
            Exception("PropertyMavenEndpoint release get").printStackTrace()
            return resolveProperty(project, "$keyPrefix.$key.release")
        }

    override val snapshotUrl: String
        get() = resolveProperty(project, "$keyPrefix.$key.snapshot")

    override val username: String
        get() = resolveProperty(project, "$keyPrefix.$key.username")

    override val password: String
        get() = resolveProperty(project, "$keyPrefix.$key.password")
}
