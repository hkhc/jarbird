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

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.nio.charset.Charset

@Suppress("TooManyFunctions")
class MockArtifactoryRepositoryServer {

    private lateinit var server: MockWebServer
    private var baseUrl = "/release"
    private lateinit var coordinate: Coordinate

    fun setUp(coordinate: Coordinate, baseUrl: String) {

        this.coordinate = coordinate

        this.baseUrl = baseUrl
        server = MockWebServer()

        val username = "username"
        val repo = "maven"

        server.dispatcher = object : Dispatcher() {
            var fileCount = 0
            override fun dispatch(request: RecordedRequest): MockResponse {
                System.out.println("AF mock server request : ${request.method} ${request.path}")
                request.headers.forEach {
                    System.out.println("AF headers : ${it.first} = ${it.second}")
                }
                if (request.method == "POST") {
                    System.out.println("AF mock server request body : ${request.body.readString(Charset.defaultCharset())}")
                }
                return request.path?.let { path ->
                    if (request.method == "PUT" &&
                        path.startsWith(
                            "/base/oss-snapshot-local/${coordinate.group.replace('.','/')}/" +
                                "${coordinate.artifactId}/${coordinate.version}"
                        )
                    ) {
                        System.out.println("publish file")
                        MockResponse().setResponseCode(HTTP_SUCCESS)
                    } else if (request.method == "GET" && path == "/base/api/system/version") {
                        MockResponse().setBody(
                            """
                            {
                              "version" : "6.18.1",
                              "revision" : "61801900",
                              "addons" : [],
                              "license" : "1e7f3be7c2a8bbfaf1c4e7c41779d89664a440e93"
                            }
                        """.trimIndent()
                        )
                    } else if (request.method == "PUT" && path == "/base/api/build") {
                        MockResponse().setResponseCode(HTTP_SUCCESS)
                    } else {
                        MockResponse().setResponseCode(HTTP_FILE_NOT_FOUND)
                    }
                } ?: MockResponse().setResponseCode(HTTP_SERVICE_NOT_AVAILABLE)
            }
        }
        server.start()
    }

    fun teardown() {
        server.shutdown()
    }

    private fun generateMetaDataXML(
        coordinate: Coordinate,
        pastVersions: List<String> = listOf()
    ): String {

        return """
            |<metadata>
            |    <groupId>${coordinate.group}</groupId>
            |    <artifactId>${coordinate.artifactId}</artifactId>
            |    <versioning>
            |    <latest>${coordinate.version}</latest>
            |    <release>${coordinate.version}</release>
            |    <versions>
            |       ${pastVersions.fold("") { c, v -> c + "<version>$v</version>\n"} }
            |       <version>0.1</version>
            |    </versions>
            |    <lastUpdated>20200513071913</lastUpdated>
            |   </versioning>
            |</metadata>
        """.trimMargin()
    }

    fun collectRequests(): List<RecordedRequest> {
        val count = server.requestCount
        System.out.println("$count recorded requests")
        return mutableListOf<RecordedRequest>()
            .apply {
                for (i in 0 until count) {
                    add(server.takeRequest())
                }
            }
            .filter { it.path?.let { path -> !path.startsWith("/base/api") } ?: false }
    }

    fun getServerUrl(): String {
        return server.url(baseUrl).toString()
    }
}
