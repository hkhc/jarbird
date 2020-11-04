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

package io.hkhc.gradle.maven

import org.gradle.api.GradleException
import org.gradle.api.Project

class MavenCentralEndpoint(val project: Project) : RepoEndpoint {

    private val keyPrefix = "repository"

    override val releaseUrl: String
        get() = "https://oss.sonatype.org/service/local/staging/deploy/maven2"

    override val snapshotUrl: String
        get() = "https://oss.sonatype.org/content/repositories/snapshots"

    override val username: String
        get() {
            try {
                return resolveProperty(
                    project,
                    "repository.mavencentral.username"
                )
            } catch (g: GradleException) {
                project.logger.warn("Maven Central username is not found.")
                return ""
            }
        }

    override val password: String
        get() {
            try {
                return resolveProperty(
                    project,
                    "repository.mavencentral.password"
                )
            } catch (g: GradleException) {
                project.logger.warn("Maven Central password is not found.")
                return ""
            }
        }

    override val apikey: String
        get() = ""

    override val name: String
        get() = "MavenCentral"
}

fun Project.mavenCentral(): RepoEndpoint {
    return MavenCentralEndpoint(this)
}
