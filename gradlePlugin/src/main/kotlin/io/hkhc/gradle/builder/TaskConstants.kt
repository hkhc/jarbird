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
import io.hkhc.gradle.internal.JarbirdPubImpl
import io.hkhc.gradle.internal.mavenRepoNameCap
import io.hkhc.gradle.internal.pubId
import io.hkhc.gradle.internal.pubNameCap

internal const val JB_TASK_PREFIX = "jbPublish"
internal const val PUBLISH_TASK_PREFIX = "publish"
internal const val MAVEN_LOCAL_CAP = "MavenLocal"

internal const val TO_MAVEN_LOCAL = "ToMavenLocal"
internal const val TO_MAVEN_REPO = "ToMavenRepository"
internal const val TO_GRADLE_PORTAL = "ToGradlePortal"
internal const val TO_BINTRAY = "ToBintray"

internal const val PLUGIN_MARKER_PUB_SUFFIX = "PluginMarkerMaven"

internal const val JB_PUBLISH_TO_MAVEN_LOCAL_TASK = "$JB_TASK_PREFIX$TO_MAVEN_LOCAL"
internal const val JB_PUBLISH_TO_MAVEN_REPO_TASK = "$JB_TASK_PREFIX$TO_MAVEN_REPO"
internal const val JB_PUBLISH_TO_BINTRAY_TASK = "$JB_TASK_PREFIX$TO_BINTRAY"
internal const val JB_PUBLISH_TO_GRADLE_PORTAL_TASK = "$JB_TASK_PREFIX$TO_GRADLE_PORTAL"
internal const val BINTRAY_UPLOAD_TASK = "bintrayUpload"
internal const val ARTIFACTORY_PUBLISH_TASK = "artifactoryPublish"

val JarbirdPub.mavenRepoName: String
    get() = "Maven$pubNameCap"

val JarbirdPub.jbPublishPubTask: String
    get() = "$JB_TASK_PREFIX$pubNameCap"

val JarbirdPub.jbPublishToCustomMavenRepoTask: String
    get() = "${JB_TASK_PREFIX}To$mavenRepoNameCap"

val JarbirdPub.jbPublishPubToMavenLocalTask: String
    get() = "$JB_TASK_PREFIX${pubNameCap}$TO_MAVEN_LOCAL"

val JarbirdPub.jbPublishPubToMavenRepoTask: String
    get() = "$JB_TASK_PREFIX${pubNameCap}$TO_MAVEN_REPO"

val JarbirdPub.jbPublishPubToCustomMavenRepoTask: String
    get() = "$JB_TASK_PREFIX${pubNameCap}To${(this as JarbirdPubImpl).mavenRepo.name}"

val JarbirdPub.publishPubToMavenLocalTask: String
    get() = "$PUBLISH_TASK_PREFIX${pubId.capitalize()}$TO_MAVEN_LOCAL"

val JarbirdPub.publishPluginMarkerPubToMavenLocalTask: String
    get() = "$PUBLISH_TASK_PREFIX$pubNameCap${PLUGIN_MARKER_PUB_SUFFIX}Publication$TO_MAVEN_LOCAL"
val JarbirdPub.publishPubToCustomMavenRepoTask: String
    get() = "$PUBLISH_TASK_PREFIX${pubId}To$mavenRepoNameCap"

val JarbirdPub.publishPluginMarkerPubToCustomMavenRepoTask: String
    get() = "$PUBLISH_TASK_PREFIX$pubNameCap${PLUGIN_MARKER_PUB_SUFFIX}PublicationTo$mavenRepoNameCap"

