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

@Suppress("TooManyFunctions")
class MockBintrayRepositoryServer : BaseMockRepositoryServer() {

    var postFileCount = 0
    val username = "username"
    val repo = "maven"

    override fun setupMatcher(coordinate: Coordinate) = with(coordinate) {
        listOf(
            HeadMatcher("/packages/$username/$repo/$artifactId", FileNotFound),
            PostMatcher("/packages/$username/$repo", Success),
            HeadMatcher("/packages/$username/$repo/$artifactId/versions/$versionWithVariant", FileNotFound),
            PostMatcher("/packages/$username/$repo/$artifactId/versions", Success),
            PutMatcher("/content/$username/$repo/$artifactId/$versionWithVariant") { request, response ->
                postFileCount++
                Success.invoke(request, response)
            },
            PostMatcher("/content/$username/$repo/$artifactId/$versionWithVariant/publish") { _, response ->
                response.setBody("{ \"files\": $postFileCount }").setResponseCode(HTTP_SUCCESS)
            }
        )
    }
}
