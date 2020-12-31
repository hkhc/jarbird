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

import io.hkhc.gradle.endpoint.resolveProperty
import io.hkhc.gradle.internal.ProjectProperty

open class PropertyBintrayRepoSpec(private val projectProperty: ProjectProperty, private val key: String) :
    AbstractBintrayRepoSpec(projectProperty, key) {

    private val keyPrefix = "repository"

    override val apikey: String
        get() = resolveProperty(projectProperty, "$keyPrefix.$key.apikey")
}
