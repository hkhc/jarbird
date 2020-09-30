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
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.withNullability

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

    // WHEN l1 is overlaying null value (l2) and is overlaying non-null value (l3)
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
): String {

    `check null cannot overlay non-null`(cstr, mutableProp, testValue)
    `check non-null will overlay anything`(cstr, mutableProp, testValue)

    return mutableProp.name
}

fun <T : Overlayable, Field, ElementField> `Array Fields merged properly when overlaying`(
    cstr: () -> T,
    mutableProp: KMutableProperty<Field>,
    v1: List<ElementField>,
    v2: List<ElementField>
): String {

    `ArrayFields merged properly when overlaying without overlap`(cstr, mutableProp, v1, v2)
    `ArrayFields merged properly when overlaying with overlap`(cstr, mutableProp, v1 + v2, v2)

    return mutableProp.name
}

fun <T : Overlayable, Field, ElementField> `ArrayFields merged properly when overlaying without overlap`(
    cstr: () -> T,
    mutableProp: KMutableProperty<Field>,
    v1: List<ElementField>,
    v2: List<ElementField>
) {

    val p1 = cstr()
    val p2 = cstr()
    mutableProp.setter.call(p1, v1.toMutableList())
    mutableProp.setter.call(p2, v2.toMutableList())

    p1.overlayTo(p2)

    mutableProp.getter.call(p2) shouldBe v2 + v1
}

fun <T : Overlayable, Field, ElementField> `ArrayFields merged properly when overlaying with overlap`(
    cstr: () -> T,
    mutableProp: KMutableProperty<Field>,
    v1: List<ElementField>,
    v2: List<ElementField>
) {

    val p1 = cstr()
    val p2 = cstr()
    mutableProp.setter.call(p1, v1.toMutableList())
    mutableProp.setter.call(p2, v2.toMutableList())

    p1.overlayTo(p2)

    mutableProp.getter.call(p2) shouldBe v2 + v1.filter { !v2.contains(it) }
}

fun isClassOrNullableType(prop: KProperty1<*, *>, cls: KClass<out Any>): Boolean {
    val propType = prop.returnType
    val matchingType = cls.createType()
    return propType == matchingType || propType == matchingType.withNullability(true)
}

fun <T : Overlayable> `Fields overlay properly`(
    cls: KClass<T>,
    cstr: () -> T,
    nonStringFields: Array<String> = arrayOf()
) {

    @Suppress("UNCHECKED_CAST")
    cls.memberProperties
        .filter { !nonStringFields.contains(it.name) }
        .forEach {
            try {
                if (isClassOrNullableType(it, String::class))
                    `Field perform overlay properly`(cstr, it as KMutableProperty1<T, String>, "value")
                else if (isClassOrNullableType(it, Int::class))
                    `Field perform overlay properly`(cstr, it as KMutableProperty1<T, Int>, 123)
                else {
                    throw Exception("No test handler for field ${cls.qualifiedName} ${it.name} ${it.returnType}")
                }
            } catch (t: Throwable) {
                throw Exception("Field ${it.name} has error", t)
            }
        }
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
