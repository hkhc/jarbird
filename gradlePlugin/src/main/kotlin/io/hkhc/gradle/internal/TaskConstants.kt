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
import io.hkhc.gradle.RepoSpec
import io.hkhc.gradle.internal.bintray.BintrayPublishPlan
import io.hkhc.gradle.internal.repo.GradlePortalSpecImpl
import io.hkhc.gradle.internal.repo.MavenCentralRepoSpec
import io.hkhc.gradle.internal.repo.MavenLocalRepoSpecImpl
import io.hkhc.gradle.internal.repo.MavenRepoSpec
import io.hkhc.utils.joinToStringAnd
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

internal const val JB_PUBLISH_TASK_PREFIX = "jbPublish"
internal const val MAVEN_LOCAL_CAP = "MavenLocal"

internal const val TO_ARTIFACTORY = "ToArtifactory"

internal const val PLUGIN_MARKER_PUB_SUFFIX = "PluginMarkerMaven"

private const val JB_PUBLISH_TO_ARTIFACTORY_TASK = "$JB_PUBLISH_TASK_PREFIX$TO_ARTIFACTORY"

abstract class TaskInfo {

    companion object {
        var eagar = false
    }

    open val group: String
        get() = SP_GROUP
    abstract val name: String
    abstract val description: String

    override fun equals(o: Any?): Boolean {
        return o?.let { other0 ->
            if (other0 is TaskInfo) {
                group == other0.group && name == other0.name && description == other0.description
            } else {
                false
            }
        } ?: false
    }

    override fun hashCode(): Int {
        return "$group$name$description".hashCode()
    }

    override fun toString(): String {
        return "TaskInfo(${super.toString()},group=$group,name=$name,description=$description)"
    }

    fun register(project: Project, block: Task.() -> Unit = {}): TaskProvider<Task> {
        return project.tasks.register(name) {
            val task = this
            task.group = this@TaskInfo.group
            task.description = this@TaskInfo.description
            block.invoke(task)
        }.apply {
            if (eagar) get()
        }
    }

    fun register(container: TaskContainer, block: Task.() -> Unit = {}): TaskProvider<Task> {
        return container.register(name) {
            val task = this
            task.group = this@TaskInfo.group
            task.description = this@TaskInfo.description
            block.invoke(task)
        }.apply {
            if (eagar) get()
        }
    }

    fun <T : Task> register(container: TaskContainer, type: Class<T>, block: T.() -> Unit = {}): TaskProvider<T> {
        return container.register(name, type) {
            val task = this
            task.group = this@TaskInfo.group
            task.description = this@TaskInfo.description
            block.invoke(task)
        }.apply {
            if (eagar) get()
        }
    }
}

object Publish {

    private const val PUBLISH_TASK_PREFIX = "publish"
    private const val taskNamePrefix = PUBLISH_TASK_PREFIX

    fun pub(pub: JarbirdPub) = Pub(taskNamePrefix, pub)
    fun pluginMarker(pub: JarbirdPub) = PluginMarker(taskNamePrefix, pub)

    class Pub(prefix: String, pub: JarbirdPub) {
        val taskName = "$prefix${pub.pubNameCap}Publication"
        val to = To(taskName)
    }

    class PluginMarker(prefix: String, pub: JarbirdPub) {
        val taskName = "$prefix${pub.markerPubNameCap}Publication"
        val to = To(taskName)
    }

    class To(private val prefix: String) {
        val mavenLocal = MavenLocalTask("${prefix}To")
        fun mavenRepo(repo: RepoSpec) = MavenRepoTask("${prefix}To", repo)
    }

    class MavenLocalTask(prefix: String) {
        val taskName = "$prefix$MAVEN_LOCAL_CAP"
    }

    class MavenRepoTask(prefix: String, repo: RepoSpec) {
        val taskName = "$prefix${repo.repoName.capitalize()}Repository"
    }

    const val taskName = PUBLISH_TASK_PREFIX

    val to = To(taskName)
}

object JbPublish {

    class SimpleTaskInfo(nameSource: () -> String, descriptionSource: () -> String) : TaskInfo() {
        constructor(n: String, d: String) : this({ n }, { d })

        override val name = nameSource()
        override val description = descriptionSource()
        fun append(another: TaskInfo) = newInfo(
            "$name${another.name.capitalize()}",
            "$description${if (another.description != "") " " else ""}${another.description}"
        )
    }

    private fun newInfo(n: String, d: String) = SimpleTaskInfo(n, d)
    private fun newInfo(n: () -> String, d: () -> String) = SimpleTaskInfo(n, d)

    private val initialInfo = newInfo(JB_PUBLISH_TASK_PREFIX, "Publish")

    fun pub(pub: JarbirdPub) = Pub(initialInfo, pub)

    val to = To(null, initialInfo.append(newInfo("", "")))

    class Pub(prefixTaskInfo: SimpleTaskInfo, pub: JarbirdPub) {
        private var variantWithBracket = if (pub.variant == "") "" else " (${pub.variant})"
        val taskInfo = prefixTaskInfo.append(
            newInfo(
                pub.pubNameCap,
                "module '${pub.pubName}'$variantWithBracket to all targeted repositories"
            )
        )
        val to = To(
            pub,
            prefixTaskInfo.append(
                newInfo(
                    pub.pubNameCap,
                    "module '${pub.pubName}'$variantWithBracket"
                )
            )
        )
    }

    class To(private val pub: JarbirdPub? = null, private val prefixInfo: SimpleTaskInfo) {
        private val toInfo = newInfo("To", "to")
        val mavenLocal = MavenLocalTask(prefixInfo.append(toInfo))
        fun mavenRepo(spec: MavenRepoSpec) = MavenRepoTask(prefixInfo.append(toInfo), spec)
        val mavenRepo = AllMavenRepoTask(prefixInfo.append(toInfo))
        fun mavenCentral(): MavenRepoTask {
            val mavenCentralRepoSpec = pub?.let { it.getRepos().filterIsInstance<MavenCentralRepoSpec>()[0] }
            if (mavenCentralRepoSpec == null)
                throw GradleException("No MavenCentral repository is declared")
            else
                return MavenRepoTask(
                    prefixInfo.append(toInfo),
                    mavenCentralRepoSpec!!
                )
        }

        var gradlePortal = GradlePortalTask(prefixInfo.append(toInfo))
    }

    class MavenLocalTask(prefixInfo: SimpleTaskInfo) {
        private val spec = MavenLocalRepoSpecImpl()
        val taskInfo = prefixInfo.append(newInfo(spec.repoName, spec.description))
    }

    class MavenRepoTask(prefixInfo: SimpleTaskInfo, spec: RepoSpec) {
        val taskInfo = prefixInfo.append(newInfo(spec.repoName, spec.description))
    }

    class AllMavenRepoTask(prefixInfo: SimpleTaskInfo) {
        val taskInfo = prefixInfo.append(newInfo("MavenRepositories", "all Maven repositories"))
    }

    class GradlePortalTask(prefixInfo: SimpleTaskInfo) {
        private val spec = GradlePortalSpecImpl()
        val taskInfo = prefixInfo.append(newInfo(spec.id, spec.description))
    }

    val taskInfo = initialInfo
}

class JbPublishToArtifactoryTaskInfo(private val publishPlan: BintrayPublishPlan) : TaskInfo() {

    override val name: String
        get() = JB_PUBLISH_TO_ARTIFACTORY_TASK
    override val description: String
        get() {
            val artifactoryLibs =
                (
                    publishPlan.artifactoryLibs
                        .map { it.getGAV() } +
                        publishPlan.artifactoryPlugins
                            .map { "plugin marker ${it.pluginCoordinate()}" }
                    ).joinToStringAnd()

            val desc = StringBuilder()
            if (artifactoryLibs != "") {
                desc.append("Publish $artifactoryLibs to Artifactory. ")
            }
            return desc.toString().trim()
        }
}

class SourceJarPubTaskInfo(private val pub: JarbirdPub) : TaskInfo() {
    override val group: String
        get() = PUBLISH_GROUP
    override val name: String
        get() = "sourcesJar${pub.pubNameCap}"
    override val description: String
        get() = if (pub.variant == "") {
            "Create archive of source code for the binary"
        } else {
            "Create archive of source code for the binary of variant '${pub.variant}' "
        }
}

class JbDokkaTaskInfo : TaskInfo() {

    private val docType = "Html"

    override val group: String
        get() = DOCUMENTATION_GROUP
    override val name: String
        get() = "jbDokka${docType}MultiModule"
    override val description: String
        get() = "Generates documentation in 'html' format"
}

class JbDokkaPubTaskInfo(private val pub: JarbirdPub) : TaskInfo() {

    private val docType = "Html"

    override val group: String
        get() = DOCUMENTATION_GROUP
    override val name: String
        get() = "jbDokka${docType}${pub.pubNameCap}"

    override val description: String
        get() = "Generates documentation in 'html' format for publication ${pub.pubName}"
}

class DokkaJarPubTaskInfo(private val pub: JarbirdPub) : TaskInfo() {

    override val group: String
        get() = "documentation"
    override val name: String
        get() = "jbDokkaJar${pub.pubNameCap}"

    override val description: String
        get() = "Assembles Kotlin docs with Dokka to Jar"
}

class ClassesJarTaskInfo(pub: JarbirdPub) : TaskInfo() {

    private val sourceSetName = (pub as JarbirdPubImpl).mSourceSet?.name

    override val name: String
        get() = "${sourceSetName}Jar"

    override val description: String
        get() = "Compile source set $sourceSetName"
}
