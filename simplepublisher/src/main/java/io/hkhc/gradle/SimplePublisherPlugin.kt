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

import com.jfrog.bintray.gradle.tasks.RecordingCopyTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectEvaluationListener
import org.gradle.api.ProjectState

@Suppress("unused")
class SimplePublisherPlugin : Plugin<Project> {

    private lateinit var extension: SimplePublisherExtension

    override fun apply(project: Project) {

        val pom = PomFactory().resolvePom(project)

        extension = project.extensions.create("simplyPublish", SimplePublisherExtension::class.java, project)
        extension.pom = pom

        /*
            ProjectEvaluationListener is invoked before any project.afterEvaluate.
            So we use projectEvaluateListener to make sure our setup has done before the projectEvaluationListener
            in other plugins
         */
        project.gradle.addProjectEvaluationListener(object:ProjectEvaluationListener {
            override fun beforeEvaluate(p0: Project) {
            }

            override fun afterEvaluate(p0: Project, p1: ProjectState) {
                PublicationBuilder(
                    extension,
                    project,
                    pom
                ).build()
            }

        })

        /*
            The following plugins shall be declared as depdendencies in build.gradle.kts.
            The exact dependency identifier can be find by accessing the plugin POM file at
            https://plugins.gradle.org/m2/[path-by-id]/[plugin-id].gradle.plugin/[version]/
                [plugin-id].gradle.plugin-[version].pom

            e.g. for plugin com.gradle.plugin-publish, check the depndency section of POM at
            https://plugins.gradle.org/m2/com/gradle/plugin-publish/
                com.gradle.plugin-publish.gradle.plugin/0.10.1/com.gradle.plugin-publish.gradle.plugin-0.10.1.pom
         */

        with(project.pluginManager) {
            apply("org.gradle.maven-publish")
            apply("org.gradle.signing")
            apply("com.jfrog.bintray")
            apply("com.jfrog.artifactory")
            apply("com.gradle.plugin-publish")
        }

    }
}
