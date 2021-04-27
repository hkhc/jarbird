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

import com.github.difflib.DiffUtils
import com.github.difflib.algorithm.myers.MyersDiff
import com.github.difflib.patch.DeltaType
import java.io.PrintWriter
import java.io.Writer
import kotlin.math.ceil
import kotlin.math.log10

fun space(count: Int, c: Char = ' '): String {
    var result = ""
    for (i in 0..count) result += c
    return result
}

class DiffPresenter<T> {

    class LineNumber<T>(private val data: List<T>) {
        private val numberSize = ceil(log10(data.size.toFloat()) + 0.1).toInt()
        private val lineNumberFormat = "%${numberSize}d"
        private var _lastNumber = 0
        val lastNumber: Int
            get() = _lastNumber
        val formattedNumber: String
            get() = lineNumberFormat.format(_lastNumber + 1)
        operator fun inc(): LineNumber<T> {
            _lastNumber++
            return this
        }
        override fun toString(): String {
            return formattedNumber
        }
        fun getData(): T {
            return data[lastNumber]
        }
        fun getNumberPlaceHolder(): String {
            return space(numberSize)
        }
    }

    fun print(source: List<T>, target: List<T>, writer: Writer) {

        var sourceLineNumber = LineNumber(source)
        var targetLineNumber = LineNumber(target)
        val printWriter = PrintWriter(writer)

        val patch = DiffUtils.diff(
            source,
            target,
            MyersDiff()
        )

        with(printWriter) {
            patch.deltas.forEach {
                when (it.type) {
                    DeltaType.INSERT -> {
                        while (targetLineNumber.lastNumber < it.target.position) {
                            println("$sourceLineNumber   $targetLineNumber    ${targetLineNumber.getData()}")
                            sourceLineNumber++
                            targetLineNumber++
                        }
                        it.target.lines.forEach { line ->
                            println("${sourceLineNumber.getNumberPlaceHolder()}  $targetLineNumber  + $line")
                            targetLineNumber++
                        }
                    }
                    DeltaType.DELETE -> {
                        while (sourceLineNumber.lastNumber < it.source.position) {
                            println("$sourceLineNumber   $targetLineNumber    ${sourceLineNumber.getData()}")
                            sourceLineNumber++
                            targetLineNumber++
                        }
                        it.source.lines.forEach { line ->
                            println("$sourceLineNumber  ${targetLineNumber.getNumberPlaceHolder()}  - $line")
                            sourceLineNumber++
                        }
                    }
                    DeltaType.CHANGE -> {
                        while (targetLineNumber.lastNumber < it.target.position) {
                            println("$sourceLineNumber   $targetLineNumber    ${targetLineNumber.getData()}")
                            sourceLineNumber++
                            targetLineNumber++
                        }
                        it.source.lines.forEach { line ->
                            println("$sourceLineNumber  ${targetLineNumber.getNumberPlaceHolder()}  - $line")
                            sourceLineNumber++
                        }
                        it.target.lines.forEach { line ->
                            println("${sourceLineNumber.getNumberPlaceHolder()}  $targetLineNumber  + $line")
                            targetLineNumber++
                        }
                    }
                    else -> {
                        println("${it.type} : Not handle yet")
                    }
                }
            }
            while (sourceLineNumber.lastNumber < source.size) {
                println("$sourceLineNumber   $targetLineNumber    ${sourceLineNumber.getData()}")
                sourceLineNumber++
                targetLineNumber++
            }
            close()
        }
    }
}
