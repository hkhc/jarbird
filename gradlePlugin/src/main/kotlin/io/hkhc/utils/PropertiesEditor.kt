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

package io.hkhc.utils

import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.util.Properties

class PropertiesEditor(val filename: String, spec: PropertiesEditor.() -> Unit) {

    val props = Properties()

    init {
        try {
            props.load(FileReader(filename))
        } catch (e: FileNotFoundException) {
            // do nothing intentionally
        }
        spec.invoke(this)
        try {
            props.store(FileWriter(filename), "")
        } catch (e: FileNotFoundException) {
            // do nothing intentionally
        }
    }

    infix fun String.to(that: String) {
        props.setProperty(this, that)
    }

    fun String.unaryMinus() {
        props.remove(this)
    }

    fun print() {
        props.forEach {
            System.out.println("${it.key} = ${it.value}")
        }
    }
}
