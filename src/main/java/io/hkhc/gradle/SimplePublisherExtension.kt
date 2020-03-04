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

// Gradle plugin extensions must be open class so that Gradle system can "decorate" it.
open class SimplePublisherExtension(private val project: Project) {

    val pom = PomFactory().resolvePom(project)

    /**
     * Configure for bintray publishing or not
     */
    var bintray = true

    /**
     * Configure for OSS JFrog Snapshot publishing or not
     */
    var ossArtifactory = true

    /**
     * Configure for gradle plugin publishing or not (reserve for future use)
     */
    var gradlePlugin = false

    /**
     * Configure for artifact signing or not
     */
    var signing = true

    /**
     * the publication name, that affect the task names for publishing
     */
    var pubName: String = "lib"

    /**
     * the variant is string that as suffix of pubName to form a final publication name. It is also used
     * to suffix the dokka jar task and soruce set jar task.
     */
    var variant: String = ""

    /**
     * The name of component to to be published
     */
    var pubComponent: String = "java"

    /**
     * the dokka task provider object for documentation. If it is not speified, the default dokka task will be used.
     */
    var dokka: Any? = null

    /**
     * The name of sourceset for archiving.
     */
    var sourceSetName: String = "main"


}
