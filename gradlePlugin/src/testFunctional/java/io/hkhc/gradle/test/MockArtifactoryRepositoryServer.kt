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

class MockArtifactoryRepositoryServer : BaseMockRepositoryServer() {

    override fun setupMatcher(coordinate: Coordinate) = with(coordinate) {
        listOf(
            PutMatcher("/base/oss-snapshot-local/${getPath()}", Success),
            GetMatcher("/base/api/system/version") { request, response ->
                response.setBody(
                    """
                    {
                      "version" : "6.18.1",
                      "revision" : "61801900",
                      "addons" : [],
                      "license" : "1e7f3be7c2a8bbfaf1c4e7c41779d89664a440e93"
                    }
                    """.trimIndent()
                )
            },
            PutMatcher("/base/api/build", Success)
        )
    }
}
