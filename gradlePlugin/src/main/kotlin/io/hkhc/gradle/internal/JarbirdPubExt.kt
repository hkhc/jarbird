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

internal fun JarbirdPub.pubNameWithVariant(pubName: String = this.pubName): String {
    return "${pubName}${variant.capitalize()}"
}

// If function is suffixed with "Cap" this means the callers do not need to worry about the first letter case.
// It should always be capitalized. If it is not with "Cap", then there is no such guarantee.

internal val JarbirdPub.pubNameCap: String
    get() = pubNameWithVariant().capitalize()

internal val JarbirdPub.pubId: String
    get() = "${pubNameCap}Publication"

internal val JarbirdPub.mavenRepoNameCap: String
    get() = "Maven${pubNameCap}Repository"

internal fun List<JarbirdPub>.needSigning() = any { it.signing }

internal fun List<JarbirdPub>.needBintray() = any { it.bintray }

internal fun List<JarbirdPub>.needGradlePlugin() = any { it.pom.isGradlePlugin() }
