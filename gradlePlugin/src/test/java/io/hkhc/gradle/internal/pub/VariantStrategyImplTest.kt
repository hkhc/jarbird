/*
 * Copyright (c) 2021. Herman Cheung
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

package io.hkhc.gradle.internal.pub

import io.hkhc.gradle.pom.Pom
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class VariantStrategyImplTest : FunSpec({

    test("default value") {
        PubVariantStrategyImpl().apply {
            pom = Pom(
                group = "mygroup",
                artifactId = "mylib",
                version = "1.0"
            )
            variantArtifactId() shouldBe "mylib"
            variantVersion() shouldBe "1.0"
        }
    }

    test("empty variant bind to artifactId") {
        PubVariantStrategyImpl().apply {
            pom = Pom(
                group = "mygroup",
                artifactId = "mylib",
                version = "1.0"
            )
            variantWithArtifactId()
            variantArtifactId() shouldBe "mylib"
            variantVersion() shouldBe "1.0"
        }
    }

    test("default value with variant") {
        PubVariantStrategyImpl("variant").apply {
            pom = Pom(
                group = "mygroup",
                artifactId = "mylib",
                version = "1.0"
            )
            variantArtifactId() shouldBe "mylib"
            variantVersion() shouldBe "1.0-variant"
        }
    }

    test("default value with variant and bind to version") {
        PubVariantStrategyImpl("variant").apply {
            pom = Pom(
                group = "mygroup",
                artifactId = "mylib",
                version = "1.0"
            )
            variantWithVersion()
            variantArtifactId() shouldBe "mylib"
            variantVersion() shouldBe "1.0-variant"
        }
    }

    test("default value with variant and bind to artifactId") {
        PubVariantStrategyImpl("variant").apply {
            pom = Pom(
                group = "mygroup",
                artifactId = "mylib",
                version = "1.0"
            )
            variantWithArtifactId()
            variantArtifactId() shouldBe "mylib-variant"
            variantVersion() shouldBe "1.0"
        }
    }

    test("default value with variant and not bind to anything") {
        PubVariantStrategyImpl("variant").apply {
            pom = Pom(
                group = "mygroup",
                artifactId = "mylib",
                version = "1.0"
            )
            variantInvisible()
            variantArtifactId() shouldBe "mylib"
            variantVersion() shouldBe "1.0"
        }
    }
})
