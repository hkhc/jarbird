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
import io.hkhc.gradle.pom.Pom
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

            pubs.forEach { pub ->

                val publishTask = "jbPublish${getPubNameCap(pub)}To$mavenLocal"

                register(publishTask) {
                    group = SP_GROUP

                    description = if (pub.pom.isGradlePlugin()) {
                        "Publish Maven publication '${pub.pubNameWithVariant()}' " +
                            "and plugin '${pub.pom.plugin?.id}' version '${pub.pom.version}' to the local Maven Repository"
                    } else {
                        "Publish Maven publication '${pub.pubNameWithVariant()}' to the local Maven Repository"
                    }

                    dependsOn("publish${getPubId(pub).capitalize()}To$mavenLocal")

                    if (pub.pom.isGradlePlugin()) {
                        dependsOn("publish${getMarkerPubId(pub).capitalize()}To$mavenLocal")
                    }
                }

                register("jbPublishTo$mavenLocal") {
                    dependsOn(publishTask)
                }
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

            pubs.forEach { pub ->
                register("jbPublish${getPubNameCap(pub)}ToMavenRepository") {
                    group = SP_GROUP

                    // I don't know why the maven repository name in the task name is not capitalized

                    description = if (pub.pom.isGradlePlugin()) {
                        "Publish Maven publication '${pub.pubNameWithVariant()}' " +
                            "and plugin '${pub.pom.plugin?.id}' version '${pub.pom.version}' to the 'Maven${getPubNameCap(pub)}' Repository"
                    } else {
                        "Publish Maven publication '${pub.pubNameWithVariant()}' to the 'Maven${getPubNameCap(pub)}' Repository"
                    }

                    dependsOn("publish${getPubId(pub)}To${getMavenRepo(pub)}")

                    if (pub.pom.isGradlePlugin()) {
                        dependsOn("publish${getMarkerPubId(pub)}To${getMavenRepo(pub)}")
                    }
                }

                register("jbPublishToMavenRepository"){
                    dependsOn("jbPublish${getPubNameCap(pub)}ToMavenRepository")
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
                            "Publish Maven publication '$${getPubNameCap(pub)}' " +
                                "and plugin '${pub.pom.plugin?.id}' version '${pub.pom.version}' to $target"
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
            val pom = pubs.first {it.pom.isGradlePlugin()}?.pom
            register("jbPublishToGradlePortal") {
                group = SP_GROUP
                // TODO fix description for multiple pom
                description = "Publish plugin '${pom.plugin?.id}' version '${pom.version}' to the Gradle plugin portal"
                dependsOn("publishPlugins")
            }
        }
    }

    private fun TaskContainer.registerPublishTask() {

        if (project.isMultiProjectRoot()) {
            registerRootProjectTasks("jbPublish")
        } else {

            pubs.forEach { pub ->

                register("jbPublish${getPubNameCap(pub)}") {
                    group = SP_GROUP

                    // assemble a list of repositories
                    val repoList = mutableListOf<String>()
                    repoList.add("Maven Local")
                    repoList.add("'Maven$${getPubNameCap(pub)}' Repository")
                    if (pub.bintray) {
                        repoList.add("Bintray")
                    }
                    if (pub.pom.isGradlePlugin()) {
                        repoList.add("Gradle Plugin Portal")
                    }
                    val repoListStr = repoList.joinToString()

                    description = if (pub.pom.isGradlePlugin()) {
                        "Publish Maven publication '${getPubNameCap(pub)}' and plugin '${pub.pom.plugin?.id}' version '${pub.pom.version}' " +
                            "to $repoListStr"
                    } else {
                        "Publish Maven publication '${getPubNameCap(pub)}' to $repoListStr"
                    }

                    dependsOn("jbPublishTo$mavenLocal")
                    dependsOn("jbPublishToMavenRepository")
                    if (pub.bintray && !(pub.pom.isGradlePlugin() && pub.pom.isSnapshot())) {
                        dependsOn("jbPublishToBintray")
                    }
                    if (pub.pom.isGradlePlugin()) {
                        dependsOn("jbPublishToGradlePortal")
                    }
                }

                register("jbPublish") {
                    dependsOn("jbPublish${getPubNameCap(pub)}")
                }
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
