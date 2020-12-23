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

package io.hkhc.gradle.internal.utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.Collections

class VersionTest : FunSpec({

    test("version comparison") {
        Version("1.1").compareTo(Version("1.1.1")) shouldBe -1
        (Version("1.1") == Version("1.1.1")) shouldBe false
        (Version("1.1") < Version("1.1.1")) shouldBe true

        Version("2.0").compareTo(Version("1.9.9")) shouldBe 1
        (Version("2.0") == Version("1.9.9")) shouldBe false
        (Version("2.0") > Version("1.9.9")) shouldBe true

        Version("1.0").compareTo(Version("1")) shouldBe 0
        (Version("1.0") == Version("1")) shouldBe true

        Version("1.1").compareTo(Version("1.1.1")) shouldBe -1
        (Version("1.1") == Version("1.1.1")) shouldBe false
        (Version("1.1") < Version("1.1.1")) shouldBe true

        Version("1.1-ABC").compareTo(Version("1.1")) shouldBe 1
        (Version("1.1-ABC") == Version("1.1")) shouldBe false
        (Version("1.1-ABC") > Version("1.1")) shouldBe true

        Version("1.1-ABC-D").compareTo(Version("1.1")) shouldBe 1
        (Version("1.1-ABC-D") == Version("1.1")) shouldBe false
        (Version("1.1-ABC-D") > Version("1.1")) shouldBe true
    }

    test("version in collection") {

        val versions = listOf<Version>(
            Version("2"),
            Version("1.0.5"),
            Version("1.01.0"),
            Version("1.00.1")
        )
        Collections.min(versions).get() shouldBe "1.00.1" // return min version
        Collections.max(versions).get() shouldBe "2" // return max version
    }
})
