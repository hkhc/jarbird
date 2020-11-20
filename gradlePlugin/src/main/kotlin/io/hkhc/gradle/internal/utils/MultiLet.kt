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

fun <T1, T2, T3, R> multiLet(it1: T1?, it2: T2?, it3: T3?, block: (T1, T2, T3) -> R): R? {
    return if (it1 == null || it2 == null || it3 == null) {
        null
    } else {
        block(it1, it2, it3)
    }
}
