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

package io.hkhc.gradle.internal.pub

import io.hkhc.gradle.PubVariantStrategy
import io.hkhc.gradle.VariantMode
import io.hkhc.gradle.VariantStrategy
import io.hkhc.gradle.pom.Pom
import io.hkhc.gradle.pom.internal.appendBeforeSnapshot
import io.hkhc.gradle.pom.internal.isSnapshot

class PubVariantStrategyImpl(
    override val variant: String = "",
    private val parent: VariantStrategy? = null
) : VariantStrategyImpl(parent), PubVariantStrategy {

    override lateinit var pom: Pom

    /**
     * if set, the deployed version will be suffixed with the variant name, delimited by '-'.
     * if version is a SNAPSHOT, the variant is added before SNAPSHOT.
     * if variant is empty, the version is not altered
     *
     * e.g.
     * version = "1.0", variant = "" -> "1.0"
     * version = "1.0", variant = "debug" -> "1.0-debug"
     * version = "1.0-SNAPSHOT", variant = "debug" -> "1.0-debug-SNAPSHOT"
     */
    override fun variantArtifactId(): String? {
        return pom.artifactId?.let { id ->
            when {
                variantMode != VariantMode.WithArtifactId -> {
                    id
                }
                variant == "" -> {
                    id
                }
                else -> {
                    "$id-$variant"
                }
            }
        }
    }

    override fun variantVersion(): String? {
        return pom.version?.let { ver ->
            when {
                variantMode != VariantMode.WithVersion -> ver
                variant == "" -> ver
                ver.isSnapshot() -> ver.appendBeforeSnapshot(variant)
                else -> "$ver-$variant"
            }
        }
    }
}
