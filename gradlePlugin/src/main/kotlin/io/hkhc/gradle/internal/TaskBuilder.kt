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
import io.hkhc.gradle.internal.repo.ArtifactoryRepoSpec
import io.hkhc.gradle.internal.repo.MavenRepoSpec
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

    fun registerGradlePortalTask(container: TaskContainer) {

        val taskInfo = JbPublish.to.gradlePortal.taskInfo

        val pub = pubs.firstOrNull { it.pom.isGradlePlugin() }
        pub?.let {
            val pubTask = JbPublish.pub(pub).to.gradlePortal.taskInfo
            pubTask.register(container) {
                dependsOn("publishPlugins")
            }
            taskInfo.register(container) {
                dependsOn(pubTask.name)
            }
        }
    }

    fun TaskContainer.registerPublishTask() {

        pubs.forEach { pub ->

            val taskInfo = JbPublish.pub(pub).taskInfo

            taskInfo.register(this) {
                dependsOn(JbPublish.pub(pub).to.mavenLocal.taskInfo.name)
                if (pub.needsReposWithType<MavenRepoSpec>()) {
                    dependsOn(JbPublish.pub(pub).to.mavenRepo.taskInfo.name)
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
            if (pubs.needReposWithType<MavenRepoSpec>()) {
                dependsOn(JbPublish.to.mavenRepo.taskInfo.name)
            }
            if (pubs.needReposWithType<ArtifactoryRepoSpec>()) {
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
            if (pubs.needReposWithType<ArtifactoryRepoSpec>()) {
                artifactoryTaskBuilder.registerArtifactoryTask()
            }
            if (pubs.needGradlePlugin()) {
                registerGradlePortalTask(this)
            }
            registerRootTask()
        }
    }
}
