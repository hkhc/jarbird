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

import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.internal.JarbirdPubImpl
import io.hkhc.gradle.internal.PomResolver
import io.hkhc.gradle.internal.pubNameWithVariant

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

fun resolveDuplicatedName(pubList: List<JarbirdPub>, proposedName: String): String {
    var curName = proposedName
    var curIndex = 1
    while (pubList.find { it.pubNameWithVariant() == it.pubNameWithVariant(curName) } != null) {
        curName = "$proposedName$curIndex"
        curIndex++
    }
    return curName
}

fun initPub(pomResolver: PomResolver, pubList: List<JarbirdPub>, newPub: JarbirdPubImpl) {

    newPub.pom = pomResolver.resolve(newPub.variant)

    val proposedPubName = normalizePubName(newPub.pom.artifactId ?: "Lib")

    newPub.pubName = resolveDuplicatedName(pubList, proposedPubName)
}
