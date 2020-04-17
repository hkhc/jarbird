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

package io.hkhc.gradle.builder

import io.hkhc.gradle.SimplePublisherExtension
import io.hkhc.gradle.pom.Pom
import io.hkhc.util.LOG_PREFIX
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.get
import org.gradle.plugins.signing.SigningExtension

class SigningConfig(
    private val project: Project,
    private val extension: SimplePublisherExtension,
    private val pom: Pom
) {

    private val ext = (project as ExtensionAware).extensions
    private val variantCap = extension.variant.capitalize()
    private val pubName = "${extension.pubName}$variantCap"

    private val signingIgnoredMessage = "Signing operation is ignored. " +
            "Maven Central publishing cannot be done without signing the artifacts."

    fun config() {

        project.logger.debug("$LOG_PREFIX configure Signing plugin")

        if (pom.isSnapshot() && extension.signing) {
            project.logger.warn("WARNING: $LOG_PREFIX Not performing signing for SNAPSHOT artifact ('${pom.version}')")
            return
        }

        if (!isV1ConfigPresents() && !isV2ConfigPresents()) {
            project.logger.warn("WARNING: $LOG_PREFIX " +
                    "No signing setting is provided. $signingIgnoredMessage")
            return
        }

        if (extension.useGpg) {
            if (isV1ConfigPresents() && !isV2ConfigPresents()) {
                project.logger.warn("WARNING: $LOG_PREFIX Setting to use keybox file but signing gpg keyring " +
                        "configuration is found. Fall back to use gpg keyring")
                extension.useGpg = false
            }
        } else {
            if (!isV1ConfigPresents() && isV2ConfigPresents()) {
                project.logger.warn("WARNING: $LOG_PREFIX Setting to use gpg keyring file but signing gpg keybox " +
                        " configuration is found. Switch to use gpg keybox")
                extension.useGpg = true
            }
        }

        val incomplete = (extension.useGpg && !isV2ConfigPresents()) ||
                (!extension.useGpg && !isV1ConfigPresents())

        if (incomplete) {
            project.logger.warn("WARNING: $LOG_PREFIX " +
                    "Signing configuration for keybox file is not complete. $signingIgnoredMessage")
        }
        else {
            project.logger.debug("$LOG_PREFIX Signing info complete")
        }

        ext.findByType(SigningExtension::class.java)?.config()
    }

    private fun isV1ConfigPresents() =
            project.properties["signing.keyId"] != null &&
            project.properties["signing.password"] != null &&
            project.properties["signing.secretKeyRingFile"] != null

    private fun isV2ConfigPresents() =
            project.properties["signing.gnupg.keyName"] != null &&
            project.properties["signing.gnupg.passphrase"] != null

    private fun SigningExtension.config() {

        if (extension.useGpg) {
            useGpgCmd()
        }

        val publishingExtension = ext.findByType(PublishingExtension::class.java)

        isRequired = !pom.isSnapshot()

        publishingExtension?.let { sign(it.publications[pubName]) }
    }
}
