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

import io.hkhc.gradle.pom.Pom
import io.hkhc.gradle.SimplePublisherExtension
import org.gradle.api.GradleException
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

    fun config() {

        if (!isV1ConfigPresents() && !isV2ConfigPresents()) {
//            project.logger.error(
//                """
//                Signing configuration is not complete. The combined properies of all gradle.properties shall specifies
//                one of the following groups of properties:
//                For GNUPG v1 (if you have .gpg file)
//                - signing.keyId : the short form if ID of the key pair for signing
//                - signing.passphrase : the passphrase to unlock the key store (.gpg file)
//                - signing.secretKeyRingFile : the full path to the .gpg file with private key for signing
//                For GNU PG v2 (if you have .kbx file. gnu pg must be installed on the system)
//                - signing.gnupg.keyName : the short form if ID of the key pair for signing
//                - signing.gnupg.passphrase : the passphrase to unlock the key store (.kbx file)
//            """.trimIndent())
//            throw GradleException("Incomplete signing config")
            project.logger.warn("Signing configuration is not complete. Signing operation is ignored.")
            project.logger.warn("Maven Central publishing cannot be done without signing the artifacts.")
            return
        } else {
            project.logger.info("Signing info complete")
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

        if (pom.isSnapshot()) {
            project.logger.info("Not performing signing for SNAPSHOT artifact")
        }

        isRequired = !pom.isSnapshot()

        publishingExtension?.let { sign(it.publications[pubName]) }
    }
}
