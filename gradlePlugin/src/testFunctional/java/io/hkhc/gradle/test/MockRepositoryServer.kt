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

import groovy.util.GroovyTestCase.assertEquals
import io.kotest.assertions.fail
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

const val HTTP_SUCCESS = 200
const val HTTP_FILE_NOT_FOUND = 404
const val HTTP_SERVICE_NOT_AVAILABLE = 503

@Suppress("TooManyFunctions")
class MockRepositoryServer {

    private lateinit var server: MockWebServer
    private var baseUrl = "/release"
    private lateinit var group: String
    private lateinit var artifactId: String
    private lateinit var version: String

    fun setUp(group: String, artifactId: String, version: String, baseUrl: String) {

        this.group = group
        this.artifactId = artifactId
        this.version = version

        this.baseUrl = baseUrl
        server = MockWebServer()

        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return request.path?.let { path ->
                    if (path.startsWith(baseUrl)) {
                        if (request.method == "PUT") {
                            MockResponse().setResponseCode(HTTP_SUCCESS)
                        } else if (request.method == "GET" && path.endsWith("maven-metadata.xml")) {
                            MockResponse().setBody(
                                generateMetaDataXML(
                                    group,
                                    artifactId,
                                    version
                                )
                            )
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
        group: String,
        artifactId: String,
        version: String,
        pastVersions: List<String> = listOf()
    ): String {

        return """
            |<metadata>
            |    <groupId>$group</groupId>
            |    <artifactId>$artifactId</artifactId>
            |    <versioning>
            |    <latest>$version</latest>
            |    <release>$version</release>
            |    <versions>
            |       ${pastVersions.fold("") { c, v -> c + "<version>$v</version>\n"} }
            |       <version>0.1</version>
            |    </versions>
            |    <lastUpdated>20200513071913</lastUpdated>
            |   </versioning>
            |</metadata>
        """.trimMargin()
    }

    private fun collectRequests(server: MockWebServer): List<RecordedRequest> {
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

    private fun assertFile(requests: List<RecordedRequest>, pathRegex: Regex) {
        var matched = requests
            .filter { it.method == "PUT" }
            .any { it.path?.let { path -> pathRegex.matches(path) } ?: false }
        if (!matched) {
            fail("$pathRegex does not match any recorded request")
        }
    }

    class RepoPatterns(
        val baseUrl: String,
        val group: String,
        val artifactId: String,
        val version: String,
        val pluginId: String? = null
    ) {

        val isSnapshot = version.endsWith("-SNAPSHOT")
        val METADATA_FILE = "maven-metadata.xml"

        fun metafile(base: String): List<String> {
            return mutableListOf<String>().apply {
                add("$base/$METADATA_FILE")
                if (isSnapshot) add("$base/$version/$METADATA_FILE")
            }
        }

        fun listPluginRepo(pluginId: String?, versionTransformer: (String) -> String) =
            pluginId?.let {
                listOf("$baseUrl/${pluginId.replace('.', '/')}/$pluginId.gradle.plugin")
                    .flatMap {
                        metafile(it) +
                            listOf("$it/$version/$pluginId.gradle.plugin-${versionTransformer(version)}.pom")
                    }
                    .flatMap(::hashedPaths)
            } ?: listOf()

        fun hashedPaths(path: String) =
            listOf("", ".md5", ".sha1", ".sha256", ".sha512")
                .map { hash -> "$path$hash" }

        fun artifactTypes(path: String) =
            listOf(".jar", "-javadoc.jar", "-sources.jar", ".module", ".pom")
                .map { suffix -> "$path$suffix" }

        fun list(versionTransformer: (String) -> String) = (
            listPluginRepo(pluginId, versionTransformer) +
                listOf("$baseUrl/${group.replace('.', '/')}/$artifactId")
                    .flatMap {
                        metafile(it) +
                            listOf("$it/$version/$artifactId-${versionTransformer(version)}")
                                .flatMap(::artifactTypes)
                                .flatMap { if (isSnapshot) listOf(it) else listOf(it, "$it.asc") }
                    }
                    .flatMap(::hashedPaths)
            )
            .map { Regex(it) }
    }

    fun transformReleaseVersion(version: String) = version

    fun transformSnapshotVersion(version: String): String {
        val snapshotVersion = version.indexOf("-SNAPSHOT")
            .let { if (it == -1) version else version.substring(0, it) }
        return "$snapshotVersion-[0-9]+.[0-9]+-[0-9]+"
    }

    fun assertArtifacts(baseUrl: String, versionTransformer: (String) -> String, pluginId: String? = null) {

        val recordedRequests = collectRequests(server)

        val expectedPaths = RepoPatterns(baseUrl, group, artifactId, version, pluginId).let {
            it.list(versionTransformer)
        }.apply {
            forEach { assertFile(recordedRequests, it) }
        }

        val remainingPaths = recordedRequests
            .filter {
                it.method == "PUT" &&
                    expectedPaths.none { regex -> it.path?.let { path -> regex.matches(path) } ?: false }
            }

        assertEquals(
            "all request to repository server are expected",
            "",
            remainingPaths.map { it.path }.joinToString("\n")
        )
    }
}
