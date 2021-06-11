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

package io.hkhc.gradle.internal.maven

import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.internal.markerPubName
import io.hkhc.gradle.internal.pubNameWithVariant
import io.hkhc.gradle.internal.repo.MavenRepoSpec
import io.hkhc.gradle.internal.repoName
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.publish.maven.MavenPublication

class MavenPublicationFilter {

    fun filter(
        pubs: List<JarbirdPub>,
        repository: MavenArtifactRepository,
        publication: MavenPublication
    ): Boolean {
        return pubs.any { pub ->
            pub.getRepos()
                .filterIsInstance<MavenRepoSpec>()
                .any { repoSpec -> pub.needs(repoSpec, repository, publication) }
        }
    }

    /**
     * return true if the combination of repository and publication is needed by the Pub/RepoSpec pair
     */
    fun JarbirdPub.needs(
        repoSpec: MavenRepoSpec,
        repository: MavenArtifactRepository,
        publication: MavenPublication
    ): Boolean {

        // return true if the repo name matches and the pubName matches
        return if (repoSpec.repoName == repository.name) {
            if (pom.isGradlePlugin()) {
                pubNameWithVariant() == publication.name || markerPubName == publication.name
            } else {
                pubNameWithVariant() == publication.name
            }
        } else {
            false
        }
    }
}
