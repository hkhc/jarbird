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

package io.hkhc.gradle.internal

import io.hkhc.gradle.DocDeclaration
import io.hkhc.gradle.JarbirdPub
import io.hkhc.gradle.RepoDeclaration
import io.hkhc.gradle.SigningStrategy
import io.hkhc.gradle.VariantStrategy
import io.hkhc.gradle.internal.pub.DocDeclarationImpl
import io.hkhc.gradle.internal.pub.RepoDeclarationsImpl
import io.hkhc.gradle.internal.pub.SigningStrategyImpl
import io.hkhc.gradle.internal.pub.VariantStrategyImpl
import io.hkhc.gradle.internal.repo.ArtifactoryRepoSpec
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.get

open class JarbirdPubImpl(
    protected val project: Project,
    private val ext: JarbirdExtensionImpl,
    projectProperty: ProjectProperty,
    variant: String = ""
) : JarbirdPub,
    SigningStrategy by SigningStrategyImpl(),
    VariantStrategy by VariantStrategyImpl(variant),
    RepoDeclaration by RepoDeclarationsImpl(project, projectProperty, ext),
    DocDeclaration by DocDeclarationImpl(ext, { /* default cokkaConfig */ })
{

    override var pubName: String = "lib"

    var mComponent: SoftwareComponent? = null
    var mSourceSet: SourceSet? = null

    var component: SoftwareComponent? = null
        get() {
            return mComponent ?: project.components["java"]
        }
        private set

    var sourceSet: SourceSet? = null
        get() = mSourceSet
        private set

    open fun sourceSetModel(): SourceSetModel? = if (component != null)
        JavaConventionSourceSetModel(project)
    else if (sourceSet != null)
        JavaConventionSourceSetModel(project, sourceSet!!.name)
    else
        null

    override fun getGAV(): String {
        return "${pom.group}:${variantArtifactId()}:${variantVersion()}"
    }

    override fun pluginCoordinate(): String {
        return if (pom.isGradlePlugin()) "${pom.plugin?.id}" else "NOT-A-PLUGIN"
    }

    override fun from(source: Any) {
        when (source) {
            is SoftwareComponent -> {
                component = source
            }
            is SourceSet -> {
                component = null
                sourceSet = source
            }
            else -> {
                throw GradleException("from() accepts SoftwareComponent or SourceSet only")
            }
        }
    }

    override fun sourceSetNames(vararg names: String): Any = SourceSetNames(project, names)
    override fun sourceSetNames(names: List<String>): Any = SourceSetNames(project, names.toTypedArray())
    override fun sourceDirs(dirs: Any): Any = SourceDirs(dirs)

    fun finalizeRepos() {

        val artifactoryRepos = artifactoryRepos()

        val err = if ((pom.group ?: "").isEmpty()) {
            """
                Group is missed in POM for pub($variant). 
                May be the variant name or POM file is not correct.
            """.trimIndent()
        } else if ((pom.artifactId ?: "").isEmpty()) {
            """
                ArtifactID is missed in POM for pub($variant).
                May be the variant name or POM file is not correct.
            """.trimIndent()
        } else if ((pom.version ?: "").isEmpty()) {
            """
                Version is missed in POM for pub($variant).
                May be the variant name or POM file is not correct.
            """.trimIndent()
        } else if (artifactoryRepos.size > 1) {
            """
                One artifactory repository per sub-project only is supported.
                ${artifactoryRepos.size} is detected. 
                ${artifactoryRepos.joinToString { it.id }}
            """.trimIndent()
        } else {
            null
        }

        if (err != null) throw GradleException(err)

        if (pom.isGradlePlugin()) {
            gradlePortal()
        }
    }

    private fun artifactoryRepos(): List<ArtifactoryRepoSpec> {
        return getRepos().filterIsInstance<ArtifactoryRepoSpec>()
    }


}
