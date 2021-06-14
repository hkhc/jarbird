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

package io.hkhc.gradle.internal.bintray

import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.internal.markerPubName
import io.hkhc.gradle.internal.pubNameWithVariant
import io.hkhc.gradle.internal.repo.ArtifactoryRepoSpec
import io.hkhc.gradle.internal.repo.effectiveUrl
import io.hkhc.gradle.internal.reposWithType
import org.gradle.api.GradleException

class ArtifactoryConfigModel(pubs: List<JarbirdPub>) {

    private val pubsWithArtifactory = pubs.filter { it.getRepos().any { repos -> repos is ArtifactoryRepoSpec } }
    private val artifactoryRepoSpecs = pubs.reposWithType<ArtifactoryRepoSpec>()

    init {
        if (mixedSnapshotRelease(pubs)) {
            throw GradleException("Component published to Artifactory must be all release or all snapshot.")
        }

        if (artifactoryRepoSpecs.size > 1) {
            throw GradleException("Only one artifactory repo is supported in one project or sub-project.")
        }
    }

    fun needsArtifactory() = pubsWithArtifactory.isNotEmpty()

    // we know artifactoryRepoSpecs is not empty and has at most 1 element when we use iternator().next()
    val repoSpec = if (artifactoryRepoSpecs.isEmpty()) null else artifactoryRepoSpecs.iterator().next()
    val publications = pubsWithArtifactory.flatMap { publicationsFromPub(it) }

    // if repoSpec is not null, pubsWithArtifactory should be non-empty
    val contextUrl = repoSpec?.effectiveUrl(pubsWithArtifactory[0])

    private fun publicationsFromPub(pub: JarbirdPub): List<String> {
        val result = mutableListOf<String>()
        result.add(pub.pubNameWithVariant())
        if (pub.pom.isGradlePlugin())
            result.add(pub.markerPubName)
        return result
    }

    private fun mixedSnapshotRelease(pubs: List<JarbirdPub>): Boolean {
        val releaseCount = pubs.count { it.pom.isRelease() }
        return (releaseCount != pubs.size) && (releaseCount != 0)
    }
}
