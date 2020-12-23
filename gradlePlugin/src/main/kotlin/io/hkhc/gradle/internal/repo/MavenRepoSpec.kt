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

import io.hkhc.gradle.RepoSpec
import io.hkhc.gradle.endpoint.PropertyRepoEndpoint
import io.hkhc.gradle.endpoint.RepoEndpoint
import org.gradle.api.Project

class MavenRepoSpec(project: Project, key: String) : RepoSpec(), MavenSpec {

    private val endpoint = PropertyRepoEndpoint(project, "maven.$key")

    override fun getEndpoint(): RepoEndpoint {
        return endpoint
    }
}
