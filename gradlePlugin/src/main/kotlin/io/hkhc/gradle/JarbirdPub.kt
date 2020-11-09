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

import appendBeforeSnapshot
import io.hkhc.gradle.maven.MavenCentralEndpoint
import io.hkhc.gradle.maven.PropertyRepoEndpoint
import io.hkhc.gradle.maven.RepoEndpoint
import io.hkhc.gradle.pom.Pom
import isSnapshot
import org.gradle.api.Project
import java.io.File

class JarbirdPub(@Suppress("unused") private val project: Project) {

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
     * if set, the deployed version will be suffixed with the variant name, delimited by '-'.
     * if version is a SNAPSHOT, the variant is added before SNAPSHOT.
     * if variant is empty, the version is not altered
     *
     * e.g.
     * version = "1.0", variant = "" -> "1.0"
     * version = "1.0", variant = "debug" -> "1.0-debug"
     * version = "1.0-SNAPSHOT", variant = "debug" -> "1.0-debug-SNAPSHOT"
     */

    private var variantMode: VariantMode = VariantMode.Invisible
    fun variantWithVersion() {
        variantMode = VariantMode.WithVersion
    }
    fun variantWithArtifactId() {
        variantMode = VariantMode.WithArtifactId
    }
    fun variantInvisible() {
        variantMode = VariantMode.Invisible
    }

    fun variantArtifactId(): String? {
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

    fun variantVersion(): String? {
        return pom.version?.let { ver ->
            when {
                variantMode != VariantMode.WithVersion -> ver
                variant == "" -> ver
                ver.isSnapshot() -> ver.appendBeforeSnapshot(variant)
                else -> "$ver-$variant"
            }
        }
    }

    fun getGAV(): String? {
        return "${pom.group}:${variantArtifactId()}:${variantVersion()}"
    }

    /**
     * The name of component to to be published
     */
    var pubComponent: String = "java"

    /**
     * the dokka task provider object for documentation. If it is not specified, the default dokka task will be used.
     */
    var dokka: Any? = null

    /**
     * The name of sourceset for archiving.
     * if sourcesPath is not provided, the plugin try to get the sources set named [sourceSetName] for source jar task
     */
    var sourceSetName: String = "main"

    // TODO make it flexible for more data type
    var sourceSets: Collection<File>? = null
    /**
     * Use if performing signing with external GPG command. false to use Gradle built-in PGP implementation.
     * We will need useGpg=true if we use new keybox (.kbx) format for pur signing key.
     */
    var useGpg = false

    /**
     * Specify maven repository for publishing.
     */
    var mavenRepository: RepoEndpoint? = null

    fun withMavenCentral() {
        mavenRepository = MavenCentralEndpoint(project)
    }

    fun withMavenByProperties(key: String) {
        mavenRepository = PropertyRepoEndpoint(project, "maven.$key")
    }
}

internal fun List<JarbirdPub>.needSigning() = any { it.signing }
internal fun List<JarbirdPub>.needBintray() = any { it.bintray }
internal fun List<JarbirdPub>.needGradlePlugin() = any { it.pom.isGradlePlugin() }
