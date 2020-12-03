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
import io.hkhc.gradle.internal.bintray.BintrayPublishPlan
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

internal const val JB_TASK_PREFIX = "jbPublish"
internal const val PUBLISH_TASK_PREFIX = "publish"
internal const val MAVEN_LOCAL_CAP = "MavenLocal"

internal const val TO_MAVEN_LOCAL = "To$MAVEN_LOCAL_CAP"
internal const val TO_MAVEN_REPO = "ToMavenRepository"
internal const val TO_GRADLE_PORTAL = "ToGradlePortal"
internal const val TO_BINTRAY = "ToBintray"

internal const val PLUGIN_MARKER_PUB_SUFFIX = "PluginMarkerMaven"

private const val JB_PUBLISH_TO_MAVEN_LOCAL_TASK = "$JB_TASK_PREFIX$TO_MAVEN_LOCAL"
private const val JB_PUBLISH_TO_MAVEN_REPO_TASK = "$JB_TASK_PREFIX$TO_MAVEN_REPO"
private const val JB_PUBLISH_TO_BINTRAY_TASK = "$JB_TASK_PREFIX$TO_BINTRAY"
internal const val JB_PUBLISH_TO_GRADLE_PORTAL_TASK = "$JB_TASK_PREFIX$TO_GRADLE_PORTAL"

interface TaskInfo {

    val group: String
        get() = SP_GROUP
    val name: String
    val description: String

    fun register(container: TaskContainer, block: Task.() -> Unit = {}): TaskProvider<Task> {
        return container.register(name) {
            val task = this
            task.group = this@TaskInfo.group
            task.description = this@TaskInfo.description
            block.invoke(task)
        }
    }

    fun <T : Task> register(container: TaskContainer, type: Class<T>, block: T.() -> Unit = {}): TaskProvider<T> {
        return container.register(name, type) {
            val task = this
            task.group = this@TaskInfo.group
            task.description = this@TaskInfo.description
            block.invoke(task)
        }
    }
}

class JbPubishTaskInfo : TaskInfo {
    override val name: String
        get() = JB_TASK_PREFIX
    override val description: String
        get() = "Publish everything"
}

class JbPublishToMavenLocalTaskInfo : TaskInfo {
    override val name: String
        get() = JB_PUBLISH_TO_MAVEN_LOCAL_TASK
    override val description: String
        get() = "Publish all maven publicationx to the local Maven Repository"
}

class JbPublishPubToMavenLocalTaskInfo(val pub: JarbirdPub) : TaskInfo {
    override val name: String
        get() = "$JB_TASK_PREFIX${pub.pubNameCap}$TO_MAVEN_LOCAL"
    override val description: String
        get() = if (pub.pom.isGradlePlugin()) {
            "Publish '${pub.getGAV()}' and plugin marker '${pub.pluginCoordinate()}' to the local Maven Repository"
        } else {
            "Publish '${pub.getGAV()}' to the local Maven Repository"
        }
}

class JbPublishToMavenRepoTaskInfo : TaskInfo {
    override val name: String
        get() = JB_PUBLISH_TO_MAVEN_REPO_TASK
    override val description: String
        get() = "Publish all Maven publications to the respective maven Repository"
}

class JbPublishPubToMavenRepoTaskInfo(val pub: JarbirdPub) : TaskInfo {
    override val name: String
        get() = "$JB_TASK_PREFIX${pub.pubNameCap}$TO_MAVEN_REPO"
    override val description: String
        get() = if (pub.pom.isGradlePlugin()) {
            "Publish '${pub.getGAV()}' and plugin marker '${pub.pluginCoordinate()}' to "+
                ((pub as JarbirdPubImpl).mavenRepo.description)
        } else {
            "Publish '${pub.getGAV()}' to the local Maven Repository"
        }
}

class JbPublishPubToCustomMavenRepoTaskInfo(val pub: JarbirdPub) : TaskInfo {
    override val name: String
        get() = "$JB_TASK_PREFIX${pub.pubNameCap}To${(pub as JarbirdPubImpl).mavenRepo.id}"
    override val description: String
        get() = if (pub.pom.isGradlePlugin()) {
            "Publish '${pub.getGAV()}' and plugin marker '${pub.pluginCoordinate()}' " +
                "to ${(pub as JarbirdPubImpl).mavenRepo.description}"
        } else {
            // TODO 'Maven${pub.pubNameCap}' is wrong
            "Publish '${pub.getGAV()}' to ${(pub as JarbirdPubImpl).mavenRepo.description}"
        }
}

class JbPublishToGradlePortalTaskInfo : TaskInfo {
    override val name: String
        get() = JB_PUBLISH_TO_GRADLE_PORTAL_TASK
    override val description: String
        get() = "Publish all publications and plugin markers to Gradle plugin portal"
}

class JbPublishPubToGradlePortalTaskInfo(val pub: JarbirdPub) : TaskInfo {
    override val name: String
        get() = "$JB_TASK_PREFIX${pub.pubNameCap}$TO_GRADLE_PORTAL"
    override val description: String
        get() = "Publish '${pub.getGAV()}' and plugin marker '${pub.pluginCoordinate()}' to Gradle plugin portal"
}

class JbPublishToBintrayTaskInfo(private val publishPlan: BintrayPublishPlan) : TaskInfo {

    override val name: String
        get() = JB_PUBLISH_TO_BINTRAY_TASK
    override val description: String
        get() {
            val bintrayLibs =
                publishPlan.bintrayLibs.joinToString(", ") { "'${it.getGAV()}'" }
            val bintrayPlugins =
                publishPlan.bintrayPlugins.joinToString(", ") { "'${it.pluginCoordinate()}'" }
            val artifactoryLibs =
                publishPlan.artifactoryLibs.joinToString(", ") { "'${it.getGAV()}'" }

            val bintrayPublicationStr = "publication${if (publishPlan.bintrayLibs.size > 1) "s" else ""}"
            val bintrayPluginStr = "plugin${if (publishPlan.bintrayPlugins.size > 1) "s" else ""}"
            val artifactoryPublicationStr = "publication${if (publishPlan.artifactoryLibs.size > 1) "s" else ""}"

            val bintrayDesc = when {
                bintrayLibs.isNotBlank() && bintrayPlugins.isNotBlank() ->
                    "Publish $bintrayPublicationStr $bintrayLibs and $bintrayPluginStr $bintrayPlugins to Bintray."
                bintrayLibs.isNotBlank() && bintrayPlugins.isBlank() ->
                    "Publish $bintrayPublicationStr $bintrayLibs to Bintray."
                bintrayLibs.isBlank() && bintrayPlugins.isNotBlank() ->
                    "Publish $bintrayPluginStr $bintrayPlugins to Bintray."
                else -> ""
            }

            val artifactoryDesc = when {
                !artifactoryLibs.isBlank() -> """
                        Publish $artifactoryPublicationStr $artifactoryLibs to OSS Jfrog.
                """.trimIndent()
                else -> ""
            }

            return "$bintrayDesc $artifactoryDesc".trim()
        }
}

class JbPublishPubTaskInfo(private val pub: JarbirdPub) : TaskInfo {
    override val name: String
        get() = "$JB_TASK_PREFIX${pub.pubNameCap}"
    override val description: String
        get() {
            // assemble a list of repositories
            val repoList = mutableListOf<String>()
            repoList.add("Maven Local")
            if (pub.maven) {
                repoList.add("'${pub.mavenRepoName}' Repository")
            }
            if (pub.bintray) {
                repoList.add("Bintray")
            }
            if (pub.pom.isGradlePlugin()) {
                repoList.add("Gradle Plugin Portal")
            }
            val repoListStr = repoList.joinToString()

            return if (pub.pom.isGradlePlugin()) {
                "Publish Maven publication '${pub.pubNameWithVariant()}:${pub.variantVersion()}' " +
                    "and plugin '${pub.pom.plugin?.id}:${pub.pom.version}'" +
                    "to $repoListStr"
            } else {
                "Publish Maven publication '${pub.pubNameWithVariant()}' to $repoListStr"
            }
        }
}

class SourceJarPubTaskInfo(private val pub: JarbirdPub) : TaskInfo {
    override val group: String
        get() = PUBLISH_GROUP
    override val name: String
        get() = pub.pubNameWithVariant("sourcesJar${pub.pubNameCap}")
    override val description: String
        get() = if (pub.variant == "") {
            "Create archive of source code for the binary"
        } else {
            "Create archive of source code for the binary of variant '${pub.variant}' "
        }
}

class JbDokkaTaskInfo : TaskInfo {

    private val docType = "Html"

    override val group: String
        get() = "documentation"
    override val name: String
        get() = "jbDokka${docType}MultiModule"
    override val description: String
        get() = "Generates documentation in 'html' format"
}

class JbDokkaPubTaskInfo(private val pub: JarbirdPub) : TaskInfo {

    private val docType = "Html"

    override val group: String
        get() = "documentation"
    override val name: String
        get() = "jbDokka${docType}${pub.pubNameCap}"

    override val description: String
        get() = "Generates documentation in 'html' format for publication ${pub.pubName}"
}

class DokkaJarPubTaskInfo(private val pub: JarbirdPub) : TaskInfo {

    override val group: String
        get() = "documentation"
    override val name: String
        get() = pub.pubNameWithVariant("jbDokkaJar${pub.pubNameCap}")

    override val description: String
        get() = "Assembles Kotlin docs with Dokka to Jar"
}

val JarbirdPub.mavenRepoName: String
    get() = "Maven$pubNameCap"

val JarbirdPub.publishPubToMavenLocalTask: String
    get() = "$PUBLISH_TASK_PREFIX${pubId.capitalize()}$TO_MAVEN_LOCAL"

val JarbirdPub.publishPluginMarkerPubToMavenLocalTask: String
    get() = "$PUBLISH_TASK_PREFIX$pubNameCap${PLUGIN_MARKER_PUB_SUFFIX}Publication$TO_MAVEN_LOCAL"
val JarbirdPub.publishPubToCustomMavenRepoTask: String
    get() = "$PUBLISH_TASK_PREFIX${pubId}To$mavenRepoNameCap"

val JarbirdPub.publishPluginMarkerPubToCustomMavenRepoTask: String
    get() = "$PUBLISH_TASK_PREFIX$pubNameCap${PLUGIN_MARKER_PUB_SUFFIX}PublicationTo$mavenRepoNameCap"
