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

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectEvaluationListener
import org.gradle.api.ProjectState
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.kotlin.dsl.get

@Suppress("unused")
class SimplePublisherPlugin : Plugin<Project> {

    private lateinit var extension: SimplePublisherExtension

    override fun apply(project: Project) {

        with(project.pluginManager) {
            apply("org.gradle.maven-publish")
            apply("org.gradle.signing")
            apply("com.jfrog.bintray")
            apply("com.jfrog.artifactory")
        }

        extension = project.extensions.create("simplyPublish", SimplePublisherExtension::class.java, project)

        val pom = PomFactory().resolvePom(project)

        project.afterEvaluate {
            PublicationBuilder(
                extension,
                project,
                pom
            ).build()

        }

    }
}
