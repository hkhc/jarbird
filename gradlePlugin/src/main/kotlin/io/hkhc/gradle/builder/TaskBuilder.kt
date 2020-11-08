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

import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.SP_GROUP
import io.hkhc.gradle.isMultiProjectRoot
import io.hkhc.gradle.utils.LOG_PREFIX
import org.gradle.api.Project
import org.gradle.api.tasks.TaskContainer

class TaskBuilder(
    private val project: Project,
    private val pubs: List<JarbirdPub>
) {

    private val mavenLocal = "MavenLocal"

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

    private fun getPubNameCap(pub: JarbirdPub) = pub.pubNameWithVariant().capitalize()
    private fun getPubId(pub: JarbirdPub) = "${getPubNameCap(pub)}Publication"
    private fun getMarkerPubId(pub: JarbirdPub) = "${getPubNameCap(pub)}PluginMarkerMavenPublication"
    private fun getMavenRepo(pub: JarbirdPub) = "Maven${getPubNameCap(pub)}Repository"

//    private val pubName: String
//        get() = pub.pubNameWithVariant()
//    private val pubNameCap: String
//        get() = pubName.capitalize()
//    private val pubId: String
//        get() = "${pubNameCap}Publication"
//    private val markerPubId: String
//        get() = "${pubNameCap}PluginMarkerMavenPublication"
//    private val mavenRepo: String
//        get() = "Maven${pubNameCap}Repository"

    private fun TaskContainer.registerMavenLocalTask() {

        if (project.isMultiProjectRoot()) {
            registerRootProjectTasks("jbPublishTo$mavenLocal")
        } else {

            val jbPublishToMavenLocal = register("jbPublishTo$mavenLocal") {
                group = SP_GROUP
                description = "Publish"
            }.get()

            pubs.forEach { pub ->

                val publishTask = "jbPublish${getPubNameCap(pub)}To$mavenLocal"

                register(publishTask) {
                    group = SP_GROUP

                    description = if (pub.pom.isGradlePlugin()) {
                        "Publish Maven publication '${pub.pubNameWithVariant()}:${pub.variantVersion()}' " +
                            "and plugin '${pub.pom.plugin?.id}:${pub.variantVersion()}' to the local Maven Repository"
                    } else {
                        "Publish Maven publication '${pub.pubNameWithVariant()}' to the local Maven Repository"
                    }

                    dependsOn("publish${getPubId(pub).capitalize()}To$mavenLocal")

                    if (pub.pom.isGradlePlugin()) {
                        dependsOn("publish${getMarkerPubId(pub).capitalize()}To$mavenLocal")
                    }
                }

                jbPublishToMavenLocal.dependsOn(publishTask)
            }
        }
    }

    private fun TaskContainer.registerMavenRepositoryTask() {

        if (project.isMultiProjectRoot()) {
            // TODO we shall check POM group that pubName is not duplicated among variants.
            pubs.forEach { pub ->
                registerRootProjectTasks("jbPublishTo${getMavenRepo(pub)}")
            }
        } else {

            val jbPublishToMavenRepository = register("jbPublishToMavenRepository") {
                group = SP_GROUP
            }.get()

            pubs.forEach { pub ->
                pub.mavenRepository?.let { repo ->
                    register("jbPublish${getPubNameCap(pub)}To${repo.name}") {
                        group = SP_GROUP

                        // I don't know why the maven repository name in the task name is not capitalized

                        description = if (pub.pom.isGradlePlugin()) {
                            "Publish Maven publication '${pub.pubNameWithVariant()}:${pub.variantVersion()}' " +
                                "and plugin '${pub.pom.plugin?.id}:${pub.pom.version}' " +
                                "to the 'Maven${getPubNameCap(pub)}' Repository"
                        } else {
                            // TODO 'Maven${getPubNameCap(pub)}' is wrong
                            "Publish Maven publication '${pub.pubNameWithVariant()}:${pub.variantVersion()}' " +
                                "to the 'Maven${getPubNameCap(pub)}' Repository"
                        }

                        dependsOn("publish${getPubId(pub)}To${getMavenRepo(pub)}")

                        if (pub.pom.isGradlePlugin()) {
                            dependsOn("publish${getMarkerPubId(pub)}To${getMavenRepo(pub)}")
                        }
                    }.get()

                    register("jbPublish${getPubNameCap(pub)}ToMavenRepository") {
                        group = SP_GROUP
                        dependsOn("jbPublish${getPubNameCap(pub)}To${repo.name}")
                    }

                    jbPublishToMavenRepository.dependsOn("jbPublish${getPubNameCap(pub)}ToMavenRepository")
                }
            }
        }
    }

    private fun publishingSupported(pub: JarbirdPub): Boolean {
        val notSupport = pub.pom.isSnapshot() && pub.bintray && pub.pom.isGradlePlugin()
        if (notSupport) {
            project.logger.warn(
                "WARNING: $LOG_PREFIX Publish snapshot Gradle Plugin to Bintray/OSSArtifactory is not supported."
            )
        }
        return !notSupport
    }

    private fun TaskContainer.registerBintrayTask() {

        if (project.isMultiProjectRoot()) {
            registerRootProjectTasks("jbPublishToBintray")
        } else {

            pubs.forEach { pub ->

                if (publishingSupported(pub)) {

                    register("jbPublishToBintray") {
                        group = SP_GROUP

                        val target = if (pub.pom.isSnapshot()) "OSS JFrog" else "Bintray"

                        description = if (pub.pom.isGradlePlugin()) {
                            "Publish Maven publication '${getPubNameCap(pub)}' " +
                                "and plugin '${pub.pom.plugin?.id}:${pub.variantVersion()}' to $target"
                        } else {
                            "Publish Maven publication '${getPubNameCap(pub)}' to $target"
                        }

                        /*
                            bintray repository does not allow publishing SNAPSHOT artifacts, it has to be published
                            to the OSS JFrog repository
                         */
                        if (pub.pom.isSnapshot()) {
                            dependsOn("artifactoryPublish")
                        } else {
                            dependsOn("bintrayUpload")
                        }
                    }
                }
            }
        }
    }

    private fun TaskContainer.registerGradlePortalTask() {

        if (project.isMultiProjectRoot()) {
            registerRootProjectTasks("jbPublishToGradlePortal")
        } else {
            val pub = pubs.firstOrNull { it.pom.isGradlePlugin() }
            val pom = pub?.pom
            pom?.let {
                register("jbPublishToGradlePortal") {
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
            registerRootProjectTasks("jbPublish")
        } else {

            val jbPublish = register("jbPublish") {
                group = SP_GROUP
            }.get()

            pubs.forEach { pub ->

                register("jbPublish${getPubNameCap(pub)}") {
                    group = SP_GROUP

                    // assemble a list of repositories
                    val repoList = mutableListOf<String>()
                    repoList.add("Maven Local")
                    repoList.add("'Maven${getPubNameCap(pub)}' Repository")
                    if (pub.bintray) {
                        repoList.add("Bintray")
                    }
                    if (pub.pom.isGradlePlugin()) {
                        repoList.add("Gradle Plugin Portal")
                    }
                    val repoListStr = repoList.joinToString()

                    description = if (pub.pom.isGradlePlugin()) {
                        "Publish Maven publication '${getPubNameCap(pub)}:${pub.variantVersion()}' " +
                            "and plugin '${pub.pom.plugin?.id}:${pub.pom.version}'" +
                            "to $repoListStr"
                    } else {
                        "Publish Maven publication '${getPubNameCap(pub)}' to $repoListStr"
                    }

                    // TODO depends on jbPublish{pubName}To{mavenLocal}
                    // TODO depends on jbPublish{pubName}To{mavenRepository}
                    dependsOn("jbPublish${getPubNameCap(pub)}To$mavenLocal")
                    dependsOn("jbPublish${getPubNameCap(pub)}ToMavenRepository")
                }

                jbPublish.dependsOn("jbPublish${getPubNameCap(pub)}")
            }

            if (findByName("jbPublishToBintray") != null) {
                jbPublish.dependsOn("jbPublishToBintray")
            }
            if (findByName("jbPublishToGradlePortal") != null) {
                jbPublish.dependsOn("jbPublishToGradlePortal")
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
