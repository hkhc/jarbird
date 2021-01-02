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

package io.hkhc.gradle.internal.bintray

import io.hkhc.gradle.internal.repo.BintrayRepoSpec
import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.internal.JbPublishToArtifactoryTaskInfo
import io.hkhc.gradle.internal.JbPublishToBintrayTaskInfo
import io.hkhc.gradle.internal.LOG_PREFIX
import io.hkhc.gradle.internal.TaskInfo
import io.hkhc.gradle.internal.isMultiProjectRoot
import io.hkhc.gradle.internal.needsArtifactory
import io.hkhc.gradle.internal.needsBintray
import io.hkhc.gradle.internal.registerRootProjectTasks
import org.gradle.api.Project

internal const val BINTRAY_UPLOAD_TASK = "bintrayUpload"
// internal const val ARTIFACTORY_PUBLISH_TASK = "artifactoryPublish"

class BintrayTaskBuilder(
    private val project: Project,
    private val pubs: List<JarbirdPub>
) {

    val publishPlan = BintrayPublishPlan(pubs)

    private val taskInfo: TaskInfo = JbPublishToBintrayTaskInfo(publishPlan)

    fun getTaskInfo() = taskInfo

    fun registerBintrayTask() {

        if (project.isMultiProjectRoot()) {
            project.registerRootProjectTasks(taskInfo)
        } else {
            registerLeafBintrayTask()
        }
    }

    private fun registerLeafBintrayTask() {

        // no need to show message if we do not enable Bintray publishing
        if (pubs.needsBintray() && publishPlan.invalidPlugins.isNotEmpty()) {
            project.logger.warn(
                "WARNING: $LOG_PREFIX Publish snapshot Gradle Plugin to Bintray/OSSArtifactory is not supported."
            )
        }

        println("bintray is not empty ${publishPlan.bintray.isNotEmpty()}")
        println("artifactory is not empty ${publishPlan.artifactory.isNotEmpty()}")

        if (pubs.needsBintray()) {

            taskInfo.register(project.tasks) {
                /*
                    bintray repository does not allow publishing SNAPSHOT artifacts, it has to be published
                    to the OSS JFrog repository
                 */

                if (pubs.needsArtifactory()) {
                    dependsOn(JbPublishToArtifactoryTaskInfo(publishPlan).name)
                }

                if (pubs.any { !it.pom.isSnapshot() && it.getRepos().any { repo -> repo is BintrayRepoSpec } }) {
                    dependsOn(BINTRAY_UPLOAD_TASK)
                }
            }
        }
    }
}
