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

abstract class BaseMockRepositoryServer {
    private lateinit var server: MockWebServer
    private var baseUrl = "/release"
    private lateinit var coordinate: Coordinate
    lateinit var matcher: List<RequestMatcher>

    abstract fun setupMatcher(coordinate: Coordinate): List<RequestMatcher>

    private fun pathMatcher(request: RecordedRequest): MockResponse {
        return matcher.find { it.matches(request) }?.let {
            it.responseHandler.invoke(request, MockResponse())
        } ?: FileNotFound.invoke(
            request,
            MockResponse()
        )
    }

    fun setUp(coordinate: Coordinate, baseUrl: String) {

        this.coordinate = coordinate

        matcher = setupMatcher(coordinate)

        this.baseUrl = baseUrl
        server = MockWebServer()

        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                with(request) {
                    println("mock server request : $method $path")
//                    headers.forEach {
//                        println("headers : ${it.first} = ${it.second}")
//                    }
//                    if (method == "POST") {
//                        println("mock server request body : ${body.readString(Charset.defaultCharset())}")
//                    }
                    return pathMatcher(request)
                }
            }
        }
        server.start()
    }

    fun teardown() {
        server.shutdown()
    }

    fun collectRequests(): List<RecordedRequest> {
        val count = server.requestCount
        println("$count recorded requests")
        return List(count) { server.takeRequest() }
    }

    fun getServerUrl(): String {
        return server.url(baseUrl).toString()
    }
}
