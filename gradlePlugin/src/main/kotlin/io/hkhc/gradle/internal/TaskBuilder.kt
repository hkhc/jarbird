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
import io.hkhc.gradle.internal.bintray.BintrayTaskBuilder
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

internal class TaskBuilder(
    private val project: Project,
    private val pubs: List<JarbirdPub>
) {

    private fun TaskContainer.registerGradlePortalTask() {

        if (project.isMultiProjectRoot()) {
            project.registerRootProjectTasks(JbPublishToGradlePortalTaskInfo().name)
        } else {

            val parentTask = JbPublishToGradlePortalTaskInfo().register(project.tasks).get()

            val pub = pubs.firstOrNull { it.pom.isGradlePlugin() }
            val pom = pub?.pom
            pom?.let {
                val task = JbPublishPubToGradlePortalTaskInfo(pub).register(this) {
                    dependsOn("publishPlugins")
                }
                parentTask.dependsOn(task)
            }
        }
    }

    private fun TaskContainer.registerPublishTask() {

        if (project.isMultiProjectRoot()) {
            pubs.forEach { pub ->
                project.registerRootProjectTasks(JbPublishPubTaskInfo(pub).name)
            }
            project.registerRootProjectTasks(JbPublishToBintrayTaskInfo(BintrayPublishPlan(pubs)).name)
        } else {

            pubs.forEach { pub ->

                val publishPubTask = JbPublishPubTaskInfo(pub).register(this) {
                    // TODO depends on jbPublish{pubName}To{mavenLocal}
                    // TODO depends on jbPublish{pubName}To{mavenRepository}
                    dependsOn(JbPublishPubToMavenLocalTaskInfo(pub).name)
                    if (pub.maven) {
                        dependsOn(JbPublishPubToMavenRepoTaskInfo(pub).name)
                    }
                }
//                jbPublish.dependsOn(publishPubTask)
            }

            findByName(JbPublishToBintrayTaskInfo(BintrayPublishPlan(pubs)).name)?.let {
//                jbPublish.dependsOn(it)
            }
            findByName(JB_PUBLISH_TO_GRADLE_PORTAL_TASK)?.let {
//                jbPublish.dependsOn(it)
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

    fun build() {

        println("TaskBuilder build")

//        with(project.tasks) {
//            MavenTaskBuilder(project, pubs).also {
//                it.registerMavenLocalTask(this)
//                it.registerMavenRepositoryTask(this)
//            }
//            if (pubs.any { it.bintray }) {
//                BintrayTaskBuilder(project, pubs).registerBintrayTask()
//            }
//            if (pubs.any { it.pom.isGradlePlugin() }) {
//                registerGradlePortalTask()
//            }
//            registerPublishTask()
//        }
    }
}
