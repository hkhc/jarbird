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
import io.hkhc.gradle.internal.ANDROID_LIBRARY_PLUGIN_ID
import io.hkhc.gradle.internal.BuildFlowBuilder
import io.hkhc.gradle.internal.DefaultProjectInfo
import io.hkhc.gradle.internal.DefaultProjectProperty
import io.hkhc.gradle.internal.DefaultSourceResolver
import io.hkhc.gradle.internal.JarbirdExtensionImpl
import io.hkhc.gradle.internal.JarbirdLogger
import io.hkhc.gradle.internal.JarbirdPubImpl
import io.hkhc.gradle.internal.LOG_PREFIX
import io.hkhc.gradle.internal.PLUGIN_FRIENDLY_NAME
import io.hkhc.gradle.internal.PLUGIN_ID
import io.hkhc.gradle.internal.ProjectInfo
import io.hkhc.gradle.internal.ProjectProperty
import io.hkhc.gradle.internal.SP_EXT_NAME
import io.hkhc.gradle.internal.SourceResolver
import io.hkhc.gradle.internal.gradleAfterEvaluate
import io.hkhc.gradle.internal.isMultiProjectRoot
import io.hkhc.gradle.internal.isRoot
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
import org.gradle.api.ProjectEvaluationListener
import org.gradle.api.ProjectState
import org.gradle.api.model.ObjectFactory
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.kotlin.dsl.extra
import org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin
import org.gradle.plugins.signing.SigningPlugin
import org.jfrog.gradle.plugin.artifactory.ArtifactoryPlugin
import javax.inject.Inject

@Suppress("unused")
open class JarbirdPlugin: Plugin<Project> {

    private lateinit var extension: JarbirdExtensionImpl
    private var androidPluginAppliedBeforeUs = false
    var pluginConfig: PluginConfig = object: PluginConfig {
        override fun getSourceResolver(project: Project): SourceResolver {
            return DefaultSourceResolver(project)
        }

        override fun newExtension(
            project: Project,
            projectProperty: ProjectProperty,
            projectInfo: ProjectInfo,
            pomGroup: PomGroup
        ): JarbirdExtensionImpl {
            return JarbirdExtensionImpl(
                project, projectProperty, projectInfo, pomGroup
            )
        }

        override fun shallCreateImplicit(): Boolean {
            return true
        }

        override fun pluginId(): String {
            return PLUGIN_ID
        }
    }

    /*
        We need to know which project this plugin instance refer to.
        We got call back from ProjectEvaluationListener once for every root/sub project, and we
        want to proceed if the project param in callback match this plugin instance
     */
    private lateinit var project: Project

    companion object {

        const val EXT_PLUGIN_CONFIG = "JARBIRD_PLUGIN_CONFIG"

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

    private fun checkAndroidPlugin(project: Project) {

        if (project.pluginManager.hasPlugin(ANDROID_LIBRARY_PLUGIN_ID)) {
            androidPluginAppliedBeforeUs = true
            project.logger.debug("$LOG_PREFIX $ANDROID_LIBRARY_PLUGIN_ID plugin is found to be applied")
        } else {
            androidPluginAppliedBeforeUs = false
            project.logger.debug("$LOG_PREFIX apply $ANDROID_LIBRARY_PLUGIN_ID is not found to be applied")
        }
    }

    @Suppress("ThrowsCount")
    // TODO revamp precheck to based on POM Group rather than project
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

        if (p.extra.has(EXT_PLUGIN_CONFIG)) {
            pluginConfig = p.extra.get(EXT_PLUGIN_CONFIG) as PluginConfig
        }

        if (p.isRoot()) JarbirdLogger.logger = p.logger

        project = p
        project.logger.debug("$LOG_PREFIX Start applying $PLUGIN_FRIENDLY_NAME (${pluginConfig.pluginId()})")

        extension = pluginConfig.newExtension(
            project,
            DefaultProjectProperty(project),
            DefaultProjectInfo(project),
            PomGroupFactory.resolvePomGroup(project.rootDir, project.projectDir)
        )
        project.extensions.add(JarbirdExtension::class.java, SP_EXT_NAME, extension)

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

            if (!project.isMultiProjectRoot() && pluginConfig.shallCreateImplicit()) {
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
                    extension.pubList,
                    pluginConfig.getSourceResolver(project)
                ).buildPhase2()
            } else {
                project.logger.debug("$LOG_PREFIX Not plugin project. Phase 2 skipped.")
            }

            // Give a change for JavaGradlePluginPlugin to setup marker publication before we can patch it.
            project.afterEvaluate {
                @Suppress("UNCHECKED_CAST")
                BuildFlowBuilder(
                    project,
                    extension,
                    extension.pubList,
                    pluginConfig.getSourceResolver(project)
                ).buildPhase3()
            }

            project.afterEvaluate {
                @Suppress("UNCHECKED_CAST")
                BuildFlowBuilder(
                    project,
                    extension,
                    extension.pubList,
                    pluginConfig.getSourceResolver(project)
                ).buildPhase4()
            }
        }

        // Gradle plugin publish plugin is not compatible with Android plugin.
        // apply it only if needed, otherwise android aar build will fail
        // Defer the configuration with afterEvaluate so that Android plugin has a chance
        // to setup itself before we configure the bintray plugin

        // Build phase 1
        project.afterEvaluate {

            // we created an implicit JarbirdPub and we have more in afterEvaluate
            //extension.removeImplicit()

            extension.finalizeRepos()

            @Suppress("UNCHECKED_CAST")
            BuildFlowBuilder(
                project,
                extension,
                extension.pubList as List<JarbirdPubImpl>,
                pluginConfig.getSourceResolver(project)
            ).buildPhase1()
        }


        /*
        We don't apply bintray and artifactory plugin conditionally, because it make use of
        projectEvaluationListener. ProjectEvaluationListener shall be setup within apply().
        On the other hand, we cannot get the information from extension until we run
        project.afterEvaluate events. This is a dilemma. So we just let go and apply these two
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
