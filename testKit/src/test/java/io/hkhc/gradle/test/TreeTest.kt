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

package io.hkhc.gradle.test

import io.hkhc.utils.tree.Node
import io.hkhc.utils.tree.TreePrinter
import io.hkhc.utils.tree.toStringTree
import io.kotest.assertions.Actual
import io.kotest.assertions.Expected
import io.kotest.assertions.assertionCounter
import io.kotest.assertions.collectOrThrow
import io.kotest.assertions.eq.Eq
import io.kotest.assertions.eq.actualIsNull
import io.kotest.assertions.eq.expectedIsNull
import io.kotest.assertions.errorCollector
import io.kotest.assertions.failure
import io.kotest.assertions.show.Printed
import io.kotest.assertions.show.Show
import io.kotest.assertions.show.show
import io.kotest.matchers.Matcher
import io.kotest.matchers.should

class NodeEq<T> : Eq<Node<T>> {

    override fun equals(actual: Node<T>, expected: Node<T>): Throwable? {
        return when {
            actual === expected -> null
            actual == expected -> null
            else ->
                failure(Expected(expected.show()), Actual(actual.show()))
        }
    }
}

fun <T : Any?> eq(actual: Node<T>?, expected: Node<T>?): Throwable? {
    // if we have null and non null, usually that's a failure, but people can override equals to allow it
    println("Node<T> eq match")
    return when {
        actual === expected -> null
        actual == null && expected == null -> null
        actual == null && expected != null && actual != expected -> actualIsNull(expected)
        actual != null && expected == null && actual != expected -> expectedIsNull(actual)
        actual != null && expected != null -> NodeEq<T>().equals(actual, expected)
        else -> null
    }
}

@Suppress("UNCHECKED_CAST")
infix fun <T> Node<T>.shouldBe(expected: Node<T>) {
    println("Node shouldBe")
    when (expected) {
        is Matcher<*> -> should(expected as Matcher<Node<T>>)
        else -> {
            val actual = this
            assertionCounter.inc()
            eq(actual, expected)?.let(errorCollector::collectOrThrow)
        }
    }
}

class StringTreeShow<T> : Show<T> {
    init {
        println("Create new StringTreeShow()")
    }
    override fun show(a: T): Printed {
        println("show()")
        if (a is Node<*>) {
            return TreePrinter().dumpToString(a.toStringTree()).show()
        } else {
            return "Not a tree".show()
        }
    }
}
