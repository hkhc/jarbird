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
import com.github.difflib.patch.AbstractDelta
import com.github.difflib.patch.DeltaType
import java.io.PrintWriter
import java.io.Writer
import kotlin.math.ceil
import kotlin.math.log10

class DiffPresenter<T> {

    class LineNumber<T>(private val data: List<T>) {
        private val numberSize = calcNumberSize(data.size)
        private val lineNumberFormat = "%${numberSize}d"
        private var _lastNumber = 0
        val lastNumber: Int
            get() = _lastNumber
        private val formattedNumber: String
            get() = lineNumberFormat.format(_lastNumber + 1)

        /**
         * log10 1 -> 0
         * log10 10 -> 1
         * log10 100 -> 2 ...
         * so theoretically we need log10(value)+1 to get data size
         * To avoid the possibility of floating rounding error, we do ceil(v+0.1) instead of v+1 to avoid the
         * log10(value.toFloat()) give value slightly less than expected and get one less size than we expected.
         * The 0.1 magic number is arbitrary and anything between 0 to 1 and not too close to 0 or 1 should work.
         *
         * @param maxValue the maximum value to calculate the digits needed
         * @return the max number of digit
         */
        @Suppress("MagicNumber")
        private fun calcNumberSize(maxValue: Int): Int =
            ceil(log10(maxValue.toFloat()) + 0.1).toInt()

        operator fun inc(): LineNumber<T> {
            _lastNumber++
            return this
        }
        override fun toString(): String {
            return formattedNumber
        }
        fun space(count: Int, c: Char = ' '): String {
            var result = ""
            for (i in 0..count) result += c
            return result
        }
        fun getData(): T {
            return data[lastNumber]
        }
        fun getNumberPlaceHolder(): String {
            return space(numberSize)
        }
    }

    private class Printer<T>(private val source: List<T>, target: List<T>, writer: Writer) {

        var sourceLineNumber = LineNumber(source)
        var targetLineNumber = LineNumber(target)
        val printWriter = PrintWriter(writer)

        val patch = DiffUtils.diff(
            source,
            target,
            com.github.difflib.algorithm.myers.MyersDiff()
        )

        private fun <T> insert(delta: AbstractDelta<T>) {
            while (targetLineNumber.lastNumber < delta.target.position) {
                printWriter.println("$sourceLineNumber   $targetLineNumber    ${targetLineNumber.getData()}")
                sourceLineNumber++
                targetLineNumber++
            }
            delta.target.lines.forEach { line ->
                printWriter.println("${sourceLineNumber.getNumberPlaceHolder()}  $targetLineNumber  + $line")
                targetLineNumber++
            }
        }

        private fun <T> delete(delta: AbstractDelta<T>) {
            while (sourceLineNumber.lastNumber < delta.source.position) {
                printWriter.println("$sourceLineNumber   $targetLineNumber    ${sourceLineNumber.getData()}")
                sourceLineNumber++
                targetLineNumber++
            }
            delta.source.lines.forEach { line ->
                printWriter.println("$sourceLineNumber  ${targetLineNumber.getNumberPlaceHolder()}  - $line")
                sourceLineNumber++
            }
        }

        private fun <T> change(delta: AbstractDelta<T>) {
            while (targetLineNumber.lastNumber < delta.target.position) {
                printWriter.println("$sourceLineNumber   $targetLineNumber    ${targetLineNumber.getData()}")
                sourceLineNumber++
                targetLineNumber++
            }
            delta.source.lines.forEach { line ->
                printWriter.println("$sourceLineNumber  ${targetLineNumber.getNumberPlaceHolder()}  - $line")
                sourceLineNumber++
            }
            delta.target.lines.forEach { line ->
                printWriter.println("${sourceLineNumber.getNumberPlaceHolder()}  $targetLineNumber  + $line")
                targetLineNumber++
            }
        }

        fun print() {

            with(printWriter) {
                patch.deltas.forEach {
                    when (it.type) {
                        DeltaType.INSERT -> insert(it)
                        DeltaType.DELETE -> delete(it)
                        DeltaType.CHANGE -> change(it)
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

    fun print(source: List<T>, target: List<T>, writer: Writer) {
        Printer(source, target, writer).print()
    }
}
