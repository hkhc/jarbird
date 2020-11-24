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

package io.hkhc.gradle.internal.dokka

import io.hkhc.gradle.JarbirdExtension
import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.SourceDirs
import io.hkhc.gradle.SourceSetNames
import io.hkhc.gradle.internal.CLASSIFIER_JAVADOC
import io.hkhc.gradle.internal.JarbirdPubImpl
import io.hkhc.gradle.internal.PUBLISH_GROUP
import io.hkhc.gradle.internal.pubNameCap
import io.hkhc.gradle.internal.pubNameWithVariant
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.dokka.gradle.DokkaTask

// TODO create tasks to generate multiple doc types
internal class DokkaConfig(private val project: Project, private val extension: JarbirdExtension) {

    private val docType = "Html"

    private fun rootTaskName() = "jbDokka${docType}MultiModule"
    private fun moduleTaskName(pub: JarbirdPub) = "jbDokka${docType}${pub.pubNameCap}"

    private fun getSourceJarSource(source: Any): Array<out Any> {
        return when (source) {
            is String -> SourceSetNames(project, arrayOf(source)).getDirs()
            is SourceSetNames -> source.getDirs()
            is SourceDirs -> arrayOf(source.getDirs())
            // TODO is SourceSetContainer -> ...
            else -> arrayOf(source)
        }
    }

    fun configRootDokka() {

        project.tasks.create(rootTaskName(), DokkaMultiModuleTask::class.java) {
            description = "Generates documentation in 'html' format"
            extension.dokkaConfig.invoke(this)
        }
    }

    @Suppress("SpreadOperator")
    fun configDokka(pub: JarbirdPubImpl) {

        project.tasks.create(moduleTaskName(pub), DokkaTask::class.java) {
            description = "Generates documentation in 'html' format for publication ${pub.pubName}"
//            moduleName.set("${pub.pom.group}:${pub.pom.artifactId}")
            dokkaSourceSets.create("${pub.pom.group}:${pub.pom.artifactId}") {
                sourceRoots.setFrom(
                    *(getSourceJarSource(pub.sourceSets))
                )
            }
            extension.dokkaConfig.invoke(this)
            pub.dokkaConfig.invoke(this, pub)
        }

        val rootTask = project.rootProject.tasks.findByName(rootTaskName())
        rootTask?.let {
            (it as DokkaMultiModuleTask).addSubprojectChildTasks(moduleTaskName(pub))
        }
    }

    @Suppress("UnstableApiUsage")
    fun setupDokkaJar(pub: JarbirdPubImpl): TaskProvider<Jar>? {

        val dokkaJarTaskName = pub.pubNameWithVariant("dokkaJar${pub.pubNameCap}")
        // TODO add error message here if dokka is null
        return project.tasks.register(dokkaJarTaskName, Jar::class.java) {
            group = PUBLISH_GROUP
            description = "Assembles Kotlin docs with Dokka to Jar"
            archiveClassifier.set(CLASSIFIER_JAVADOC)
            archiveBaseName.set(pub.variantArtifactId())
            archiveVersion.set(pub.variantVersion())
            from(project.tasks.named(moduleTaskName(pub)))
            dependsOn(project.tasks.named(moduleTaskName(pub)))
        }
    }
}
