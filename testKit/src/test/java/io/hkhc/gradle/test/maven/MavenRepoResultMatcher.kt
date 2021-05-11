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

package io.hkhc.gradle.test.maven

import io.hkhc.gradle.test.MavenRepoResult
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult

fun publishedToMavenRepositoryCompletely(withMetadata: Boolean = true) = object : Matcher<MavenRepoResult> {
    override fun test(value: MavenRepoResult): MatcherResult {
        MavenPublishingChecker(value.coordinates, value.packaging, withMetadata).assertArtifacts(value.recordedRequests)
        return MatcherResult(
            true,
            "All files should be published to Maven repository",
            "Not all files should be published to Maven repository"
        )
    }
}
