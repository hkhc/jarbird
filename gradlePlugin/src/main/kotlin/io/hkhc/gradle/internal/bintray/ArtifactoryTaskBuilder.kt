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

package io.hkhc.gradle.internal.bintray

import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.internal.JbPublishToArtifactoryTaskInfo
import io.hkhc.gradle.internal.TaskInfo
import io.hkhc.gradle.internal.needReposWithType
import io.hkhc.gradle.internal.repo.ArtifactoryRepoSpec
import org.gradle.api.Project

// internal const val BINTRAY_UPLOAD_TASK = "bintrayUpload"
internal const val ARTIFACTORY_PUBLISH_TASK = "artifactoryPublish"

class ArtifactoryTaskBuilder(
    private val project: Project,
    private val pubs: List<JarbirdPub>
) {

    val publishPlan = BintrayPublishPlan(pubs)

    private val taskInfo: TaskInfo = JbPublishToArtifactoryTaskInfo(publishPlan)

    fun getTaskInfo() = taskInfo

    fun registerArtifactoryTask() {
        registerLeafArtifactoryTask()
    }

    private fun registerLeafArtifactoryTask() {

        println("registerLeafArtifactoryTask")

        if (pubs.needReposWithType<ArtifactoryRepoSpec>()) {

            taskInfo.register(project.tasks) {
                /*
                    bintray repository does not allow publishing SNAPSHOT artifacts, it has to be published
                    to the OSS JFrog repository
                 */

                dependsOn(ARTIFACTORY_PUBLISH_TASK)
            }
        }
    }
}
