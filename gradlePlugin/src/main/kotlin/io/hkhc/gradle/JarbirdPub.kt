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
import org.jetbrains.dokka.gradle.AbstractDokkaTask

interface JarbirdPub : RepoDeclaration {

    val pom: Pom

    /**
     * the publication name, that affect the task names for publishing
     */
    val pubName: String

    val variant: String

    val docSourceSets: Any

    /**
     * Not to do signing for this pub
     */
    fun doNotSign()

    /**
     * Do signing for this pub (default)
     */
    fun shouldSign(): Boolean

    /**
     * Use GnuPG v1 Keyring to perform artifact signing
     */
    fun signWithKeyring()

    /**
     * return true if the artifacts is going to be signed with keyring
     */
    fun isSignWithKeyring(): Boolean

    /**
     * Use GnuPG v2 Keybox to perform artifact signing
     */
    fun signWithKeybox()

    /**
     * return true if the artifacts is going to be signed with keybox
     */
    fun isSignWithKeybox(): Boolean

    fun configureDokka(block: AbstractDokkaTask.(pub: JarbirdPub) -> Unit)

    /**
     * provide information on how the project is build. The parameter could be instance of
     * - SoftwareComponent
     * - SourceSet
     */
    fun from(source: Any)

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

    /**
     * get the effective coordinate
     */
    fun getGAV(): String?

    /**
     * get the plugin coordinate
     */
    fun pluginCoordinate(): String?

    fun sourceSetNames(vararg names: String): Any
    fun sourceSetNames(names: List<String>): Any
    fun sourceDirs(dirs: Any): Any
}
