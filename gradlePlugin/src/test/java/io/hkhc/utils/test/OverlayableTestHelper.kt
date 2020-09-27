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

package io.hkhc.utils.test

import io.hkhc.gradle.pom.Overlayable
import io.kotest.matchers.shouldBe
import kotlin.reflect.KMutableProperty

fun <T : Overlayable, Field> `check null cannot overlay non-null`(
    cstr: () -> T,
    mutableProp: KMutableProperty<Field>,
    testValue: Field?
) {

    // GIVEN
    val l0 = cstr()
    val l1 = cstr()
    val l2 = cstr()
    mutableProp.setter.call(l2, testValue)

    // WHEN
    l1.overlayTo(l2)

    // THEN
    mutableProp.getter.call(l1) shouldBe mutableProp.getter.call(l0)
    mutableProp.getter.call(l2) shouldBe testValue
}

fun <T : Overlayable, Field> `check non-null will overlay anything`(
    cstr: () -> T,
    mutableProp: KMutableProperty<Field>,
    testValue: Field?
) {

    // GIVEN
    val l1 = cstr()
    val l2 = cstr()
    val l3 = cstr()
    mutableProp.setter.call(l1, testValue)
    mutableProp.setter.call(l3, testValue)

    // WHEN
    l1.overlayTo(l2)
    l1.overlayTo(l3)

    // THEN l2 and l3 are overlaid by l1 because l1.name!=null
    mutableProp.getter.call(l1) shouldBe testValue
    mutableProp.getter.call(l2) shouldBe testValue
    mutableProp.getter.call(l3) shouldBe testValue
}

fun <T : Overlayable, Field> `Field perform overlay properly`(
    cstr: () -> T,
    mutableProp: KMutableProperty<Field>,
    testValue: Field?
) {

    `check null cannot overlay non-null`(cstr, mutableProp, testValue)
    `check non-null will overlay anything`(cstr, mutableProp, testValue)
}

fun <T : Overlayable> `check -1 cannot overlay non -1`(
    cstr: () -> T,
    mutableProp: KMutableProperty<Int>
) {

    // GIVEN
    val l1 = cstr.invoke()
    val l2 = cstr.invoke()
    mutableProp.setter.call(l2, 2020)

    // WHEN
    l1.overlayTo(l2)

    // THEN
    mutableProp.getter.call(l1) shouldBe -1
    mutableProp.getter.call(l2) shouldBe 2020
}

fun <T : Overlayable> `check non -1 will overlay anything`(
    cstr: () -> T,
    mutableProp: KMutableProperty<Int>
) {

    // GIVEN
    val l1 = cstr.invoke()
    val l2 = cstr.invoke()
    val l3 = cstr.invoke()
    mutableProp.setter.call(l1, 2020)
    mutableProp.setter.call(l3, 2020)

    // WHEN
    l1.overlayTo(l2)
    l1.overlayTo(l3)

    // THEN l2 and l3 are overlaid by l1 because l1.name!=null
    mutableProp.getter.call(l1) shouldBe 2020
    mutableProp.getter.call(l2) shouldBe 2020
    mutableProp.getter.call(l3) shouldBe 2020
}

fun <T : Overlayable> `Int field is overlay properly`(
    cstr: () -> T,
    mutableProp: KMutableProperty<Int>
) {
    `check -1 cannot overlay non -1`(cstr, mutableProp)
    `check non -1 will overlay anything`(cstr, mutableProp)
}
