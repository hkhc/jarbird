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

package io.hkhc.gradle.pom.internal

import java.io.File

internal const val POM_FILENAME = "pom.yaml"
internal const val POM_FILENAME_2 = "pom.yml"

internal fun pomPath(base: String): String {

    val filename = base + File.separatorChar + POM_FILENAME
    val filename2 = base + File.separatorChar + POM_FILENAME_2
    return if (!File(filename).exists() && File(filename2).exists()) {
        filename2
    } else {
        filename
    }
}
