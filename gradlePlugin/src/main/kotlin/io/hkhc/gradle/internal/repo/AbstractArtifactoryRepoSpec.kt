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

import io.hkhc.gradle.internal.ProjectProperty

abstract class AbstractArtifactoryRepoSpec(projectProperty: ProjectProperty, key: String) :
    PropertyRepoSpec(projectProperty, key), ArtifactoryRepoSpec {

    override fun equals(other: Any?): Boolean {

        return other?.let {

            if (this === it) return true

            val that = it as? ArtifactoryRepoSpec ?: return false

            return super.equals(that) &&
                repoKey == that.repoKey
        } ?: false
    }

    override fun toString() =
        """
            ${super.toString()}
            Repo Key     : $repoKey
        """.trimIndent()

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + repoKey.hashCode()
        return result
    }
}
