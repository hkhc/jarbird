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

package io.hkhc.gradle.internal.utils

import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.safeCast

fun Any.commonEquals(other: Any?): Boolean {

    return other?.let {

        if (this === it) return true

//        val me = clazz.safeCast(this) ?: return false
        val that = this::class.safeCast(other) ?: return false

        return this::class.memberProperties.all { prop ->
            prop.getter.call(this) == prop.getter.call(that)
        }
    } ?: false
}

fun Any.commonToString() = this::class.memberProperties.joinToString(
    ", ",
    "${this::class.java.simpleName}(",
    ")"
) { "${it.name}=${it.getter.call(this)}" }

@Suppress("MagicNumber")
fun Any.commonHashCode() = this::class.memberProperties.fold(0) { result, curr ->
    (31 * result + curr.getter.call(this).hashCode())
}
