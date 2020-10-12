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

const val HTTP_SUCCESS = 200
const val HTTP_FILE_NOT_FOUND = 404
const val HTTP_SERVICE_NOT_AVAILABLE = 503

@Suppress("TooManyFunctions")
class MockMavenRepositoryServer {

    private lateinit var server: MockWebServer
    private var baseUrl = "/release"
    private lateinit var coordinate: Coordinate

    fun setUp(coordinate: Coordinate, baseUrl: String) {

        this.coordinate = coordinate

        this.baseUrl = baseUrl
        server = MockWebServer()

        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                System.out.println("mock server request : ${request.method} ${request.path}")
//                request.headers.forEach {
//                    System.out.println("headers : ${it.first} = ${it.second}")
//                }
//                if (request.method=="POST") {
//                    System.out.println("mock server request body : ${request.body.readUtf8LineStrict()}")
//                }
                return request.path?.let { path ->
                    if (path.startsWith(baseUrl)) {
                        if (request.method == "PUT") {
                            MockResponse().setResponseCode(HTTP_SUCCESS)
                        } else if (request.method == "GET" && path.endsWith("maven-metadata.xml")) {
                            MockResponse().setBody(generateMetaDataXML(coordinate))
                        } else {
                            MockResponse().setResponseCode(HTTP_FILE_NOT_FOUND)
                        }
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
        return mutableListOf<RecordedRequest>().apply {
            for (i in 0 until count) {
                add(server.takeRequest())
            }
        }
    }

    fun getServerUrl(): String {
        return server.url(baseUrl).toString()
    }
}
