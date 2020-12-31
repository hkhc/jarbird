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

import io.hkhc.gradle.BintrayRepoSpec
import io.hkhc.gradle.internal.ProjectProperty

class ArtifactoryRepoSpecImpl(projectProperty: ProjectProperty) : PropertyArtifactoryRepoSpec(projectProperty, "artifactory")

class BintraySnapshotRepoSpecImpl(private val bintrayRepoSpec: BintrayRepoSpec) :
    AbstractRepoSpec(),
    ArtifactoryRepoSpec {

    override val repoKey: String
        get() = "oss-snapshot-local"
    override val releaseUrl: String
        get() = ""
    override val snapshotUrl: String
        get() = bintrayRepoSpec.snapshotUrl
    override val username: String
        get() = bintrayRepoSpec.username
    override val password: String
        get() = bintrayRepoSpec.password
    override val description: String
        get() = bintrayRepoSpec.description
    override val id: String
        get() = bintrayRepoSpec.id
}
