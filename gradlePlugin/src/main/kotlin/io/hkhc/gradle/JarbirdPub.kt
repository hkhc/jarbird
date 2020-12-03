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

package io.hkhc.gradle

import io.hkhc.gradle.endpoint.RepoEndpoint
import io.hkhc.gradle.pom.Pom
import org.jetbrains.dokka.gradle.AbstractDokkaTask

abstract class JarbirdPub {

    lateinit var pom: Pom

    /**
     * Configure for Maven publishing or not (No matter maven central or alternate maven repository)
     */
    var maven = true

    /**
     * Configure for Bintray publishing or not
     */
    var bintray = true

    /**
     * Configure for artifact signing or not
     */
    var signing = true

    /**
     * the publication name, that affect the task names for publishing
     */
    var pubName: String = "lib"

    /**
     * the variant is string that as suffix of pubName to form a final publication name. It is also used
     * to suffix the dokka jar task and source set jar task.
     * It is usually used for building Android artifact
     */
    var variant: String = ""

    /**
     * The name of component to to be published
     */
    var pubComponent: String = "java"

    var sourceSets: Any = "main"

    /**
     * Use if performing signing with external GPG command. false to use Gradle built-in PGP implementation.
     * We will need useGpg=true if we use new keybox (.kbx) format for pur signing key.
     */
    var useGpg = false

    var dokkaConfig: AbstractDokkaTask.(pub: JarbirdPub) -> Unit = {}

    abstract fun variantWithVersion()
    abstract fun variantWithArtifactId()
    abstract fun variantInvisible()

    abstract fun variantArtifactId(): String?
    abstract fun variantVersion(): String?
    abstract fun getGAV(): String?
    abstract fun pluginCoordinate(): String?

    abstract fun sourceSetNames(vararg names: String): Any
    abstract fun sourceSetNames(names: List<String>): Any
    abstract fun sourceDirs(dirs: Any): Any

    /**
     * Specify maven repository for publishing.
     */
    abstract fun withMaven(endpoint: RepoEndpoint)
    abstract fun withMavenCentral()
    abstract fun withMavenByProperties(key: String)
}
