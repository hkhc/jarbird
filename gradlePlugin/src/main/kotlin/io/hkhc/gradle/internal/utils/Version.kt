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
    private val versionText: String
    fun get(): String {
        return versionText
    }

    class VersionStruct(v: String) {
        var versionParts: Array<String>
        var suffixParts: Array<String>
        init {
            val prefixDelimiter = v.indexOf('-')
            if (prefixDelimiter != -1) {
                versionParts = v.substring(0, prefixDelimiter).split(".").toTypedArray()
                suffixParts = v.substring(prefixDelimiter + 1, v.length).split("-").toTypedArray()
            } else {
                versionParts = v.split(".").toTypedArray()
                suffixParts = arrayOf()
            }
        }
    }

    override fun compareTo(other: Version?): Int {
        if (other == null) return 1
        val thisParts = VersionStruct(this.get())
        val thatParts = VersionStruct(other.get())
        var length = Math.max(thisParts.versionParts.size, thatParts.versionParts.size)
        for (i in 0 until length) {
            val thisPart = if (i < thisParts.versionParts.size) thisParts.versionParts[i].toInt() else 0
            val thatPart = if (i < thatParts.versionParts.size) thatParts.versionParts[i].toInt() else 0
            if (thisPart < thatPart) return -1
            if (thisPart > thatPart) return 1
        }
        length = Math.max(thisParts.suffixParts.size, thatParts.suffixParts.size)
        for (i in 0 until length) {
            val thisPart = if (i < thisParts.suffixParts.size) thisParts.suffixParts[i] else ""
            val thatPart = if (i < thatParts.suffixParts.size) thatParts.suffixParts[i] else ""
            if (thisPart < thatPart) return -1
            if (thisPart > thatPart) return 1
        }
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        return if (this.javaClass != other.javaClass) false else this.compareTo(other as Version) == 0
    }

    override fun hashCode(): Int {
        return versionText.hashCode()
    }

    init {
        requireNotNull(version) { "Version can not be null" }
        require(version.matches(Regex("[0-9]+(\\.[0-9]+)*(-[a-zA-Z0-9\\-]+)*"))) {
            "'$version' is invalid version format"
        }
        this.versionText = version
    }
}
