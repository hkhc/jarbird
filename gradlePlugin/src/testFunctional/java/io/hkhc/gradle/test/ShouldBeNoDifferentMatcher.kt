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

package io.hkhc.gradle.test

import com.github.difflib.DiffUtils
import io.hkhc.utils.DiffPresenter
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import java.io.StringWriter

class ListWithException<T> (val list: List<T>, val exception: List<T>)

infix fun <T, C : List<T>> C?.shouldBeNoDifference(expected: C) = this should noDifferent(expected)
infix fun <T, C : List<T>> C?.shouldBeNoDifference(expected: ListWithException<T>) =
    this should noDifferentWithException(expected)

fun <T> noDifferentWithException(expected: ListWithException<T>) = object : Matcher<List<T>?> {
    override fun test(actual: List<T>?): MatcherResult {

        val filteredExpected = expected.list.filter { !expected.exception.contains(it) }
        val filteredActual = actual?.filter { !expected.exception.contains(it) } ?: listOf()

        return actual?.let {
            val patch = DiffUtils.diff(
                filteredExpected,
                filteredActual
            )

            val failureMessageWriter = StringWriter()
            DiffPresenter<T>().print(filteredExpected, filteredActual, failureMessageWriter)

            return MatcherResult(
                patch.deltas.isEmpty(),
                { "Expected List and Actual List should not be different \n$failureMessageWriter" },
                { "Expected List and Actual List should be different \n$failureMessageWriter" }
            )
        } ?: MatcherResult(
            false,
            "null",
            "not null"
        )
    }
}

fun <T> noDifferent(excepted: List<T>) = noDifferentWithException(
    ListWithException(excepted, listOf())
)

infix fun <T> List<T>.except(exception: List<T>) = ListWithException(this, exception)
