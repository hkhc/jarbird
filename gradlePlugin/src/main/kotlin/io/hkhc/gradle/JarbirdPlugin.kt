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

import com.gradle.publish.PublishPlugin
import com.jfrog.bintray.gradle.BintrayPlugin
import io.hkhc.gradle.endpoint.PropertyRepoEndpoint
import io.hkhc.gradle.internal.ANDROID_LIBRARY_PLUGIN_ID
import io.hkhc.gradle.internal.BuildFlowBuilder
import io.hkhc.gradle.internal.JarbirdExtensionImpl
import io.hkhc.gradle.internal.JarbirdPubImpl
import io.hkhc.gradle.internal.LOG_PREFIX
import io.hkhc.gradle.internal.PLUGIN_FRIENDLY_NAME
import io.hkhc.gradle.internal.PLUGIN_ID
import io.hkhc.gradle.internal.SP_EXT_NAME
import io.hkhc.gradle.internal.gradleAfterEvaluate
import io.hkhc.gradle.internal.isMultiProjectRoot
import io.hkhc.gradle.internal.needGradlePlugin
import io.hkhc.gradle.internal.needSigning
import io.hkhc.gradle.internal.utils.detailMessageError
import io.hkhc.gradle.internal.utils.fatalMessage
import io.hkhc.gradle.pom.Pom
import io.hkhc.gradle.pom.PomGroup
import io.hkhc.gradle.pom.internal.PomGroupFactory
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin
import org.gradle.plugins.signing.SigningPlugin
import org.jfrog.gradle.plugin.artifactory.ArtifactoryPlugin

@Suppress("unused")
class JarbirdPlugin : Plugin<Project>, PomGroupCallback {

    private lateinit var extension: JarbirdExtensionImpl
    private lateinit var pomGroup: PomGroup
    private var androidPluginAppliedBeforeUs = false

    /*
        We need to know which project this plugin instance refer to.
        We got call back from ProjectEvaluationListener once for every root/sub project, and we
        want to proceed if the project param in callback match this plugin instance
     */
    private lateinit var project: Project

    companion object {
        internal fun normalizePubName(name: String): String {
            val newName = StringBuffer()
            var newWord = true
            var firstWord = true
            name.forEach {
                if (it.isLetterOrDigit()) {
                    if (newWord) {
                        if (firstWord) {
                            newName.append(it.toLowerCase())
                        } else {
                            newName.append(it.toUpperCase())
                        }
                        newWord = false
                    } else {
                        newName.append(it)
                        firstWord = false
                    }
                } else {
                    // ignore non letter or digit char
                    newWord = true
                }
            }
            return newName.toString()
        }
    }

    // TODO check if POM fulfill minimal requirements for publishing
    // TODO maven publishing dry-run
    // TODO accept both pom.yml or pom.yaml

    private fun checkAndroidPlugin(project: Project) {

        if (project.pluginManager.hasPlugin(ANDROID_LIBRARY_PLUGIN_ID)) {
            androidPluginAppliedBeforeUs = true
            project.logger.debug("$LOG_PREFIX $ANDROID_LIBRARY_PLUGIN_ID plugin is found to be applied")
        } else {
            androidPluginAppliedBeforeUs = false
            project.logger.debug("$LOG_PREFIX apply $ANDROID_LIBRARY_PLUGIN_ID is not found to be applied")
        }
    }

    override fun initPub(pub: JarbirdPub) {

        pub.pom = pomGroup[pub.variant]

        // TODO we ignore that pom overwrite some project properties in the mean time.
        // need to properly take care of it.
        pub.pom.syncWith(project)

        // TODO handle two publications of same artifactaId in the same module.
        pub.pubName = normalizePubName(pub.pom.artifactId ?: "Lib")

        // pre-check of final data, for child project
        // TODO handle multiple level child project?
        if (!project.isMultiProjectRoot()) {
            precheck(pub.pom, project)
        }
    }

    @Suppress("ThrowsCount")
    private fun precheck(pom: Pom, project: Project) {
        with(project) {
            if (group.toString().isBlank()) {
                detailMessageError(
                    project.logger,
                    "Group name is not specified",
                    "Add 'group' value to build script or pom.xml"
                )
                throw GradleException("$LOG_PREFIX Group name is not specified")
            }
            if (version.toString().isBlank()) {
                detailMessageError(
                    project.logger,
                    "Version name is not specified",
                    "Add 'version' value to build script or pom.xml"
                )
                throw GradleException("$LOG_PREFIX Version is not specified")
            }
            if (pluginManager.hasPlugin(ANDROID_LIBRARY_PLUGIN_ID)) {
                if (!androidPluginAppliedBeforeUs) {
                    fatalMessage(
                        project,
                        "$PLUGIN_ID should not applied before $ANDROID_LIBRARY_PLUGIN_ID"
                    )
                }
                if (pom.isGradlePlugin()) {
                    fatalMessage(
                        project,
                        "Cannot build Gradle plugin in Android project"
                    )
                }
            }
        }
    }

    /**
     * The order of applying plugins and whether they are deferred by the two kind of afterEvaluate listener, are
     * important. So mess around them without know exactly the consequence.
     */
    override fun apply(p: Project) {

        project = p
        project.logger.debug("$LOG_PREFIX Start applying $PLUGIN_FRIENDLY_NAME")
        pomGroup = PomGroupFactory(p).resolvePomGroup()

        extension = JarbirdExtensionImpl(project)
        project.extensions.add(JarbirdExtension::class.java, SP_EXT_NAME, extension)
        extension.pomGroupCallback = this

        project.logger.debug("$LOG_PREFIX Aggregated POM configuration: $pomGroup")

        checkAndroidPlugin(p)

        /*

        gradle.afterEvaluate
            - Sync POM
            - Phase 1
                - config bintray extension
                - config artifactory extension
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

            Further, the ProjectEvaluationListener added by Gradle.addProjectEvaluationListener() within
            project.afterEvaluate will not be executed, as the ProjectEvaluateListeners have been executed
            before callback of project.afterEvaluate and will not go back to run again. So if a plugin needs to invoke
            project.afterEvaluate, then it should not be applied within another project.afterEvaluate. However it is
            OK to apply it in ProjectEvaluationListener.

            Setup bintrayExtension before bintray's ProjectEvaluationListener.afterEvaluate
            which expect bintray extension to be ready.
            The bintray task has been given publication names and it is fine the the publication
            is not ready yet. The actual publication is not accessed until execution of task.

            Setup publication for android library shall be done at late afterEvaluate, so that android library plugin
            has change to create the components and source sets.

         */

        with(project.pluginManager) {

            /**
             * "org.gradle.maven-publish"
             * no evaluation listener
             */
            apply(MavenPublishPlugin::class.java)
        }

        /* Under the following situation we need plugins to be applied within the Gradle-scope afterEvaluate
            method. We need the corresponding extension ready before apply it, and we might actually generate
            that extension within plugin, so we need to defer the application of the plugin (e.g. SigningPlugin)
         */

        // Build Phase 1
        project.gradleAfterEvaluate {

            extension.bintrayRepository = extension.bintrayRepository ?: PropertyRepoEndpoint(
                project,
                "bintray"
            )

            if (!project.isMultiProjectRoot()) {
                extension.createImplicit()
            }

            /*
            JavaGradlePluginPlugin expect plugin declaration at top level and not in afterEvaluate block.
            So we can safely configuring gradle plugin pub here.
             */
            if (extension.pubList.needGradlePlugin()) {
                /**
                 * "com.gradle.plugin-publish"
                 *      project.afterEvaluate
                 *          setup sourcejar docjar tasks
                 */
                project.pluginManager.apply(PublishPlugin::class.java)

                /**
                 * @see org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin
                 * "org.gradle.java-gradle-plugin"
                 * project.afterEvaluate
                 *      add testkit dependency
                 * project.afterEvaluate
                 *      validate plugin declaration
                 */
                project.pluginManager.apply(JavaGradlePluginPlugin::class.java)

                @Suppress("UNCHECKED_CAST")
                BuildFlowBuilder(
                    project,
                    extension,
                    extension.pubList
                ).buildPhase2()
            }

            // Give a change for JavaGradlePluginPlugin to setup marker publication before we can patch it.
            project.afterEvaluate {
                @Suppress("UNCHECKED_CAST")
                BuildFlowBuilder(
                    project,
                    extension,
                    extension.pubList
                ).buildPhase3()
            }
        }

        // Gradle plugin publish plugin is not compatible with Android plugin.
        // apply it only if needed, otherwise android aar build will fail
        // Defer the configuration with afterEvaluate so that Android plugin has a chance
        // to setup itself before we configure the bintray plugin

        // Build phase 1
        project.afterEvaluate {

            // we created an implicit JarbirdPub and we have more in afterEvaluate
            extension.removeImplicit()

            with(p.pluginManager) {

                if (extension.pubList.needSigning()) {
                    /**
                     * "org.gradle.signing"
                     * no evaluation listener
                     */
                    /**
                     * "org.gradle.signing"
                     * no evaluation listener
                     */
                    apply(SigningPlugin::class.java)
                }
            }
            @Suppress("UNCHECKED_CAST")
            BuildFlowBuilder(
                project,
                extension,
                extension.pubList as List<JarbirdPubImpl>
            ).buildPhase1()
        }

        project.afterEvaluate {
            @Suppress("UNCHECKED_CAST")
            BuildFlowBuilder(
                project,
                extension,
                extension.pubList
            ).buildPhase4()
        }

        /*
        We don't apply bintray and artifactory plugin conditionally, because it make use of
        projectEvaluationListener, but we cannot get the flag from extension until we run
        afterEvaluate event. This is a conflict. So we just let go and apply these two
        plugins anyway. We put the bintray extension configuration code at PublicationBuilder.buildPhase1, which is
        executed in another ProjectEvaluationListener setup before Bintray's. (@see PublicationBuilder)
         */

        with(project.pluginManager) {
            /**
             * "com.jfrog.bintray"
             * ProjectsEvaluationListener
             *     afterEvaluate:
             *         bintrayUpload task depends on subProject bintrayUpload
             *     projectEvaluated:
             *         bintrayUpload task depends on publishToMavenLocal
             */
            apply(BintrayPlugin::class.java)

            /**
             * "com.jfrog.artifactory"
             *     afterEvaluate:
             *         artifactoryTasks task depends on subProject
             *     projectEvaluated:
             *         finalize artifactoryTasks task
             */
            apply(ArtifactoryPlugin::class.java)
        }
    }
}
