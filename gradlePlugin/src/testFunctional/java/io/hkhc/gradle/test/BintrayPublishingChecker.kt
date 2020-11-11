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

package io.hkhc.gradle.test

import io.hkhc.gradle.utils.SNAPSHOT_SUFFIX
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals

class BintrayPublishingChecker(val coordinate: Coordinate, private val packaging: String) {

    private fun assertFile(requests: List<RecordedRequest>, pathRegex: Regex) {
        val matched = requests
            .filter { it.method == "PUT" }
            .any { it.path?.let { path -> pathRegex.matches(path) } ?: false }
        Assertions.assertTrue(
            matched,
            "$pathRegex does not match any recorded request\n" +
                requests.map { it.path }.joinToString("\n")
        )
    }

    private fun transformReleaseVersion(version: String) = version

    private fun transformSnapshotVersion(version: String): String {
        val snapshotVersion = version.indexOf(SNAPSHOT_SUFFIX)
            .let { if (it == -1) version else version.substring(0, it) }
        return "$snapshotVersion-[0-9]+.[0-9]+-[0-9]+"
    }

    fun assertReleaseArtifacts(recordedRequests: List<RecordedRequest>, username: String, repo: String) {
        assertArtifacts(recordedRequests, ::transformReleaseVersion, username, repo)
    }

    fun assertArtifacts(
        recordedRequests: List<RecordedRequest>,
        versionTransformer: (String) -> String,
        username: String,
        repo: String
    ) {

        val expectedPaths = BintrayRepoPatterns(
            coordinate,
            username,
            repo,
            packaging
        ).list(versionTransformer).apply {
            forEach { assertFile(recordedRequests, it) }
        }

        val remainingPaths = recordedRequests
            .filter {
                it.method == "PUT" &&
                    expectedPaths.none { regex -> it.path?.let { path -> regex.matches(path) } ?: false }
            }

        assertEquals(
            "",
            remainingPaths.map { it.path }.joinToString("\n"),
            "all request to repository server are expected"
        )
    }
}
