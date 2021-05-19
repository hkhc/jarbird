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

package io.hkhc.gradle.internal.bintray

import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.internal.PLUGIN_MARKER_PUB_SUFFIX
import io.hkhc.gradle.internal.markerPubName
import io.hkhc.gradle.internal.needsBintray
import io.hkhc.gradle.internal.pubNameWithVariant

class BintrayPublishPlan(val pubs: List<JarbirdPub>) {
//    val bintray: MutableList<JarbirdPub> = mutableListOf()
//    val artifactory: MutableList<JarbirdPub> = mutableListOf()
    val artifactoryPlugins: MutableList<JarbirdPub> = mutableListOf()
    val bintrayLibs: MutableList<JarbirdPub> = mutableListOf()
    val artifactoryLibs: MutableList<JarbirdPub> = mutableListOf()
    val bintrayPlugins: MutableList<JarbirdPub> = mutableListOf()
    val invalidPlugins: MutableList<JarbirdPub> = mutableListOf()

    init {
        pubs.filter { it.needsBintray() }.forEach {
            if (it.pom.isSnapshot()) {
                artifactoryLibs.add(it)
                if (it.pom.isGradlePlugin()) {
                    artifactoryPlugins.add(it)
                }
            } else {
                bintrayLibs.add(it)
                if (it.pom.isGradlePlugin()) {
                    bintrayPlugins.add(it)
                }
            }
        }
    }

    fun bintrayPublications(): List<String> {
        return mutableListOf<String>().apply {
            addAll(bintrayLibs.map { it.pubNameWithVariant() })
            addAll(bintrayPlugins.map { it.markerPubName})
        }
    }

    fun artifactoryPublications(): List<String> {
        return mutableListOf<String>().apply {
            addAll(artifactoryLibs.map { it.pubNameWithVariant() })
            addAll(artifactoryPlugins.map { it.markerPubName })
        }
    }
}
