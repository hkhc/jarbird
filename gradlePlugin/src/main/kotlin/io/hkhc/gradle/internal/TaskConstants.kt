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
import io.hkhc.gradle.RepoSpec
import io.hkhc.gradle.internal.bintray.BintrayPublishPlan
import io.hkhc.gradle.internal.repo.BintraySnapshotRepoSpec
import io.hkhc.gradle.internal.repo.GradlePortalSpec
import io.hkhc.gradle.internal.repo.MavenRepoSpec
import io.hkhc.gradle.internal.utils.joinToStringAnd
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
internal const val TO_ARTIFACTORY = "ToArtifactory"

internal const val PLUGIN_MARKER_PUB_SUFFIX = "PluginMarkerMaven"

private const val JB_PUBLISH_TO_MAVEN_LOCAL_TASK = "$JB_TASK_PREFIX$TO_MAVEN_LOCAL"
private const val JB_PUBLISH_TO_MAVEN_REPO_TASK = "$JB_TASK_PREFIX$TO_MAVEN_REPO"
private const val JB_PUBLISH_TO_BINTRAY_TASK = "$JB_TASK_PREFIX$TO_BINTRAY"
private const val JB_PUBLISH_TO_ARTIFACTORY_TASK = "$JB_TASK_PREFIX$TO_ARTIFACTORY"
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
        get() = "Publish all publications to the local Maven Repository"
}

class JbPublishPubToMavenLocalTaskInfo(val pub: JarbirdPub) : TaskInfo {
    override val name: String
        get() = "$JB_TASK_PREFIX${pub.pubNameCap}$TO_MAVEN_LOCAL"
    override val description: String
        get() = if (pub.pom.isGradlePlugin()) {
            val pubDesc = listOf(pub.getGAV(), "plugin marker ${pub.pluginCoordinate()}").joinToStringAnd()
            "Publish $pubDesc to the local Maven Repository"
        } else {
            "Publish ${pub.getGAV()} to the local Maven Repository"
        }
}

class JbPublishToMavenRepoTaskInfo : TaskInfo {
    override val name: String
        get() = JB_PUBLISH_TO_MAVEN_REPO_TASK
    override val description: String
        get() = "Publish all publications to the respective Maven Repositories"
}

class JbPublishToCustomMavenRepoTaskInfo(val repo: RepoSpec) : TaskInfo {
    override val name: String
        get() = "${JB_TASK_PREFIX}To${repo.id}"
    override val description: String
        get() = "Publish Maven publications to '${repo.description}'"
}

class JbPublishPubToMavenRepoTaskInfo(val pub: JarbirdPub) : TaskInfo {

    private val mavenRepos = pub.getRepos().filterIsInstance<MavenRepoSpec>().joinToStringAnd { it.description }

    override val name: String
        get() = "$JB_TASK_PREFIX${pub.pubNameCap}$TO_MAVEN_REPO"
    override val description: String
        get() = if (pub.pom.isGradlePlugin()) {
            val pubDesc = listOf(pub.getGAV(), "plugin marker ${pub.pluginCoordinate()}").joinToStringAnd()
            "Publish $pubDesc to $mavenRepos"
        } else {
            "Publish '${pub.getGAV()}' to all of the respective Maven Repo $mavenRepos"
        }
}

class JbPublishPubToCustomMavenRepoTaskInfo(val pub: JarbirdPub, val repo: RepoSpec) : TaskInfo {
    override val name: String
        get() = "$JB_TASK_PREFIX${pub.pubNameCap}To${repo.id }"
    override val description: String
        get() = if (pub.pom.isGradlePlugin()) {
            val pubDesc = listOf(pub.getGAV(), "plugin marker ${pub.pluginCoordinate()}").joinToStringAnd()
            "Publish $pubDesc to ${repo.description} "
        } else {
            "Publish '${pub.getGAV()}' to ${repo.description}"
        }
}

class JbPublishToGradlePortalTaskInfo(private val pubs: List<JarbirdPub>) : TaskInfo {
    override val name: String
        get() = JB_PUBLISH_TO_GRADLE_PORTAL_TASK
    override val description: String
        get() {
            val pluginPubs = pubs.filter { pub -> pub.getRepos().any { repo -> repo is GradlePortalSpec } }
            val pluginPubsDesc = pluginPubs.flatMap { pub ->
                listOf(pub.getGAV(), "plugin marker ${pub.pluginCoordinate()}")
            }.joinToStringAnd()
            return "Publish $pluginPubsDesc to Gradle plugin portal"
        }
}

class JbPublishPubToGradlePortalTaskInfo(val pub: JarbirdPub) : TaskInfo {
    override val name: String
        get() = "$JB_TASK_PREFIX${pub.pubNameCap}$TO_GRADLE_PORTAL"
    override val description: String
        get() {
            val pubDesc = listOf(pub.getGAV(), "plugin marker ${pub.pluginCoordinate()}").joinToStringAnd()
            return "Publish $pubDesc to Gradle plugin portal"
        }
}

class JbPublishToBintrayTaskInfo(private val publishPlan: BintrayPublishPlan) : TaskInfo {

    override val name: String
        get() = JB_PUBLISH_TO_BINTRAY_TASK
    override val description: String
        get() {
            val bintrayLibsAndPlugins =
                (
                    publishPlan.bintrayLibs.map { it.getGAV() } +
                        publishPlan.bintrayPlugins.map { it.pluginCoordinate() }
                    ).joinToStringAnd()
            val artifactoryLibs =
                (
                    publishPlan.artifactoryLibs.map { it.getGAV() } +
                        publishPlan.artifactoryPlugins.map { "plugin marker ${it.pluginCoordinate()}" }
                    ).joinToStringAnd()

            val desc = StringBuilder()
            if (bintrayLibsAndPlugins != "") {
                desc.append("Publish $bintrayLibsAndPlugins to Bintray. ")
            }
            if (artifactoryLibs != "") {
                desc.append("Publish $artifactoryLibs to OSS Jfrog.")
            }

            return desc.toString().trim()
        }
}

class JbPublishToArtifactoryTaskInfo(private val publishPlan: BintrayPublishPlan) : TaskInfo {

    override val name: String
        get() = JB_PUBLISH_TO_ARTIFACTORY_TASK
    override val description: String
        get() {
            val artifactoryLibs =
                (
                    publishPlan.artifactoryLibs
                        .filter { it.getRepos().all { it !is BintraySnapshotRepoSpec } }
                        .map { it.getGAV() } +
                        publishPlan.artifactoryPlugins
                            .filter { it.getRepos().all { it !is BintraySnapshotRepoSpec } }
                            .map { "plugin marker ${it.pluginCoordinate()}" }
                    ).joinToStringAnd()
            val oSSLibs =
                (
                    publishPlan.artifactoryLibs
                        .filter { it.getRepos().any { it is BintraySnapshotRepoSpec } }
                        .map { it.getGAV() } +
                        publishPlan.artifactoryPlugins
                            .filter { it.getRepos().any { it is BintraySnapshotRepoSpec } }
                            .map { "plugin marker ${it.pluginCoordinate()}" }
                    ).joinToStringAnd()

            val desc = StringBuilder()
            if (artifactoryLibs != "") {
                desc.append("Publish $artifactoryLibs to Artifactory. ")
            }
            if (oSSLibs != "") {
                desc.append("Publish $oSSLibs to OSS Jfrog.")
            }

            return desc.toString().trim()
        }
}

class JbPublishPubTaskInfo(private val pub: JarbirdPub) : TaskInfo {
    override val name: String
        get() = "$JB_TASK_PREFIX${pub.pubNameCap}"
    override val description: String
        get() {
            val repoListStr = pub.getRepos().joinToStringAnd { it.description }

            return if (pub.pom.isGradlePlugin()) {
                "Publish publication '${pub.pubNameWithVariant()}:${pub.variantVersion()}' " +
                    "and plugin '${pub.pom.plugin?.id}:${pub.pom.version}' " +
                    "to $repoListStr"
            } else {
                "Publish publication '${pub.pubNameWithVariant()}' to $repoListStr"
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
        get() = DOCUMENTATION_GROUP
    override val name: String
        get() = "jbDokka${docType}MultiModule"
    override val description: String
        get() = "Generates documentation in 'html' format"
}

class JbDokkaPubTaskInfo(private val pub: JarbirdPub) : TaskInfo {

    private val docType = "Html"

    override val group: String
        get() = DOCUMENTATION_GROUP
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

class ClassesJarTaskInfo(pub: JarbirdPub) : TaskInfo {

    private val sourceSetName = (pub as JarbirdPubImpl).sourceSet?.name

    override val name: String
        get() = "${sourceSetName}Jar"

    override val description: String
        get() = "Compile source set $sourceSetName"
}

val JarbirdPub.publishPubToMavenLocalTask: String
    get() = "$PUBLISH_TASK_PREFIX${pubNameCap}Publication$TO_MAVEN_LOCAL"

val JarbirdPub.publishPluginMarkerPubToMavenLocalTask: String
    get() = "$PUBLISH_TASK_PREFIX${markerPubNameCap}Publication$TO_MAVEN_LOCAL"

fun JarbirdPub.publishPubToCustomMavenRepoTask(repo: RepoSpec) =
    "$PUBLISH_TASK_PREFIX${pubNameCap}PublicationTo${repo.repoName}Repository"

fun JarbirdPub.publishPluginMarkerPubToCustomMavenRepoTask(repo: RepoSpec) =
    "$PUBLISH_TASK_PREFIX${markerPubNameCap}PublicationTo${repo.repoName}Repository"
