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
package io.hkhc.gradle.endpoint

import groovy.lang.MissingPropertyException
import io.hkhc.gradle.internal.DefaultProjectProperty
import io.hkhc.gradle.internal.ProjectProperty
import org.gradle.api.Project

abstract class RepoEndpoint {

    companion object {

        fun create(
            id: String,
            release: String,
            snapshot: String,
            username: String,
            password: String,
            @Suppress("UNUSED_PARAMETER") apikey: String = "",
            description: String = id
        ):
            RepoEndpoint {
                return SimpleRepoEndpoint(
                    id,
                    release,
                    snapshot,
                    username,
                    password,
                    apikey,
                    description
                )
            }
    }

    abstract val releaseUrl: String
    abstract val snapshotUrl: String
    abstract val username: String
    abstract val password: String
    abstract val apikey: String
    open val description: String
        get() = ""
    abstract val id: String

    override fun equals(other: Any?): Boolean {

        if (other != null) {

            if (this === other) return true
            if (this::class.java != other::class.java) return false

            val that = other as RepoEndpoint

            if (releaseUrl != that.releaseUrl) return false
            if (snapshotUrl != that.snapshotUrl) return false
            if (username != that.username) return false
            if (password != that.password) return false
            if (apikey != that.apikey) return false
            if (description != that.description) return false
            if (id != that.id) return false

            return true
        }

        return false
    }

    override fun hashCode(): Int {

        return """
            1 - $releaseUrl
            2 - $snapshotUrl
            3 - $username
            4 - $password
            5 - $apikey
            6 - $description
            7 - $id
        """.trimIndent().hashCode()
    }

    override fun toString() =
        """
            Release URL  : $releaseUrl
            Snapshot URL : $snapshotUrl
            Username     : $username
            Password     : $password
            API Key      : $apikey
            Description  : $description
            ID           : $id
        """.trimIndent()
}

fun Project.byProperty(key: String): RepoEndpoint {
    return PropertyRepoEndpoint(DefaultProjectProperty(this), key)
}
fun resolveProperty(projectProperty: ProjectProperty, propertyName: String): String {
    val value: String = try {
        projectProperty.property(propertyName) as String? ?: ""
    } catch (e: MissingPropertyException) {
        ""
    }
//    if (value == null) {
//        detailMessageError(
//            JarbirdLogger.logger,
//            "Failed to find property '$propertyName'.",
//            "Add it to one of the gradle.properties, or specify -D$propertyName command line option."
//        )
//        throw GradleException("$LOG_PREFIX Failed to find property '$propertyName'")
//    }
    return value
}
