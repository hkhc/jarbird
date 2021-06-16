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

import io.hkhc.utils.DiffPresenter
import io.hkhc.utils.tree.Node
import io.hkhc.utils.tree.TreePrinter
import io.hkhc.utils.tree.isSubtreeOf
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import java.io.StringWriter

infix fun <C : Node<String>> C.shouldBeSubreeOf(tree: Node<String>) = this should isSubtreeOfMatcher(tree)

fun isSubtreeOfMatcher(actual: Node<String>) = object : Matcher<Node<String>> {
    override fun test(value: Node<String>): MatcherResult {

        val expected = value

        val isSubtree = expected.isSubtreeOf(actual)

        println("isSubtree $isSubtree")

        val actualGraph = TreePrinter().dumpToString(actual)
        val expectedGraph = TreePrinter().dumpToString(expected)
        val failureMessageWriter = StringWriter()
        DiffPresenter<String>().print(expectedGraph.lines(), actualGraph.lines(), failureMessageWriter)

        return MatcherResult(
            isSubtree,
            { "Expected task graph should be subtree of actual tasks graph\n$failureMessageWriter" },
            { "Expected task graph should not be subtree of actual tasks graph\n$actualGraph" }
        )
    }
}
