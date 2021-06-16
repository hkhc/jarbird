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

package io.hkhc.gradle.internal

import io.hkhc.gradle.JarbirdExtension
import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.RepoSpec
import org.gradle.api.GradleException

internal fun JarbirdPub.pubNameWithVariant(pubName: String = this.pubName): String {
    return "${pubName}${variant.capitalize()}"
}

internal fun JarbirdPub.pluginMarkerArtifactIdWithVariant(): String {
    return requireNotNull(pom.plugin) {
        "POM plugin is unexpectedly null. Probably a bug."
    }.run { "$id.gradle.plugin" }
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
    get() = id

internal fun List<JarbirdPub>.needSigning() = any { it.needsSigning() }

internal fun JarbirdPub.needsSigning() = isSignWithKeybox() || isSignWithKeyring()

internal inline fun <reified T : RepoSpec> JarbirdPub.reposWithType() =
    getRepos().filterIsInstance<T>()

internal inline fun <reified T : RepoSpec> List<JarbirdPub>.reposWithType() =
    flatMap { it.getRepos() }.filterIsInstance<T>().toSet()

internal inline fun <reified T : RepoSpec> JarbirdPub.needsReposWithType() = getRepos().any { it is T }

internal inline fun <reified T : RepoSpec> List<JarbirdPub>.needReposWithType() = any { it.needsReposWithType<T>() }

internal inline fun <reified T : RepoSpec> JarbirdExtension.reposWithType() =
    (this as JarbirdExtensionImpl).pubList.flatMap { it.reposWithType<T>() }

internal fun List<JarbirdPub>.needGradlePlugin() = any { it.pom.isGradlePlugin() }

internal fun List<JarbirdPub>.needsGenDoc() = any { it.needsGenDoc() }

// getDocOrNot may be null and (null!=false) == true
internal fun JarbirdPub.needsGenDoc() = genDocOrNot() != false
