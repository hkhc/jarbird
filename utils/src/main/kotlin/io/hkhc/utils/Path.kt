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

class Path(private val relative: Boolean = false) {

    companion object {
        fun path(vararg pathParts: String): Path {
            val p = Path()
            p.build(*pathParts)
            return p
        }
        fun relativePath(vararg pathParts: String): Path {
            val p = Path(true)
            p.build(*pathParts)
            return p
        }
    }

    private lateinit var str: String

    fun build(vararg pathParts: String) {
        val builder = StringBuilder()
        pathParts.forEachIndexed { index, part ->
            if (index > 0 || !relative) builder.append('/')
            builder.append(part)
        }
        str = builder.toString()
    }

    override fun toString(): String {
        return str
    }

    operator fun plus(other: Path): Path {
        return relativePath(
            this.toString(),
            if (other.toString()[0] == '/') other.toString().substring(1) else other.toString()
        )
    }
}
