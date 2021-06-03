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
import io.hkhc.gradle.internal.pluginMarkerPubNameWithVariant
import io.hkhc.gradle.internal.pubNameWithVariant
import io.hkhc.gradle.internal.repo.ArtifactoryRepoSpec

class BintrayPublishPlan(val pubs: List<JarbirdPub>) {
    val artifactoryPlugins: MutableList<JarbirdPub> = mutableListOf()
    val artifactoryLibs: MutableList<JarbirdPub> = mutableListOf()
    val invalidPlugins: MutableList<JarbirdPub> = mutableListOf()

    init {
        pubs.filter { it.getRepos().any { it is ArtifactoryRepoSpec } }.forEach {
            artifactoryLibs.add(it)
            if (it.pom.isGradlePlugin()) {
                artifactoryPlugins.add(it)
            }
        }
    }

    fun artifactoryPublications(): List<String> {
        return mutableListOf<String>().apply {
            addAll(artifactoryLibs.map { it.pubNameWithVariant() })
            addAll(artifactoryPlugins.map { it.pluginMarkerPubNameWithVariant() })
        }
    }
}
