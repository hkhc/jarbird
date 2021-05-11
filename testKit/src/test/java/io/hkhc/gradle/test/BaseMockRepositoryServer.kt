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
    private lateinit var coordinates: List<Coordinate>
    private lateinit var matcher: List<RequestMatcher>

    abstract fun setupMatcher(coordinates: List<Coordinate>): List<RequestMatcher>

    private fun pathMatcher(request: RecordedRequest): MockResponse {
        return matcher.find { it.matches(request) }?.
            responseHandler?.invoke(request, MockResponse()) ?: FileNotFound.invoke(
            request,
            MockResponse()
        )
    }

    fun setUp(coordinates: List<Coordinate>, baseUrl: String) {

        this.coordinates = coordinates

        matcher = setupMatcher(coordinates)

        this.baseUrl = baseUrl
        server = MockWebServer()

        // TODO enable HTTPS for mockwebserver

//        val localhost = InetAddress.getByName("localhost").canonicalHostName
//        val localhostCertificate: HeldCertificate = HeldCertificate.Builder()
//            .addSubjectAlternativeName(localhost)
//            .build()
//        val rootCertificate: HeldCertificate = HeldCertificate.Builder()
//            .certificateAuthority(1)
//            .build()
//        val serverCertificate: HeldCertificate = HeldCertificate.Builder()
//            .addSubjectAlternativeName(localhost)
//            .signedBy(rootCertificate)
//            .build()
//        val serverCertificates = HandshakeCertificates.Builder()
//            .heldCertificate(serverCertificate, rootCertificate.certificate)
//            .build();
//        server.useHttps(serverCertificates.sslSocketFactory(), false);

        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return pathMatcher(request).apply {
                    println("mock server request : ${request.method} ${request.path} result ${this.status}")
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
        return List(count) { server.takeRequest() }.also {
            it.forEach {
                println("recorded request = ${it.path}")
            }
        }
    }

    fun getServerUrl(): String {
        return server.url(baseUrl).toString()
    }
}
