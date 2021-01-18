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

public fun <T> Collection<T>.joinToStringAnd(transform: ((T) -> CharSequence)? = null): String {

    val list = toList()

    return if (list.size == 0) {
        ""
    } else if (list.size == 1) {
        (transform?.invoke(list[0]) ?: list[0]).toString()
    } else if (list.size == 2) {
        joinToString(separator = " and ", transform = transform)
    } else {
        list.subList(0, size - 1).joinToString(separator = ", ", transform = transform) +
            " and " +
            (transform?.invoke(list[size - 1]) ?: list[size - 1])
    }
}
