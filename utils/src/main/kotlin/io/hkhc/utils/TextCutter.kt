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

/**
 * Given a multi-line String, extract one part of it by specifying the starting line and end line
 * @param src the input string, supposingly multi-line string
 */
class TextCutter(val src: String) {
    /**
     * Given a multi-line String, extract one part of it by specifying the starting line and end line
     * @param startLine the first line to be extracted
     * @param endLine the last line to be extracted
     * @return the multi-line string extracted
     */
    fun cut(startLine: String, endLine: String): String {
        val result = StringBuilder()
        var started = false
        src.lines().forEach {
            if (!started) {
                if (it == startLine) {
                    started = true
                    result.append(it).append("\n")
                }
            } else {
                if (it == endLine) {
                    return@cut result.toString()
                } else {
                    result.append(it).append("\n")
                }
            }
        }
        return result.toString()
    }
}
