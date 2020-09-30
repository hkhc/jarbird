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

import io.hkhc.util.LOG_PREFIX
import io.hkhc.util.detailMessageError
import org.gradle.api.GradleException
import org.gradle.api.Project

interface MavenEndpoint {

    companion object {
        fun create(release: String, snapshot: String, username: String, password: String): MavenEndpoint {
            return SimpleMavenEndpoint(release, snapshot, username, password)
        }
    }

    val releaseUrl: String
    val snapshotUrl: String
    val username: String
    val password: String
}

fun Project.byProperty(key: String): MavenEndpoint {
    return PropertyMavenEndpoint(this, key)
}
fun Project.mavenCentral(): MavenEndpoint {
    return MavenCentralEndpoint(this)
}

fun resolveProperty(project: Project, propertyName: String): String {
    var value: String? = project.property(propertyName) as String?
    if (value == null) {
        detailMessageError(
            project.logger,
            "Failed to find property '$propertyName'.",
            "Add it to one of the gradle.properties, or specify -D$propertyName command line option."
        )
        throw GradleException("$LOG_PREFIX Failed to find property '$propertyName'")
    }
    return value
}
