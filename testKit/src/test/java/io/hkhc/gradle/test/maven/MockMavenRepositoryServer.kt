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

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class GetMatcherAtEnd(
    path: String,
    responseHandler: (RecordedRequest, MockResponse) -> MockResponse
) : GetMatcher(path, responseHandler) {
    override fun matches(request: RecordedRequest) = request.method == method && (request.path?.endsWith(path) ?: false)
}

class MockMavenRepositoryServer : BaseMockRepositoryServer() {

    // TODO fix multi coordinates matcher
    override fun setupMatcher(coordinates: List<Coordinate>, baseUrl: String) =
        listOf(
            PutMatcher("", Success),
            GetMatcherAtEnd("maven-metadata.xml") { _, response ->
                response.setBody(generateMetaDataXML(coordinates[0]))
            }
        )

    private fun generateMetaDataXML(
        coordinate: Coordinate,
        pastVersions: List<String> = listOf()
    ): String {

        return """
                |<metadata>
                |    <groupId>${coordinate.group}</groupId>
                |    <artifactId>${coordinate.artifactIdWithVariant}</artifactId>
                |    <versioning>
                |    <latest>${coordinate.versionWithVariant}</latest>
                |    <release>${coordinate.versionWithVariant}</release>
                |    <versions>
                |       ${pastVersions.fold("") { c, v -> "$c<version>$v</version>\n"} }
                |       <version>0.1</version>
                |    </versions>
                |    <lastUpdated>20200513071913</lastUpdated>
                |   </versioning>
                |</metadata>
            """.trimMargin()
    }
}
