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

package io.hkhc.gradle.internal.pub

import groovy.lang.Closure
import io.hkhc.gradle.DocDeclaration
import io.hkhc.gradle.JarbirdPub
import org.jetbrains.dokka.gradle.DokkaTask

/**
 * The normal usage would be having default block for terminal declration only.
 */
class DocDeclarationImpl(
    private val parent: DocDeclaration? = null,
    private val defaultBlock: (DokkaTask.(pub: JarbirdPub) -> Unit)? = null
): DocDeclaration {

    private var mDokkaConfig: (DokkaTask.(pub: JarbirdPub) -> Unit)? = null

    private var shouldGenDoc: Boolean? = null

    override fun noDoc() {
        shouldGenDoc = false
    }

    override fun genDoc() {
        shouldGenDoc = true
    }

    override fun genDocOrNot(): Boolean? {
        return shouldGenDoc ?: parent?.genDocOrNot()
    }

    /**
     * The order of precedence
     * - the local custom dokkaConfig
     * - the dokkaConfig as provided by parent declaration
     * - the local default dokkaConfig
     * - the ultimate default of default "{}"
      */
    override val dokkaConfig: DokkaTask.(pub: JarbirdPub) -> Unit
        get() {
            return mDokkaConfig ?: parent?.dokkaConfig ?: defaultBlock ?: { }
        }

    override fun dokkaConfig(action: Closure<DokkaTask>) {
        mDokkaConfig = { pub ->
            action.delegate = this
            action.resolveStrategy = Closure.DELEGATE_FIRST
            action.call(pub)
        }
    }

    override fun dokkaConfig(block: DokkaTask.(pub: JarbirdPub) -> Unit) {
        mDokkaConfig = block
    }


}
