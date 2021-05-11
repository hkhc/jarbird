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

package io.hkhc.test.utils.test

import io.kotest.core.TestConfiguration
import java.io.File
import java.lang.IllegalStateException
import java.nio.file.Files

/**
 * Modify from kotest's tempfile(), to create a temporary directory
 * */

fun TestConfiguration.tempDirectory(prefix: String? = null): File {
    val dir = Files.createTempDirectory(prefix).toFile()
    afterSpec {
        dir.deleteRecursively()
    }
    return dir
}

fun File.mkdir(subdir: String): File {
    if (!isDirectory) {
        throw IllegalStateException("Cannot create directory under file")
    }
    val newDir = File(this, subdir)
    newDir.mkdirs()
    return newDir
}
