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

package io.hkhc.gradle

interface SigningStrategy {
    /**
     * Not to do signing for this pub
     */
    fun doNotSign()

    /**
     * Do signing for this pub (default)
     */
    fun shouldSign(): Boolean

    /**
     * Use GnuPG v1 Keyring to perform artifact signing
     */
    fun signWithKeyring()

    /**
     * return true if the artifacts is going to be signed with keyring
     */
    fun isSignWithKeyring(): Boolean

    /**
     * Use GnuPG v2 Keybox to perform artifact signing
     */
    fun signWithKeybox()

    /**
     * return true if the artifacts is going to be signed with keybox
     */
    fun isSignWithKeybox(): Boolean
}
