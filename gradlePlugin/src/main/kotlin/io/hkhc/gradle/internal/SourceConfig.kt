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

package io.hkhc.gradle.internal

import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.SourceDirs
import io.hkhc.gradle.SourceSetNames
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar

internal class SourceConfig(private val project: Project) {

    private fun getSourceJarSource(source: Any): Array<out Any> {
        return when(source) {
            is String -> SourceSetNames(project, arrayOf(source)).getDirs()
            is SourceSetNames -> source.getDirs()
            is SourceDirs -> arrayOf(source.getDirs())
            else -> arrayOf(source)
        }
    }

    @Suppress("UnstableApiUsage")
    fun configSourceJarTask(pub: JarbirdPubImpl): TaskProvider<Jar>? {

        val sourcesJarTaskName = pub.pubNameWithVariant("sourcesJar${pub.pubNameCap}")
        return try {
            project.tasks.named(sourcesJarTaskName, Jar::class.java) {
                archiveClassifier.set(CLASSIFIER_SOURCE)
                // TODO look like it affect other JAR tasks, may be a better place for that
                archiveBaseName.set(pub.variantArtifactId())
                archiveVersion.set(pub.variantVersion())
            }
        } catch (e: UnknownTaskException) {
            val desc = if (pub.variant == "") {
                "Create archive of source code for the binary"
            } else {
                "Create archive of source code for the binary of variant '${pub.variant}' "
            }

            println("PublishingConfig sourceSets count ${sourceSets.size}")
            sourceSets.forEach {
                println("PublishingConfig sourceSets ${it.name}")
            }

            project.tasks.register(sourcesJarTaskName, Jar::class.java) {
                group = PUBLISH_GROUP
                description = desc
                archiveClassifier.set(CLASSIFIER_SOURCE)
                archiveBaseName.set(pub.variantArtifactId())
                archiveVersion.set(pub.variantVersion())
                from(*getSourceJarSource(pub.sourceSets ?:
                    throw GradleException("Source set is not available")
                ))
            }
        }
    }

    private val extensions = (project as ExtensionAware).extensions

    private val sourceSets: SourceSetContainer
        get() =
            extensions.getByName("sourceSets") as SourceSetContainer
}
