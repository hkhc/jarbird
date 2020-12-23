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

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class NormalizePubNameTest : FunSpec({

    test("empty string") {
        JarbirdPlugin.normalizePubName("") shouldBe ""
    }

    test("single word with lowercase first char") {
        JarbirdPlugin.normalizePubName("hello") shouldBe "hello"
    }

    test("single word with uppercase first char") {
        JarbirdPlugin.normalizePubName("Hello") shouldBe "hello"
    }

    test("two consecutive words with lowercase first char") {
        JarbirdPlugin.normalizePubName("helloWorld") shouldBe "helloWorld"
    }

    test("two consecutive words with uppercase first char") {
        JarbirdPlugin.normalizePubName("HelloWorld") shouldBe "helloWorld"
    }

    test("single word with digit") {
        JarbirdPlugin.normalizePubName("he11o") shouldBe "he11o"
    }

    test("two words with hyphen in between") {
        JarbirdPlugin.normalizePubName("he11o-world") shouldBe "he11oWorld"
    }

    test("two words with hyphen at the beginning") {
        JarbirdPlugin.normalizePubName("-helloWorld") shouldBe "helloWorld"
    }

    test("two words with hyphen at the end") {
        JarbirdPlugin.normalizePubName("helloWorld-") shouldBe "helloWorld"
    }
})
