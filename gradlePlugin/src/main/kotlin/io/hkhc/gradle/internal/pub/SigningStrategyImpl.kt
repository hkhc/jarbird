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

import io.hkhc.gradle.SigningStrategy

class SigningStrategyImpl : SigningStrategy {

    /**
     * Configure for artifact signing or not
     */
    private var signing = true

    /**
     * Use if performing signing with external GPG command. false to use Gradle built-in PGP implementation.
     * We will need useGpg=true if we use new keybox (.kbx) format for signing key.
     */
    private var useKeybox = true

    override fun doNotSign() {
        signing = false
    }

    override fun needSign() {
        signing = true
    }

    override fun shouldSignOrNot(): Boolean {
        return signing
    }

    override fun signWithKeyring() {
        signing = true
        useKeybox = false
    }

    override fun signWithKeybox() {
        signing = true
        useKeybox = true
    }

    override fun isSignWithKeyring(): Boolean {
        return signing && !useKeybox
    }

    override fun isSignWithKeybox(): Boolean {
        return signing && useKeybox
    }
}
