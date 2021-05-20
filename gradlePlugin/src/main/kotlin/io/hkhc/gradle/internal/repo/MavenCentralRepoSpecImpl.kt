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

data class MavenCentralRepoSpecImpl(
    override val username: String,
    override val password: String,
    override val newUser: Boolean
) : MavenCentralRepoSpec {

    private fun host() = if (newUser) "s01.oss.sonatype.org" else "oss.sonatype.org"

    override val description = "Maven Central"
    override val id = "MavenCentral"
    override val releaseUrl: String = "https://${host()}/service/local/staging/deploy/maven2"
    override val snapshotUrl: String = "https://${host()}/content/repositories/snapshots"
    override val isAllowInsecureProtocol: Boolean = false
}
