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

import io.hkhc.gradle.JarbirdExtension
import io.hkhc.gradle.pom.Pom
import io.hkhc.gradle.utils.LOG_PREFIX
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.get
import org.gradle.plugins.signing.SigningExtension

class SigningConfig(
    private val project: Project,
    private val extension: JarbirdExtension,
    private val pom: Pom
) {

    private val signingIgnoredMessage = "Signing operation is ignored. " +
        "Maven Central publishing cannot be done without signing the artifacts."

    // return true if checking passed, otherwise failed
    private fun validateConfig(project: Project, extension: JarbirdExtension, pom: Pom): Boolean {

        var complete = if (pom.isSnapshot() && extension.signing) {
            project.logger.warn("WARNING: $LOG_PREFIX Not performing signing for SNAPSHOT artifact ('${pom.version}')")
            false
        } else if (!isV1ConfigPresents() && !isV2ConfigPresents()) {
            project.logger.warn(
                "WARNING: $LOG_PREFIX " +
                    "No signing setting is provided. $signingIgnoredMessage"
            )
            false
        } else {
            true
        }

        if (complete) {
            if (extension.useGpg) {
                if (isV1ConfigPresents() && !isV2ConfigPresents()) {
                    project.logger.warn(
                        "WARNING: $LOG_PREFIX Setting to use keybox file but signing gpg keyring " +
                            "configuration is found. Fall back to use gpg keyring"
                    )
                    extension.useGpg = false
                }
            } else if (!isV1ConfigPresents() && isV2ConfigPresents()) {
                project.logger.warn(
                    "WARNING: $LOG_PREFIX Setting to use gpg keyring file but signing gpg keybox " +
                        " configuration is found. Switch to use gpg keybox"
                )
                extension.useGpg = true
            }
            complete = (!extension.useGpg || isV2ConfigPresents()) &&
                (extension.useGpg || isV1ConfigPresents())
        }

        return complete
    }

    fun config() {

        project.logger.debug("$LOG_PREFIX configure Signing plugin")

        val complete = validateConfig(project, extension, pom)

        if (!complete) {
            project.logger.warn(
                "WARNING: $LOG_PREFIX " +
                    "Signing configuration for keybox file is not complete. $signingIgnoredMessage"
            )
        } else {
            project.logger.debug("$LOG_PREFIX Signing info complete")
        }

        (
            project.findByType(SigningExtension::class.java)
                ?: throw GradleException("Signing extension is not found. May be Signing Plugin is not applied?")
            ).config()
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

        isRequired = !pom.isSnapshot()

        if (!pom.isSnapshot()) {
            project.findByType(PublishingExtension::class.java)?.let {
                sign(it.publications[extension.pubNameWithVariant()])
            }
        }
    }
}
