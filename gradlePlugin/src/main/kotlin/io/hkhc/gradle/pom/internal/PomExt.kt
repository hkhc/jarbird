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

import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.internal.SNAPSHOT_SUFFIX
import io.hkhc.gradle.internal.utils.multiLet

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
    val index = lastIndexOf(SNAPSHOT_SUFFIX)
    return if (index == -1) {
        "$this-$s"
    } else {
        val versionPrefix = substring(0, index)
        "$versionPrefix-$s$SNAPSHOT_SUFFIX"
    }
}

val JarbirdPub.gavPath: String
    get() {
        with(pom) {
            return multiLet(group, artifactId, version) { g, _, _ ->
                "${g.replace('.', '/')}/${variantArtifactId()}/${variantVersion()}"
            } ?: throw IllegalStateException(
                "GAV in POM is not complete: " +
                    "group=$group, artifactId=$artifactId, version=$version"
            )
        }
    }

val JarbirdPub.avFileBase: String
    get() = "${variantArtifactId()}-${variantVersion()}"

/**
 * List of license identifiers and URL to the text as according to SPDX License List
 * https://spdx.org/licenses/
 */
internal var LICENSE_MAP = mapOf(
    "Apache-2.0" to "http://www.apache.org/licenses/LICENSE-2.0.txt",
    "BSD-3-Clause" to "https://https://opensource.org/licenses/BSD-3-Clause",
    "MIT" to "http://www.opensource.org/licenses/mit-license.php",
    "GPLv3" to "https://www.gnu.org/licenses/gpl-3.0.html",
    "LGPLv3" to "https://www.gnu.org/licenses/lgpl-3.0.html"
)
