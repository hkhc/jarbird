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

/**
 * Adapted from https://stackoverflow.com/a/11024200/181597
 */
class Version(version: String?) : Comparable<Version?> {
    private val version: String
    fun get(): String {
        return version
    }

    override fun compareTo(that: Version?): Int {
        if (that == null) return 1
        val thisParts = this.get().split(".").toTypedArray()
        val thatParts = that.get().split(".").toTypedArray()
        val length = Math.max(thisParts.size, thatParts.size)
        for (i in 0 until length) {
            val thisPart = if (i < thisParts.size) thisParts[i].toInt() else 0
            val thatPart = if (i < thatParts.size) thatParts[i].toInt() else 0
            if (thisPart < thatPart) return -1
            if (thisPart > thatPart) return 1
        }
        return 0
    }

    override fun equals(that: Any?): Boolean {
        if (this === that) return true
        if (that == null) return false
        return if (this.javaClass != that.javaClass) false else this.compareTo(that as Version) == 0
    }

    init {
        requireNotNull(version) { "Version can not be null" }
        require(version.matches(Regex("[0-9]+(\\.[0-9]+)*"))) { "Invalid version format" }
        this.version = version
    }
}
