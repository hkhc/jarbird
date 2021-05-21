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

package io.hkhc.gradle.test.bintray

import io.hkhc.gradle.test.BaseMockRepositoryServer
import io.hkhc.gradle.test.Coordinate
import io.hkhc.gradle.test.FileNotFound
import io.hkhc.gradle.test.HTTP_SUCCESS
import io.hkhc.gradle.test.HeadMatcher
import io.hkhc.gradle.test.PostMatcher
import io.hkhc.gradle.test.PutMatcher
import io.hkhc.gradle.test.Success

class MockBintrayRepositoryServer : BaseMockRepositoryServer() {

    var postFileCount = 0
    val username = "username"
    val repo = "maven"

    // TODO fix multi coordinates matcher
    override fun setupMatcher(coordinates: List<Coordinate>, baseUrl: String) = coordinates.flatMap { coordinate ->
        val first = coordinates[0]
        with(coordinate) {
            listOf(
                HeadMatcher(
                    "/packages/$username/$repo/${first.artifactIdWithVariant}",
                    FileNotFound
                ),
                PostMatcher(
                    "/packages/$username/$repo",
                    Success
                ),
                HeadMatcher(
                    "/packages/$username/$repo/${first.artifactIdWithVariant}/versions/$versionWithVariant",
                    FileNotFound
                ),
                PostMatcher(
                    "/packages/$username/$repo/${first.artifactIdWithVariant}/versions",
                    Success
                ),
                PutMatcher(
                    "/content/$username/$repo/${first.artifactIdWithVariant}/${first.versionWithVariant}"
                ) { request, response ->
                    postFileCount++
                    Success.invoke(request, response)
                },
                PostMatcher(
                    "/content/$username/$repo/${first.artifactIdWithVariant}/${first.versionWithVariant}/publish"
                ) { _, response ->
                    response.setBody("{ \"files\": $postFileCount }").setResponseCode(HTTP_SUCCESS)
                }
            )
        }
    }
}
