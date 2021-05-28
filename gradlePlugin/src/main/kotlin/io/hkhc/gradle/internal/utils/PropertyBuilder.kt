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

package io.hkhc.gradle.internal.utils

import groovy.lang.MissingPropertyException
import io.hkhc.gradle.internal.ProjectProperty

class PropertyBuilder(
    private val projectProperty: ProjectProperty,
    private val keyPrefix: String,
    private val defaultValue: String = ""
) {
    fun resolve(key: String, defaultValue: String = "") =
        resolveProperty(projectProperty, "repository.$keyPrefix.$key", defaultValue)

    fun resolveProperty(projectProperty: ProjectProperty, propertyName: String, defaultValue: String = ""): String {

        /*
        https://linchpiner.github.io/gradle-for-devops-2.html
        order of precedence to resolve property
        - Project.property()
            - ext block
            - -P, -D, environment variable (ORG_GRADLE_PROJECT_*)
            - ~/.gradle/gradle.properties
            - project gradle.properties
        - System.getenv()
         */

        val value: String = try {
            projectProperty.property(propertyName) as String? ?: ""
        } catch (e: MissingPropertyException) {
            defaultValue
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

}
