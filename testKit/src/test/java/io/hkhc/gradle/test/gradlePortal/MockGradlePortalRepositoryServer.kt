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

import com.google.gson.Gson
import java.nio.charset.Charset

@Suppress("UnsafeCallOnNullableType")
class MockGradlePortalRepositoryServer : BaseMockRepositoryServer() {

    // TODO fix multi coordinates matcher
    @Suppress("UNCHECKED_CAST")
    override fun setupMatcher(coordinates: List<Coordinate>, baseUrl: String) = coordinates.flatMap { coordinate ->
        val first = coordinates[0]
        with(coordinate) {
            listOf(
                PostMatcher("/api/v1/publish/versions/new/${coordinate.pluginId!!}") { recordedRequest, response ->
                    val requestBody = recordedRequest.body.readString(Charset.defaultCharset())
//                    println("requestBody new ${requestBody}")
                    val requestBodyTree = Gson().fromJson(requestBody, Map::class.java)
                    val requestUrl = requireNotNull(
                        recordedRequest.requestUrl?.let { url -> "${url.scheme}://${url.host}:${url.port}" }
                    ) {
                        "requestUrl is null"
                    }
                    val artifacts = (requestBodyTree["artifacts"] as List<Map<String, String>>)
                        .joinToString(separator = ",") {
                            "\"${it["hash"]}\": \"$requestUrl$baseUrl/upload/${it["hash"]}/\""
                        }

                    response.setBody(
                        """
                            {
                                pluginId: "test.plugin",
                                version: "0.1",
                                publishTo: { $artifacts },
                                warningMessage: "",
                                warning: false,
                                failed: false,
                                errorMessage: ""
                            }
                        """.trimIndent()
                    ).setResponseCode(HTTP_SUCCESS)
                },
                PostMatcher(
                    "/api/v1/publish/versions/activate/${coordinate.pluginId}/${coordinate.version}"
                ) { recordedRequest, response ->
                    val requestBody = recordedRequest.body.readString(Charset.defaultCharset())
//                    println("requestBody activate ${requestBody}")
                    response.setResponseCode(HTTP_SUCCESS)
                },
                PutMatcher("/base/upload/") { _, response ->
                    response.setResponseCode(HTTP_SUCCESS)
                }
            )
        }
    }

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
