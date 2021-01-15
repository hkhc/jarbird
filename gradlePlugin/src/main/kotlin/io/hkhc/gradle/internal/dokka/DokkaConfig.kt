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
import io.hkhc.gradle.internal.DokkaJarPubTaskInfo
import io.hkhc.gradle.internal.JarbirdPubImpl
import io.hkhc.gradle.internal.JbDokkaPubTaskInfo
import io.hkhc.gradle.internal.JbDokkaTaskInfo
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.dokka.gradle.DokkaTask

// TODO create tasks to generate multiple doc types
internal class DokkaConfig(private val project: Project, private val extension: JarbirdExtension) {

    private val docType = "Html"

    private fun getSourceJarSource(source: Any): Array<out Any> {
        return when (source) {
            is SourceSet -> source.allSource.srcDirs.toTypedArray()
            is String -> SourceSetNames(project, arrayOf(source)).getDirs()
            is SourceSetNames -> source.getDirs()
            is SourceDirs -> arrayOf(source.getDirs())
            // TODO is SourceSetContainer -> ...
            else -> arrayOf(source)
        }
    }

    fun configRootDokka(pubs: List<JarbirdPub>) {

        JbDokkaTaskInfo().register(project.tasks, DokkaMultiModuleTask::class.java) {
//            extension.dokkaConfig.invoke(this)
            pubs.forEach { pub ->
                addSubprojectChildTasks(JbDokkaPubTaskInfo(pub).name)
            }
        }
    }

    @Suppress("SpreadOperator")
    fun configDokka(pubs: List<JarbirdPub>) {
        pubs.forEach { pub ->
            val impl = pub as JarbirdPubImpl
            JbDokkaPubTaskInfo(pub).register(project.tasks, DokkaTask::class.java) {
                dokkaSourceSets.create("${pub.pom.group}:${pub.pom.artifactId}") {
                    sourceRoots.setFrom(
                        *(getSourceJarSource(impl.sourceSet ?: impl.docSourceSets))
                    )
                }
//                extension.dokkaConfig.invoke(this)
                pub.dokkaConfig.invoke(this, pub)
            }
        }
    }

    @Suppress("UnstableApiUsage")
    fun setupDokkaJar(pub: JarbirdPub): TaskProvider<Jar> {

        // TODO add error message here if dokka is null
        return DokkaJarPubTaskInfo(pub).register(project.tasks, Jar::class.java) {

            archiveClassifier.set(CLASSIFIER_JAVADOC)
            archiveBaseName.set(pub.variantArtifactId())
            archiveVersion.set(pub.variantVersion())
            from(project.tasks.named(JbDokkaPubTaskInfo(pub).name))
            dependsOn(project.tasks.named(JbDokkaPubTaskInfo(pub).name))
        }
    }
}
