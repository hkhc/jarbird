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

import io.hkhc.gradle.endpoint.SimpleRepoEndpoint
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class RepoSpecTest : FunSpec({

    fun createTestEndpoint() = SimpleRepoEndpoint(
        "id",
        "releaseUrl",
        "snapshotUrl",
        "username",
        "password",
        "apikey",
        "description"
    )

    test("Repo equality") {

        // To objects with identical data

        val spec1 = createTestEndpoint()
        val spec2 = createTestEndpoint()
        spec1 shouldBe spec2
    }

    test("Repo inequality") {

        // To objects with different data

        val spec1 = createTestEndpoint()
        val spec2 = SimpleRepoEndpoint(
            "id",
            "releaseUrl",
            "snapshotUrl",
            "username",
            "password",
            "apikey",
            "description different"
        )

        spec1 shouldNotBe spec2
    }

    test("Set compatability") {

        // To objects with identical data

        val spec1 = createTestEndpoint()
        val spec2 = createTestEndpoint()
        val set = mutableSetOf(spec1, spec2)

        set shouldHaveSize 1
    }

    test("Set merging") {

        // To objects with identical data

        val spec1 = createTestEndpoint()
        val spec2 = createTestEndpoint()
        val set = mutableSetOf(spec1) + spec2

        set shouldHaveSize 1
    }
})
