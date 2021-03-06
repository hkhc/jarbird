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

import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.internal.JbPublishToBintrayTaskInfo
import io.hkhc.gradle.internal.LOG_PREFIX
import io.hkhc.gradle.internal.TaskInfo
import io.hkhc.gradle.internal.isMultiProjectRoot
import io.hkhc.gradle.internal.needBintray
import io.hkhc.gradle.internal.registerRootProjectTasks
import org.gradle.api.Project

internal const val BINTRAY_UPLOAD_TASK = "bintrayUpload"
internal const val ARTIFACTORY_PUBLISH_TASK = "artifactoryPublish"

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
        if (pubs.needBintray() && publishPlan.invalidPlugins.isNotEmpty()) {
            project.logger.warn(
                "WARNING: $LOG_PREFIX Publish snapshot Gradle Plugin to Bintray/OSSArtifactory is not supported."
            )
        }

        if (publishPlan.bintray.isNotEmpty() || publishPlan.artifactory.isNotEmpty()) {

            taskInfo.register(project.tasks) {
                /*
                    bintray repository does not allow publishing SNAPSHOT artifacts, it has to be published
                    to the OSS JFrog repository
                 */

                if (publishPlan.artifactory.isNotEmpty()) {
                    dependsOn(ARTIFACTORY_PUBLISH_TASK)
                }

                if (publishPlan.bintray.isNotEmpty()) {
                    dependsOn(BINTRAY_UPLOAD_TASK)
                }
            }
        }
    }
}
