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

package io.hkhc.gradle.internal

import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.internal.utils.findByType
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.get
import org.gradle.plugins.signing.SigningExtension

class SigningConfig(
    private val project: Project,
    private val pubs: List<JarbirdPub>
) {

    private val signingIgnoredMessage = "Signing operation is ignored. " +
        "Maven Central publishing cannot be done without signing the artifacts."

    // return true if checking passed, otherwise failed
    private fun validateConfig(project: Project, pub: JarbirdPub): Boolean {

        var complete = if (pub.pom.isSnapshot() && pub.shouldSignOrNot()) {
            project.logger.warn(
                "WARNING: $LOG_PREFIX Not performing signing " +
                    "for SNAPSHOT artifact ('${pub.pom.version}')"
            )
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
            if (pub.isSignWithKeybox()) {
                if (isV1ConfigPresents() && !isV2ConfigPresents()) {
                    project.logger.warn(
                        "WARNING: $LOG_PREFIX Setting to use keybox file but signing gpg keyring " +
                            "configuration is found. Fall back to use gpg keyring"
                    )
                    pub.signWithKeyring()
                }
            } else if (!isV1ConfigPresents() && isV2ConfigPresents()) {
                project.logger.warn(
                    "WARNING: $LOG_PREFIX Setting to use gpg keyring file but signing gpg keybox " +
                        " configuration is found. Switch to use gpg keybox"
                )
                pub.signWithKeybox()
            }
            complete = (!pub.isSignWithKeybox() || isV2ConfigPresents()) &&
                (pub.isSignWithKeybox() || isV1ConfigPresents())
        }

        return complete
    }

    fun config() {

        project.logger.debug("$LOG_PREFIX configure Signing plugin")

        pubs.forEach { pub ->
            val complete = validateConfig(project, pub)

            if (!complete) {
                project.logger.warn(
                    "WARNING: $LOG_PREFIX " +
                        "Signing configuration for keybox file is not complete. $signingIgnoredMessage"
                )
            } else {
                project.logger.debug("$LOG_PREFIX Signing info complete")
            }
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

        pubs.forEach { pub ->
            if (pub.isSignWithKeybox()) {
                useGpgCmd()
            }

            isRequired = pub.pom.isRelease()

            if (pub.pom.isRelease()) {
                project.findByType(PublishingExtension::class.java)?.let {
                    sign(it.publications[pub.pubNameWithVariant()])
                    if (pub.pom.isGradlePlugin()) {
                        sign(it.publications[pub.markerPubName])
                    }
                }
            }
        }
    }
}
