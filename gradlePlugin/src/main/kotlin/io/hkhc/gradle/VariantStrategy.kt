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

package io.hkhc.gradle

import io.hkhc.gradle.pom.Pom

interface VariantStrategy {

    val variant: String
    var pom: Pom

    /**
     * variant is combined with version
     * e.g. mygroup:mylib:1.0.0-variant
     */
    fun variantWithVersion()

    /**
     * variant is combined with artifactId
     * e.g. mygroup:mylib-variant:1.0.0
     */
    fun variantWithArtifactId()

    /**
     * variant is not shown in coordinate
     * e.g. mygroup:mylib:1.0.0
     */
    fun variantInvisible()

    /**
     * get the artifactID augmented by variatn
     * e.g. mylib-variant
     */
    fun variantArtifactId(): String?

    /**
     * get the version augmented by variatn
     * e.g. 1.0.0-variant
     */
    fun variantVersion(): String?
}
