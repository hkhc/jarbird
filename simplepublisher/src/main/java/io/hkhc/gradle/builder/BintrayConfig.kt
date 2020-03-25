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

import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.RecordingCopyTask
import io.hkhc.gradle.pom.Pom
import io.hkhc.gradle.PublishConfig
import io.hkhc.gradle.SimplePublisherExtension
import io.hkhc.gradle.pom.fill
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.closureOf

class BintrayConfig(
    private val project: Project,
    private val extension: SimplePublisherExtension,
    private val pom: Pom
) {

    private val pubConfig = PublishConfig(project)
    private val variantCap = extension.variant.capitalize()
    private val pubName = "${extension.pubName}$variantCap"
    private val ext = (project as ExtensionAware).extensions

    fun config() {
        ext.findByType(BintrayExtension::class.java)?.config()

        /*
            Why does bintrayUpload not depends on _bintrayRecordingCopy by default?
         */
        project.tasks.named("bintrayUpload").get().apply {
            dependsOn("_bintrayRecordingCopy")
        }
    }

    private fun BintrayExtension.config() {
        override = true
        dryRun = false
        publish = true

        user = pubConfig.bintrayUser
        key = pubConfig.bintrayApiKey

        if (extension.gradlePlugin) {
            setPublications(pubName, "${pubName}PluginMarkerMaven")
        } else {
            setPublications(pubName)
        }

        pom.fill(pkg)

        // Bintray requires our private key in order to sign archives for us. I don't want to share
        // the key and hence specify the signature files manually and upload them.
        filesSpec(closureOf<RecordingCopyTask> {
            from("${project.buildDir}/libs").apply {
                include("*.aar.asc")
                include("*.jar.asc")
            }

            from("${project.buildDir}/publications/$pubName").apply {
                include("pom-default.xml.asc")
                rename("pom-default.xml.asc",
                    "${pom.name}-${pom.version}.pom.asc")
            }
            into("${pom.group!!.replace('.', '/')}/${pom.name}/${pom.version}")
        })
    }
}
