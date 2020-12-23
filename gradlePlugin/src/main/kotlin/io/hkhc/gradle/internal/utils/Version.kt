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

    companion object {
        private val versionRegex = Regex("[0-9]+(\\.[0-9]+)*(-[a-zA-Z0-9\\-]+)*")
    }

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
        fun partAsInt(i: Int): Int {
            return if (i < versionParts.size) versionParts[i].toInt() else 0
        }
        fun suffixAsString(i: Int): String {
            return if (i < suffixParts.size) suffixParts[i] else ""
        }
    }

    override fun compareTo(other: Version?): Int {
        if (other == null) return 1
        val thisParts = VersionStruct(this.get())
        val thatParts = VersionStruct(other.get())
        var length = Math.max(thisParts.versionParts.size, thatParts.versionParts.size)
        var result = 0
        for (i in 0 until length) {
            val thisPart = thisParts.partAsInt(i)
            val thatPart = thatParts.partAsInt(i)
            result = thisPart.compareTo(thatPart)
            if (result != 0) break
        }
        if (result == 0) {
            length = Math.max(thisParts.suffixParts.size, thatParts.suffixParts.size)
            for (i in 0 until length) {
                val thisPart = thisParts.suffixAsString(i)
                val thatPart = thatParts.suffixAsString(i)
                result = thisPart.compareTo(thatPart)
                if (result != 0) break
            }
        }
        return result
    }

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> {
                true
            }
            other == null -> {
                false
            }
            this.javaClass != other.javaClass -> {
                false
            }
            else -> {
                this.compareTo(other as Version) == 0
            }
        }
    }

    override fun hashCode(): Int {
        return versionText.hashCode()
    }

    init {
        requireNotNull(version) { "Version can not be null" }
        require(version.matches(versionRegex)) {
            "'$version' is invalid version format"
        }
        this.versionText = version
    }
}
