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

import io.hkhc.gradle.internal.JarbirdPubImpl
import io.hkhc.gradle.internal.SP_GROUP
import io.hkhc.gradle.internal.isMultiProjectRoot
import io.hkhc.gradle.internal.pubNameCap
import io.hkhc.gradle.internal.pubNameWithVariant
import org.gradle.api.Project
import org.gradle.api.tasks.TaskContainer

internal class TaskBuilder(
    private val project: Project,
    private val pubs: List<JarbirdPubImpl>
) {

    private fun registerRootProjectTasks(taskPath: String) {

        project.tasks.register(taskPath) {
            group = SP_GROUP
            project.childProjects.forEach { (_, child) ->
                val rootTask = this
                child.tasks.findByPath(taskPath)?.let { childTask ->
                    rootTask.dependsOn(childTask.path)
                }
            }
        }
    }

    private fun TaskContainer.registerMavenLocalTask() {

        if (project.isMultiProjectRoot()) {
            registerRootProjectTasks(JB_PUBLISH_TO_MAVEN_LOCAL_TASK)
        } else {

            val jbPublishToMavenLocal = register(JB_PUBLISH_TO_MAVEN_LOCAL_TASK) {
                group = SP_GROUP
                description = "Publish"
            }.get()

            pubs.forEach { pub ->

                val publishTask = pub.jbPublishPubToMavenLocalTask

                register(publishTask) {
                    group = SP_GROUP

                    description = if (pub.pom.isGradlePlugin()) {
                        "Publish Maven publication '${pub.pubNameWithVariant()}:${pub.variantVersion()}' " +
                            "and plugin '${pub.pom.plugin?.id}:${pub.variantVersion()}' to the local Maven Repository"
                    } else {
                        "Publish Maven publication '${pub.pubNameWithVariant()}' to the local Maven Repository"
                    }

                    dependsOn(pub.publishPubToMavenLocalTask)

                    if (pub.pom.isGradlePlugin()) {
                        dependsOn(pub.publishPluginMarkerPubToMavenLocalTask)
                    }
                }

                jbPublishToMavenLocal.dependsOn(publishTask)
            }
        }
    }

    private fun TaskContainer.registerMavenRepositoryTask() {

        if (project.isMultiProjectRoot()) {
            // TODO we shall check POM group that pubName is not duplicated among variants.
            pubs.filter { it.maven }.forEach { pub ->
                registerRootProjectTasks(pub.jbPublishToCustomMavenRepoTask)
            }
        } else if (pubs.any { it.maven }) {

            val jbPublishToMavenRepo = register(JB_PUBLISH_TO_MAVEN_REPO_TASK) {
                group = SP_GROUP
                // TODO description
                description = "TODO..."
            }.get()

            pubs.forEach { pub ->
                pub.mavenRepo.also {
                    // TODO shall repo.name be capitalized?
                    register(pub.jbPublishPubToCustomMavenRepoTask) {
                        group = SP_GROUP

                        // I don't know why the maven repository name in the task name is not capitalized

                        description = if (pub.pom.isGradlePlugin()) {
                            "Publish Maven publication '${pub.pubNameWithVariant()}:${pub.variantVersion()}' " +
                                "and plugin '${pub.pom.plugin?.id}:${pub.pom.version}' " +
                                "to the 'Maven${pub.pubNameCap}' Repository"
                        } else {
                            // TODO 'Maven${pub.pubNameCap}' is wrong
                            "Publish Maven publication '${pub.pubNameWithVariant()}:${pub.variantVersion()}' " +
                                "to the 'Maven${pub.pubNameCap}' Repository"
                        }

                        dependsOn(pub.publishPubToCustomMavenRepoTask)

                        if (pub.pom.isGradlePlugin()) {
                            dependsOn(pub.publishPluginMarkerPubToCustomMavenRepoTask)
                        }
                    }.get()

                    register(pub.jbPublishPubToMavenRepoTask) {
                        group = SP_GROUP
                        // TODO description
                        description = "TODO..."
                        dependsOn(pub.jbPublishPubToCustomMavenRepoTask)
                    }

                    jbPublishToMavenRepo.dependsOn(pub.jbPublishPubToMavenRepoTask)
                }
            }
        }
    }

    private fun TaskContainer.registerBintrayTask() {

        if (project.isMultiProjectRoot()) {
            registerRootProjectTasks(JB_PUBLISH_TO_BINTRAY_TASK)
        } else {
            BintrayTaskBuilder(project, pubs).registerBintrayTask(this)
        }
    }

    private fun TaskContainer.registerGradlePortalTask() {

        if (project.isMultiProjectRoot()) {
            registerRootProjectTasks(JB_PUBLISH_TO_GRADLE_PORTAL_TASK)
        } else {
            val pub = pubs.firstOrNull { it.pom.isGradlePlugin() }
            val pom = pub?.pom
            pom?.let {
                register(JB_PUBLISH_TO_GRADLE_PORTAL_TASK) {
                    group = SP_GROUP
                    // TODO fix description for multiple pom
                    description = "Publish plugin '${it.plugin?.id}:${pub.variantVersion()}' " +
                        "to the Gradle plugin portal"
                    dependsOn("publishPlugins")
                }
            }
        }
    }

    private fun TaskContainer.registerPublishTask() {

        if (project.isMultiProjectRoot()) {
            registerRootProjectTasks(JB_TASK_PREFIX)
        } else {

            val jbPublish = register(JB_TASK_PREFIX) {
                group = SP_GROUP
            }.get()

            pubs.forEach { pub ->

                register(pub.jbPublishPubTask) {
                    group = SP_GROUP

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

                    description = if (pub.pom.isGradlePlugin()) {
                        "Publish Maven publication '${pub.pubNameWithVariant()}:${pub.variantVersion()}' " +
                            "and plugin '${pub.pom.plugin?.id}:${pub.pom.version}'" +
                            "to $repoListStr"
                    } else {
                        "Publish Maven publication '${pub.pubNameWithVariant()}' to $repoListStr"
                    }

                    // TODO depends on jbPublish{pubName}To{mavenLocal}
                    // TODO depends on jbPublish{pubName}To{mavenRepository}
                    dependsOn(pub.jbPublishPubToMavenLocalTask)
                    if (pub.maven) {
                        dependsOn(pub.jbPublishPubToMavenRepoTask)
                    }
                }

                jbPublish.dependsOn(pub.jbPublishPubTask)
            }

            if (findByName(JB_PUBLISH_TO_BINTRAY_TASK) != null) {
                jbPublish.dependsOn(JB_PUBLISH_TO_BINTRAY_TASK)
            }
            if (findByName(JB_PUBLISH_TO_GRADLE_PORTAL_TASK) != null) {
                jbPublish.dependsOn(JB_PUBLISH_TO_GRADLE_PORTAL_TASK)
            }
        }
    }

    fun build() {

        with(project.tasks) {
            registerMavenLocalTask()
            registerMavenRepositoryTask()
            if (pubs.any { it.bintray }) {
                registerBintrayTask()
            }
            if (pubs.any { it.pom.isGradlePlugin() }) {
                registerGradlePortalTask()
            }
            registerPublishTask()
        }
    }
}
