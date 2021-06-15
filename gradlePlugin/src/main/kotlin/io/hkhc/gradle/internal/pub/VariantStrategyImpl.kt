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

import io.hkhc.gradle.VariantMode
import io.hkhc.gradle.VariantStrategy

open class VariantStrategyImpl(
    private val parent: VariantStrategy? = null
) : VariantStrategy {

    private var mVariantMode: VariantMode? = null

    override val variantMode: VariantMode
        get() {
            return mVariantMode ?: parent?.variantMode ?: VariantMode.WithVersion
        }

    override fun variantWithVersion() {
        mVariantMode = VariantMode.WithVersion
    }
    override fun variantWithArtifactId() {
        mVariantMode = VariantMode.WithArtifactId
    }
    override fun variantInvisible() {
        mVariantMode = VariantMode.Invisible
    }
}
