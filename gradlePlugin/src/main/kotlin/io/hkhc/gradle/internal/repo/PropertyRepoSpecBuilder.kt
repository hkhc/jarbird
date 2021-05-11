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

package io.hkhc.gradle.internal.repo

import io.hkhc.gradle.JarbirdPlugin
import io.hkhc.gradle.internal.ProjectProperty
import io.hkhc.gradle.internal.utils.PropertyBuilder

class PropertyRepoSpecBuilder(
    private val projectProperty: ProjectProperty
) {

    fun buildMavenLocalRepo(): MavenLocalRepoSpec = MavenLocalRepoSpecImpl()

    fun buildGradlePluginRepo(): GradlePortalSpec = GradlePortalSpecImpl()

    fun buildMavenRepo(key: String): MavenRepoSpec = with(PropertyBuilder(projectProperty, "maven.$key")) {
        MavenRepoSpecImpl(
            releaseUrl = resolve("release"),
            snapshotUrl = resolve("snapshot"),
            username = resolve("username"),
            password = resolve("password"),
            description = resolve("description", "Maven repository '$key'"),
            isAllowInsecureProtocol = resolve("allowSecureProtocol", "false") == "true",
            id = JarbirdPlugin.normalizePubName("maven.$key").capitalize()
        )
    }

    fun buildBintrayRepo(): BintrayRepoSpec = with(PropertyBuilder(projectProperty, "bintray")) {
        BintrayRepoSpecImpl(
            releaseUrl = resolve("release", ""), // Bintray plugin will fill the URL if we don't provide one
            snapshotUrl = resolve("snapshot", "https://oss.jfrog.org"),
            username = resolve("username"),
            description = resolve("description", "Bintray"),
            apikey = resolve("apikey"),
            id = "Bintray"
        )
    }

    fun buildArtifactoryRepo(): ArtifactoryRepoSpec = with(PropertyBuilder(projectProperty, "artifactory")) {
        ArtifactoryRepoSpecImpl(
            releaseUrl = resolve("release"),
            snapshotUrl = resolve("snapshot"),
            username = resolve("username"),
            password = resolve("password"),
            description = resolve("description", "Artifactory"),
            repoKey = resolve("repoKey"),
            id = "Artifactory"
        )
    }

    fun buildMavenCentral(): MavenRepoSpec = with(PropertyBuilder(projectProperty, "mavencentral")) {

        MavenCentralRepoSpecImpl(
            username = resolve("username"),
            password = resolve("password"),
            description = "Maven Central",
            id = "MavenCentral"
        )
    }

    fun buildBintraySnapshotRepoSpec(bintrayRepoSpec: BintrayRepoSpec) =
        BintraySnapshotRepoSpecImpl(
            repoKey = "oss-snapshot-local",
            releaseUrl = "",
            snapshotUrl = bintrayRepoSpec.snapshotUrl,
            username = bintrayRepoSpec.username,
            password = bintrayRepoSpec.apikey,
            description = "Artifactory OSS Snapshot",
            id = bintrayRepoSpec.id
        )
}
