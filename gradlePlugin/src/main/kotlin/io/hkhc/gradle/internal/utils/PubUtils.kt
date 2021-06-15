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

package io.hkhc.gradle.internal.utils

import io.hkhc.gradle.internal.JarbirdPubImpl
import io.hkhc.gradle.internal.PomResolver

fun normalizePubName(name: String): String {
    val newName = StringBuffer()
    var newWord = true
    var firstWord = true
    name.forEach {
        if (it.isLetterOrDigit()) {
            if (newWord) {
                newName.append(if (firstWord) it.toLowerCase() else it.toUpperCase())
                newWord = false
            } else {
                newName.append(it)
                firstWord = false
            }
        } else {
            // ignore non letter or digit char
            newWord = true
        }
    }
    return newName.toString()
}

fun initPub(pomResolver: PomResolver, pub: JarbirdPubImpl) {

    pub.pom = pomResolver.resolve(pub.variant)

    // TODO handle two publications of same artifactaId in the same module.
    // check across the whole pubList, and generate alternate pubName if there is colliding of artifactId
    pub.pubName = normalizePubName(pub.pom.artifactId ?: "Lib")
}
