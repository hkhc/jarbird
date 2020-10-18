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

import io.hkhc.gradle.maven.MavenCentralEndpoint
import io.hkhc.gradle.maven.PropertyRepoEndpoint
import io.hkhc.gradle.maven.RepoEndpoint
import org.gradle.api.Project
import java.io.File

class JarbirdPub(@Suppress("unused") private val project: Project) {

    /**
     * Configure for Maven publishing or not (No matter maven central or alternate maven repository)
     */
    var maven = true

    /**
     * Configure for Bintray publishing or not
     */
    var bintray = true

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
     * to suffix the dokka jar task and souruce set jar task.
     * It is usually used for building Android artifact
     */
    var variant: String = ""

    /**
     * The name of component to to be published
     */
    var pubComponent: String = "java"

    /**
     * the dokka task provider object for documentation. If it is not specified, the default dokka task will be used.
     */
    var dokka: Any? = null

    /**
     * The name of sourceset for archiving.
     * if sourcesPath is not provided, the plugin try to get the sources set named [sourceSetName] for source jar task
     */
    var sourceSetName: String = "main"
//
//    /**
//     * The path of sourceset for archiving.
//     * as specified in from(...) of Jar task
//     * if sourcesPath is provided, sourceSetName will be ignored
//     */
//    var sourcesPath: Any? = null

    // TODO make it flexible for more data type
    var sourceSets: Collection<File>? = null
    /**
     * Use if performing signing with external GPG command. false to use Gradle built-in PGP implementation.
     * We will need useGpg=true if we use new keybox (.kbx) format for pur signing key.
     */
    var useGpg = false

    /**
     * Specify maven repository for publishing.
     */
    var mavenRepository: RepoEndpoint? = null

    fun withMavenCentral() {
        mavenRepository = MavenCentralEndpoint(project)
    }

    fun withMavenByProperties(key: String) {
        mavenRepository = PropertyRepoEndpoint(project, "maven.$key")
    }

    var bintrayRepository: RepoEndpoint? = null
}
