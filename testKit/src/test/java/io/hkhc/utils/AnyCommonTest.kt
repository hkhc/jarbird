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

package io.hkhc.utils

import io.hkhc.gradle.internal.utils.commonEquals
import io.hkhc.gradle.internal.utils.commonHashCode
import io.hkhc.gradle.internal.utils.commonToString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

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

class AnyCommonTest : FunSpec({

    open class Hello(var fruit: String = "Apple") {
        override fun equals(other: Any?) = commonEquals(other)
        override fun toString() = commonToString()
        override fun hashCode() = commonHashCode()
    }

    class World(var animal: String = "dog", fruit: String) : Hello(fruit) {
        override fun equals(other: Any?) = commonEquals(other)
        override fun hashCode() = commonHashCode()
    }

    test("toString() with one property") {
        Hello("orange").toString() shouldBe "Hello(fruit=orange)"
    }

    test("equals with one property") {
        (Hello("orange") == Hello("orange")) shouldBe true
        (Hello("orange") == Hello("pear")) shouldBe false
    }

    test("hashCose with one property") {
        Hello("orange").hashCode() shouldBe Hello("orange").hashCode()
        Hello("orange").hashCode() shouldNotBe Hello("apple").hashCode()
    }

    test("toString() with extended class") {
        World("dog", "apple").toString() shouldBe "World(animal=dog, fruit=apple)"
    }

    test("equals with extended class") {
        (World("cat", "orange") == World("cat", "orange")) shouldBe true
        (World("cat", "orange") == World("dog", "orange")) shouldBe false
        (World("cat", "orange") == World("cat", "banana")) shouldBe false
    }

    test("hashCode with extended class") {

        World("mice", "orange").hashCode() shouldBe World("mice", "orange").hashCode()
        World("mice", "orange").hashCode() shouldNotBe World("mice", "apple").hashCode()
        World("mice", "orange").hashCode() shouldNotBe World("elephant", "orange").hashCode()
    }
})
