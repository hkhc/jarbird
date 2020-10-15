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

const val HTTP_SUCCESS = 200
const val HTTP_FILE_NOT_FOUND = 404
const val HTTP_SERVICE_NOT_AVAILABLE = 503

open class RequestMatcher(
    val method: String,
    val path: String,
    val responseHandler: (RecordedRequest, MockResponse) -> MockResponse
) {
    open fun matches(request: RecordedRequest) = request.method == method && request.path == path
}

class HeadMatcher(
    path: String,
    responseHandler: (RecordedRequest, MockResponse) -> MockResponse
) : RequestMatcher("HEAD", path, responseHandler)

open class GetMatcher(
    path: String,
    responseHandler: (RecordedRequest, MockResponse) -> MockResponse
) : RequestMatcher("GET", path, responseHandler)

class PostMatcher(
    path: String,
    responseHandler: (RecordedRequest, MockResponse) -> MockResponse
) : RequestMatcher("POST", path, responseHandler)

class PutMatcher(
    path: String,
    responseHandler: (RecordedRequest, MockResponse) -> MockResponse
) : RequestMatcher("PUT", path, responseHandler) {
    override fun matches(request: RecordedRequest) =
        request.method == method &&
            (request.path?.startsWith(path) ?: false)
}

val FileNotFound = { _: RecordedRequest, response: MockResponse -> response.setResponseCode(HTTP_FILE_NOT_FOUND) }
val Success = { _: RecordedRequest, response: MockResponse -> response.setResponseCode(HTTP_SUCCESS) }
val ServiceNotAvailable = { _: RecordedRequest, response: MockResponse ->
    response.setResponseCode(HTTP_SERVICE_NOT_AVAILABLE)
}
