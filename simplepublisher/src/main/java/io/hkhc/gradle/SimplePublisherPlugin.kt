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

import io.hkhc.gradle.builder.PublicationBuilder
import io.hkhc.gradle.pom.PomFactory
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectEvaluationListener
import org.gradle.api.ProjectState

@Suppress("unused")
class SimplePublisherPlugin : Plugin<Project> {

    private lateinit var extension: SimplePublisherExtension

    /*
        We need to know which project this plugin instance refer to.
        We got call back from ProjectEveluationListener once for every root/sub project, and we
        want to proceed if the project param in callback match this plugin instance
     */
    private lateinit var project: Project

    override fun apply(p: Project) {

        project = p

        val pom = PomFactory().resolvePom(project)

        extension = project.extensions.create(SP_EXT_NAME, SimplePublisherExtension::class.java, project)
        extension.pom = pom

        /*
            ProjectEvaluationListener is invoked before any project.afterEvaluate.
            So we use projectEvaluateListener to make sure our setup has done before the projectEvaluationListener
            in other plugins.

            Setup bintrayExtension before bintray's ProjectEvaluationListener.afterEvaluate
            which expect bintray extension to be ready.
            The bintray task has been given publication names and it is fine the the publication
            is not ready yet. The actual publication is not accessed until execution of task.

            Setup publication for android library shall be done at late afterEvaluate, so that android library plugin
            has change to create the components and source sets.

         */
            project.gradle.addProjectEvaluationListener(object : ProjectEvaluationListener {
                override fun beforeEvaluate(project: Project) {
                    // Do nothing intentionally
                }

                // Build Phase 1
                override fun afterEvaluate(p: Project, projectState: ProjectState) {

                    if (project == p) {
                        // Gradle plugin publish plugin is not compatible with Android plugin.
                        // apply it only if needed
                        if (extension.gradlePlugin) {
                            project.pluginManager.apply("com.gradle.plugin-publish")
                        }
                        if (extension.gradlePlugin && !project.pluginManager.hasPlugin("com.gradle.plugin-publish")) {
                            project.pluginManager.apply("com.gradle.plugin-publish")
                        } else {
                            PublicationBuilder(extension, project, pom).buildPhase1()
                        }
                    }
                }
            })

        // Build phase 3
        project.afterEvaluate {
            PublicationBuilder(extension, project, pom).buildPhase3()
        }

        /*
            The following plugins shall be declared as dependencies in build.gradle.kts.
            The exact dependency identifier can be find by accessing the plugin POM file at
            https://plugins.gradle.org/m2/[path-by-id]/[plugin-id].gradle.plugin/[version]/
                [plugin-id].gradle.plugin-[version].pom

            e.g. for plugin com.gradle.plugin-publish, check the depndency section of POM at
            https://plugins.gradle.org/m2/com/gradle/plugin-publish/
                com.gradle.plugin-publish.gradle.plugin/0.10.1/com.gradle.plugin-publish.gradle.plugin-0.10.1.pom
         */

        with(project.pluginManager) {
            apply("org.gradle.maven-publish")
            if (extension.signing) {
                apply("org.gradle.signing")
            }
            apply("com.jfrog.bintray")
            apply("com.jfrog.artifactory")
        }

        project.gradle.addProjectEvaluationListener(object : ProjectEvaluationListener {
            override fun beforeEvaluate(project: Project) {
                // Do nothing intentionally
            }

            // Build Phase 2
            override fun afterEvaluate(p: Project, projectState: ProjectState) {
                if (project == p) {
                    PublicationBuilder(extension, project, pom).buildPhase2()
                }
            }
        })

        // Build phase 4
        project.afterEvaluate {
            PublicationBuilder(extension, project, pom).buildPhase4()
            PublicationBuilder(extension, project, pom).buildPhase5()
        }
    }
}
