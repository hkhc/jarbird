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

package io.hkhc.test.utils

import java.io.File

class FileTree {

    // Box drawing characters https://en.wikipedia.org/wiki/Box-drawing_character
    fun dump(file: File, block: (String) -> Unit) {
        if (file.isDirectory) {
            block.invoke(file.name)
            file.listFiles()?.let { list ->
                val lastIndex = list.size - 1
                list.forEachIndexed { idx, it ->
                    val headPrefix = if (idx == lastIndex)
                        "\u2570\u2500\u2500\u2500 " /* L--- */
                    else
                        "\u251C\u2500\u2500\u2500 " /* +--- */
                    val tailPrefix = if (idx == lastIndex)
                        "     "
                    else
                        "\u2502    " /* | */
                    dump(it) {
                        if (idx==0) {
                            block.invoke(headPrefix + it)
                        } else {
                            block.invoke(tailPrefix + it)
                        }
                    }
                }
            }
        } else {
            block.invoke("${file.name} (${file.length()})")
        }
    }
}