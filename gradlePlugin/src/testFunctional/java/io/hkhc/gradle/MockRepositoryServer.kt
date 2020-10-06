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

package io.hkhc.gradle

import groovy.util.GroovyTestCase.assertEquals
import io.kotest.assertions.fail
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

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

        server.dispatcher = object: Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                System.out.println("dispatch ${request.path}")
                if (request.path!!.startsWith(baseUrl)) {
                    if (request.method=="PUT") {
                        return MockResponse().setResponseCode(200)
                    }
                    else if (request.method=="GET" && request.path!!.endsWith("maven-metadata.xml")) {
                        return MockResponse().setBody(
                            generateMetaDataXML(
                                group,
                                artifactId,
                                version
                            )
                        )
                    }
                    else {
                        return MockResponse().setResponseCode(505)
                    }
                }
                else {
                    return MockResponse().setResponseCode(505)
                }
            }
        }
        server.start()

    }

    fun teardown() {
        server.shutdown()
    }

    private fun generateFileLists(
        group: String,
        artifactId: String,
        version: String,
        hashes: List<String> = listOf("md5", "sha1", "sha256", "sha512"),
        suffixes: List<String> = listOf("-javadoc.jar", "-sources.jar", ".jar", ".module", ".pom")
    ): MutableList<String> {
        return mutableListOf<String>().apply {
            suffixes.forEach { suffix ->
                val basePath = "${baseUrl}/${group.replace('.', '/')}/"+
                    "${artifactId}/${version}/${artifactId}-${version}${suffix}"
                generateFileList(basePath, hashes)
                generateFileList("$basePath.asc",hashes)
            }
            generateFileList("${baseUrl}/${group.replace('.', '/')}/"+
                "${artifactId}/maven-metadata.xml", hashes)
        }
    }

    private fun MutableList<String>.generateFileList(
        path: String,
        hashes: List<String>
    ) {
        add(path)
        hashes.forEach { hash ->
            add("${path}.${hash}")
        }
    }

    private fun generateMetaDataXML(
        group: String,
        artifactId: String,
        version: String,
        pastVersions: List<String> = listOf()): String {

        return """
            |<metadata>
            |    <groupId>${group}</groupId>
            |    <artifactId>${artifactId}</artifactId>
            |    <versioning>
            |    <latest>${version}</latest>
            |    <release>${version}</release>
            |    <versions>
            |       ${pastVersions.fold("") { c, v -> c+"<version>${v}</version>\n"} }
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
            for(i in 0 until count) {
                add(server.takeRequest())
            }
        }
    }

    fun getServerUrl(): String {
        return server.url(baseUrl).toString()
    }

    private fun assertFile(requests: List<RecordedRequest>, path: String) {
        var matched = false
        requests
            .filter { it.method=="PUT" }
            .forEach {
                if (Regex(path).matches(it.path!!)) {
                    matched = true
                    return@forEach
                }
            }
        if (!matched) {
            fail("${path} does not match any recorded request")
        }
    }

    private fun assertReleaseFiles(requests: List<RecordedRequest>, baseUrl: String, suffix: String, additionalSuffix: String = ""): List<String> {
        val packageBase = "${baseUrl}/${group.replace('.','/')}/${artifactId}/${version}"
        val filenamePrefix = "${artifactId}-${version}"
        val tail = suffix

        return assertFiles(requests,"${packageBase}/${filenamePrefix}${additionalSuffix}${tail}" )
    }

    private fun assertFiles(requests: List<RecordedRequest>, url: String): List<String> {
        assertFile(requests, url)
        return listOf("", ".md5", ".sha1", ".sha256", ".sha512")
            .map { "${url}${it}" }
            .fold(mutableListOf()) { assertedFiles, path ->
                assertedFiles.apply {
                    assertFile(requests, path)
                    add(path)
                }
            }
    }

    private fun assertSnapshotFiles(requests: List<RecordedRequest>, baseUrl: String, suffix: String): List<String> {
        val packageBase = "${baseUrl}/${group.replace('.','/')}/${artifactId}/${version}"
        val versionNumber = version.substring(0, version.indexOf("-SNAPSHOT"))
        val filenamePrefix = "${artifactId}-${versionNumber}-[0-9]+.[0-9]+-[0-9]+"

        return assertFiles(requests,"${packageBase}/${filenamePrefix}${suffix}" )
    }

    fun assertReleaseArtifacts() {

        val recordedRequests = collectRequests(server)

        val assertedPaths = listOf(".jar", "-javadoc.jar", "-sources.jar", ".module", ".pom")
            .fold(mutableListOf<String>()) { assertedFiles, suffix ->
                assertedFiles.apply {
                    addAll(assertReleaseFiles(recordedRequests, baseUrl, suffix))
                    addAll(assertReleaseFiles(recordedRequests, baseUrl, "${suffix}.asc"))
                }
            }
            .apply {
                val packageBase = "${baseUrl}/${group.replace('.','/')}/${artifactId}"
                addAll(assertFiles(recordedRequests, "${packageBase}/maven-metadata.xml"))
            }

        val remainingPaths = recordedRequests
            .filter { it.method=="PUT" && assertedPaths.none { p -> Regex(p).matches(it.path!!) } }

        assertEquals(0, remainingPaths.size)

    }

    fun assertSnapshotArtifacts() {

        val recordedRequests = collectRequests(server)

        val assertedPaths = listOf(".jar", "-javadoc.jar", "-sources.jar", ".module", ".pom")
            .fold(mutableListOf<String>()) { assertedFiles, suffix ->
                assertedFiles.apply {
                    addAll(assertSnapshotFiles(recordedRequests, baseUrl, suffix ))
                    addAll(assertSnapshotFiles(recordedRequests, baseUrl, "${suffix}.asc"))
                }
            }
            .apply {
                val packageBase = "${baseUrl}/${group.replace('.','/')}/${artifactId}"
                addAll(assertFiles(recordedRequests, "${packageBase}/maven-metadata.xml"))
                addAll(assertFiles(recordedRequests, "${packageBase}/${version}/maven-metadata.xml"))
            }

        val remainingPaths = recordedRequests
            .filter { it.method=="PUT" && assertedPaths.none { p -> Regex(p).matches(it.path!!) } }

        assertEquals(0, remainingPaths.size)

    }


}
