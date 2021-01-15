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

import groovy.util.GroovyTestCase
import io.hkhc.gradle.internal.SNAPSHOT_SUFFIX
import io.kotest.assertions.fail
import okhttp3.mockwebserver.RecordedRequest

class MavenPublishingChecker(
    val coordinates: List<Coordinate>,
    private val packaging: String,
    private val withMetadata: Boolean = true
) {

    private fun assertFile(requests: List<RecordedRequest>, pathRegex: Regex) {
        val matched = requests
            .filter { it.method == "PUT" }
            .any { it.path?.let { path -> pathRegex.matches(path) } ?: false }
        if (!matched) {
            fail("$pathRegex does not match any recorded request")
        }
    }

    private fun transformReleaseVersion(version: String) = version

    private fun transformSnapshotVersion(version: String): String {
        val snapshotVersion = version.indexOf(SNAPSHOT_SUFFIX)
            .let { if (it == -1) version else version.substring(0, it) }
        return "$snapshotVersion-[0-9]+.[0-9]+-[0-9]+"
    }

    fun assertArtifacts(recordedRequests: List<RecordedRequest>) {

        val expectedPaths = MavenRepoPatterns(
            "/base",
            coordinates,
            packaging,
            ::transformReleaseVersion,
            ::transformSnapshotVersion,
            withMetadata
        ).let {
            it.list()
        }.apply {
            forEach { assertFile(recordedRequests, it) }
        }

        val remainingPaths = recordedRequests
            .filter {
                it.method == "PUT" &&
                    expectedPaths.none { regex -> it.path?.let { path -> regex.matches(path) } ?: false }
            }

        GroovyTestCase.assertEquals(
            "all request to repository server are expected",
            "",
            remainingPaths.map { it.path }.joinToString("\n")
        )
    }
}
