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

package io.hkhc.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.provideDelegate

class PublishConfig(project: Project) {

    val artifactGroup : String? by project
    val artifactId : String? by project
    val artifactEyeD = artifactId?: project.name
    val artifactVersion : String by project
    val pomDescription : String? by project
    val pomUrl : String? by project
    val licenseName : String? by project
    val licenseUrl : String? by project
    val licenseDist : String? by project
    val developerId : String? by project
    val developerName : String? by project
    val developerEmail : String? by project
    val scmUrl : String? by project
    val scmConnection : String? by project
    val scmDeveloperConnection : String? by project
    val scmGithubRepo : String? by project
    val issuesUrl : String? by project

    val nexusSnapshotRepositoryUrl : String? by project
    val nexusReleaseRepositoryUrl : String? by project
    val nexusUsername : String? by project
    val nexusPassword : String? by project

    val bintrayUser: String? by project
    val bintrayApiKey: String? by project
    val bintrayLabels: String? by project // comma delimited

}