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

package io.hkhc.gradle.test.mavenlocal

import io.hkhc.gradle.test.LocalRepoResult
import io.hkhc.gradle.test.artifacory.ArtifactChecker
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult

fun publishToMavenLocalCompletely() = object : Matcher<LocalRepoResult> {
    override fun test(value: LocalRepoResult): MatcherResult {
        ArtifactChecker().verifyRepository(value.repoDir, value.coordinate, value.packaging)
        return MatcherResult(
            true,
            "All files should be published to Maven local repository",
            "Not all files should be published to Maven local repository"
        )
    }
}

fun publishPluginToMavenLocalCompletely() = object : Matcher<LocalRepoResult> {
    override fun test(value: LocalRepoResult): MatcherResult {
        ArtifactChecker().verifyPluginMarkerRepository(value.repoDir, value.coordinate)
        return MatcherResult(
            true,
            "All files should be published to Maven local repository",
            "Not all files should be published to Maven local repository"
        )
    }
}
