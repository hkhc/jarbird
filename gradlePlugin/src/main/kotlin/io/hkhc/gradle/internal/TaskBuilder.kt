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
import io.hkhc.gradle.internal.bintray.ArtifactoryTaskBuilder
import io.hkhc.gradle.internal.maven.MavenTaskBuilder
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

internal class TaskBuilder(
    private val project: Project,
    private val pubs: List<JarbirdPub>
) {

    private val artifactoryTaskBuilder = ArtifactoryTaskBuilder(project, pubs)
    private val mavenTaskBuilder = MavenTaskBuilder(project, pubs)

    private fun TaskContainer.registerGradlePortalTask() {

        val taskInfo = JbPublish.to.gradlePortal.taskInfo

        if (project.isMultiProjectRoot()) {
            project.registerRootProjectTasks(taskInfo)
        } else {
            val pub = pubs.firstOrNull { it.pom.isGradlePlugin() }
            pub?.let {
                val pubTask = JbPublish.pub(pub).to.gradlePortal.taskInfo
                pubTask.register(this) {
                    dependsOn("publishPlugins")
                }
                taskInfo.register(project.tasks) {
                    dependsOn(pubTask.name)
                }
            }
        }
    }

    private fun TaskContainer.registerPublishTask() {

        pubs.forEach { pub ->

            val taskInfo = JbPublish.pub(pub).taskInfo

            if (project.isMultiProjectRoot()) {
                project.registerRootProjectTasks(taskInfo)
            } else {
                taskInfo.register(this) {
                    // TODO depends on jbPublish{pubName}To{mavenLocal}
                    // TODO depends on jbPublish{pubName}To{mavenRepository}
                    dependsOn(JbPublish.pub(pub).to.mavenLocal.taskInfo.name)
                    if (pub.needsNonLocalMaven()) {
                        dependsOn(JbPublish.pub(pub).to.mavenRepo.taskInfo.name)
                    }
                }
            }
        }
    }

//    private fun registerRootTask(project: Project): Task {
//
//        return try {
//            project.tasks.named(JB_TASK_PREFIX).get()
//        } catch (e: UnknownTaskException) {
//            project.tasks.create(JB_TASK_PREFIX) {
//                group = SP_GROUP
//            }
//        }
//    }

    fun registerRootTask(): TaskProvider<Task> {
        return JbPublish.taskInfo.register(project) {
            dependsOn(mavenTaskBuilder.getLocalTaskInfo().name)
            if (pubs.needGradlePlugin()) {
                dependsOn(JbPublish.to.gradlePortal.taskInfo.name)
            }
            if (pubs.needsNonLocalMaven()) {
                dependsOn(JbPublish.to.mavenRepo.taskInfo.name)
//                mavenTaskBuilder.getRepoTaskInfos().forEach {
//                    dependsOn(it.name)
//                }
            }
            if (pubs.needArtifactory()) {
                dependsOn(artifactoryTaskBuilder.getTaskInfo().name)
            }
        }
    }

    fun build() {

        with(project.tasks) {
            registerPublishTask()
            mavenTaskBuilder.also {
                it.registerMavenLocalTask(this)
                it.registerMavenRepositoryTask(this)
            }
            if (pubs.needArtifactory()) {
                artifactoryTaskBuilder.registerArtifactoryTask()
            }
            if (pubs.any { it.pom.isGradlePlugin() }) {
                registerGradlePortalTask()
            }
            registerRootTask()
        }
    }
}
