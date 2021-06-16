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

package io.hkhc.gradle.internal.repo

import io.hkhc.gradle.internal.LOG_PREFIX
import io.hkhc.gradle.internal.ProjectProperty
import io.hkhc.gradle.internal.utils.PropertyBuilder
import io.hkhc.gradle.internal.utils.normalizePubName
import org.gradle.api.GradleException
import org.gradle.api.Project

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
            id = normalizePubName("maven.$key").capitalize()
        )
    }

    fun buildArtifactoryRepo(key: String): ArtifactoryRepoSpec =
        with(PropertyBuilder(projectProperty, "artifactory.$key")) {
            val result = ArtifactoryRepoSpecImpl(
                releaseUrl = resolve("release"),
                snapshotUrl = resolve("snapshot"),
                username = resolve("username"),
                password = resolve("password"),
                description = resolve("description", "Artifactory repository '$key'"),
                repoKey = resolve("repoKey"),
                id = normalizePubName("artifactory.$key").capitalize()
            )
            if (result.releaseUrl.isEmpty() && result.snapshotUrl.isEmpty()) {
                throw GradleException(
                    "Artifactory repository ${result.id} must have either 'release' or 'snapshot' property"
                )
            }
            if (result.repoKey.isEmpty()) {
                throw GradleException(
                    "Artifactory repository ${result.id} must have 'repoKey' property."
                )
            }
            return result
        }

    fun buildMavenCentral(project: Project): MavenRepoSpec = with(PropertyBuilder(projectProperty, "mavencentral")) {

        if (resolve("newUser", "XXX") == "XXX") {
            project.logger.warn(
                "WARNING: [$LOG_PREFIX] OSSRH user has not been specified to be new or old. " +
                    "If the account is created before February 2021, it is old account. " +
                    "Add 'repository.mavencentral.newUser=false' to gradle.properties. " +
                    "Otherwise, new account is assumed."
            )
        }

        val newUser = resolve("newUser", "true").toBoolean()

        MavenCentralRepoSpecImpl(
            username = resolve("username"),
            password = resolve("password"),
            newUser = newUser
        )
    }
}
