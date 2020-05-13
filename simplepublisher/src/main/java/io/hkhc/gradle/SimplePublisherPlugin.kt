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
import io.hkhc.util.ANDROID_LIBRARY_PLUGIN_ID
import io.hkhc.util.LOG_PREFIX
import io.hkhc.util.detailMessageError
import io.hkhc.util.fatalMessage
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectEvaluationListener
import org.gradle.api.ProjectState

@Suppress("unused")
class SimplePublisherPlugin : Plugin<Project> {

    private lateinit var extension: SimplePublisherExtension
    private var androidPluginAppliedBeforeUs = false

    /*
        We need to know which project this plugin instance refer to.
        We got call back from ProjectEveluationListener once for every root/sub project, and we
        want to proceed if the project param in callback match this plugin instance
     */
    private lateinit var project: Project

    // TODO check if POM fulfill minimal requirements for publishing
    // TODO maven publishing dry-run
    // TODO accept both pom.yml or pom.yaml

    @Suppress("ThrowsCount")
    private fun precheck(project: Project) {
        with(project) {
            if (group.toString().isBlank()) {
                detailMessageError(project.logger,
                    "Group name is not specified",
                    "Add 'group' value to build script or pom.xml")
                throw GradleException("$LOG_PREFIX Group name is not specified")
            }
            if (version.toString().isBlank()) {
                detailMessageError(project.logger,
                    "Version name is not specified",
                    "Add 'version' value to build script or pom.xml")
                throw GradleException("$LOG_PREFIX Version is not specified")
            }
            if (pluginManager.hasPlugin(ANDROID_LIBRARY_PLUGIN_ID)) {
                if (!androidPluginAppliedBeforeUs) {
                    fatalMessage(project, "io.hkhc.simplepublisher should not " +
                            "applied before $ANDROID_LIBRARY_PLUGIN_ID")
                }
                if (extension.gradlePlugin) {
                    fatalMessage(project, "Cannot build Gradle plugin in Android project")
                }
            }
        }
    }

    private fun checkAndroidPlugin(project: Project) {

        if (project.pluginManager.hasPlugin(ANDROID_LIBRARY_PLUGIN_ID)) {
            androidPluginAppliedBeforeUs = true
            project.logger.debug("$LOG_PREFIX $ANDROID_LIBRARY_PLUGIN_ID plugin is found to be applied")
        } else {
            androidPluginAppliedBeforeUs = false
            project.logger.debug("$LOG_PREFIX apply $ANDROID_LIBRARY_PLUGIN_ID is not found to be applied")
        }
    }

    override fun apply(p: Project) {

        project = p
        project.logger.debug("$LOG_PREFIX Start applying simplepublisher plugin")

        val pom = PomFactory().resolvePom(project)

        extension = project.extensions.create(SP_EXT_NAME, SimplePublisherExtension::class.java, project)
        extension.pom = pom

        project.logger.debug("$LOG_PREFIX Aggregrated POM configuration: $pom")

        checkAndroidPlugin(p)

        /*

        gradle.afterEvaluate
            - Sync POM
            - Phase 1
                - config bintray extension
                - config artifactory exctension
            - plugin: bintray gradle.afterEvaluate
            - Phase 2
                - config plugin publishing

        project.afterEvaluate
            - Phase 3
            - ...
            - bintray
            - gradle plugin
                setup testkit dependency
                validate plugin config
            - Phase 4
                configure publishing
                configure signing
                setup tasks

        gradle.projectEvaluated
            - bintray

        ------------------------------------------------


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

        with(project.pluginManager) {

            /**
             * @see org.gradle.api.publish.maven.plugins.MavenPublishPlugin
             * no evaluation listener
             */
            apply("org.gradle.maven-publish")

            /**
             * @see org.jetbrains.dokka.gradle.DokkaPlugin
             * no evaluation listener
             */
            apply("org.jetbrains.dokka")
        }

        project.gradle.addProjectEvaluationListener(object : ProjectEvaluationListener {
            override fun beforeEvaluate(project: Project) {
                // Do nothing intentionally
            }

            // Build Phase 1
            override fun afterEvaluate(p: Project, projectState: ProjectState) {

                if (project == p) {

                    pom.syncWith(p)

                    // pre-check of final data, for child project
                    // TODO handle multiple level child project?
                    if (!project.isMultiProjectRoot()) {
                        precheck(p)
                    }

                    if (extension.signing) {
                        /**
                         * @see org.gradle.plugins.signing.SigningPlugin
                         * no evaluation listener
                         */
                        project.pluginManager.apply("org.gradle.signing")
                    }

                    if (extension.gradlePlugin) {

                        /**
                         * @see com.gradle.publish.PublishPlugin
                         *      project.afterEvaluate
                         *          setup sourcejar docjar tasks
                         */
                        project.pluginManager.apply("com.gradle.plugin-publish")

                        /**
                         * @see org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin
                         * project.afterEvaluate
                         *      add testkit dependency
                         * project.afterEvaluate
                         *      validate plugin declaration
                         */
                        project.pluginManager.apply("org.gradle.java-gradle-plugin")
                    }
                }
            }
        })

        project.gradle.addProjectEvaluationListener(object : ProjectEvaluationListener {

            override fun beforeEvaluate(project: Project) {
                // do nothing intentionally
            }
            override fun afterEvaluate(p: Project, state: ProjectState) {
                // Gradle plugin publish plugin is not compatible with Android plugin.
                // apply it only if needed, otherwise android aar build will fail
                if (p == project) {
                    PublicationBuilder(extension, project, pom).buildPhase1()
                }
            }
        })

        /*
        We don't apply bintray and artifactory plugin conditionally, because it make use of
        projectEvaluationListener, but we cannot get the flag from extenstion until we run
        afterEvaluate event. This is a conflict. So we just let go and apply these two
        plugins anyway. However we will configure the relevant extensions according to
        the flags in our extension. (@see PublicationBuilder)
         */
        with(project.pluginManager) {
            /**
             * @see com.jfrog.bintray.gradle.BintrayPlugin
             * ProjectsEvaluationListener
             *     afterEvaluate:
             *         bintrayUpload task depends on subProject bintrayUpload
             *     projectEvaluated:
             *         bintrayUpload task depends on publishToMavenLocal
             */
            apply("com.jfrog.bintray")

            /**
             * @see org.jfrog.gradle.plugin.artifactory.ArtifactoryPlugin
             *     afterEvaluate:
             *         artifactoryTasks task depends on subProject
             *     projectEvaluated:
             *         finialize artifactoryTasks task
             */
            apply("com.jfrog.artifactory")
        }

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
        }
    }
}
