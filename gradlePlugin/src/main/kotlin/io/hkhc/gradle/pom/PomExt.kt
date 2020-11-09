import io.hkhc.gradle.utils.SNAPSHOT_SUFFIX

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

fun String?.isSnapshot() = this?.endsWith(SNAPSHOT_SUFFIX) ?: false

/* assume that there is nothing after -SNAPSHOT */
fun String.appendBeforeSnapshot(s: String): String {
    val index = indexOf(SNAPSHOT_SUFFIX)
    return if (index == -1) {
        "$this-$s"
    } else {
        val versionPrefix = substring(0, index)
        "$versionPrefix-$s$SNAPSHOT_SUFFIX"
    }
}
