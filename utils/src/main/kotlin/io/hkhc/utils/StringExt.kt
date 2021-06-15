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

@Suppress("ControlFlowWithEmptyBody")
fun String.removeLineBreak(
    ensureSpaceWithMerge: Boolean = false
): String {

    val builder = StringBuffer()

    var currPos = 0
    var lastChar: Char = 0.toChar()
    while (currPos < length) {
        var endIndex = currPos
        while (endIndex < length && get(endIndex) != '\n') {
            lastChar = get(endIndex)
            endIndex++
        }
        if (endIndex == length) {
            builder.append(substring(currPos))
            currPos = endIndex
        } else {
            builder.append(substring(currPos, endIndex))
            if (ensureSpaceWithMerge && lastChar != ' ') {
                builder.append(' ')
            }
            currPos = endIndex + 1
        }
    }

    return builder.toString()
}
