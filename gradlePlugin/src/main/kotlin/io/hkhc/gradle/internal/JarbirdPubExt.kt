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

package io.hkhc.gradle.internal

import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.RepoSpec
import io.hkhc.gradle.internal.repo.BintraySpec
import io.hkhc.gradle.internal.repo.MavenSpec

internal fun JarbirdPub.pubNameWithVariant(pubName: String = this.pubName): String {
    return "${pubName}${variant.capitalize()}"
}

// If function is suffixed with "Cap" this means the callers do not need to worry about the first letter case.
// It should always be capitalized. If it is not with "Cap", then there is no such guarantee.

internal val JarbirdPub.pubNameCap: String
    get() = pubNameWithVariant().capitalize()

internal val JarbirdPub.markerPubName: String
    get() = pubNameWithVariant() + PLUGIN_MARKER_PUB_SUFFIX

internal val JarbirdPub.markerPubNameCap: String
    get() = (pubNameWithVariant() + PLUGIN_MARKER_PUB_SUFFIX).capitalize()

internal val RepoSpec.repoName: String
    get() = getEndpoint().id

internal fun List<JarbirdPub>.needSigning() = any { it.signing }

internal fun List<JarbirdPub>.needGradlePlugin() = any { it.pom.isGradlePlugin() }

internal fun JarbirdPub.needsBintray(): Boolean {
    return (this as JarbirdPubImpl).getRepos().filterIsInstance<BintraySpec>().isNotEmpty()
}

internal fun List<JarbirdPub>.needsBintray() = any { it.needsBintray() }

internal fun JarbirdPub.needsNonLocalMaven(): Boolean {
    return (this as JarbirdPubImpl).getRepos().filterIsInstance<MavenSpec>().isNotEmpty()
}

internal fun List<JarbirdPub>.needsNonLocalMaven() = any { it.needsNonLocalMaven() }
