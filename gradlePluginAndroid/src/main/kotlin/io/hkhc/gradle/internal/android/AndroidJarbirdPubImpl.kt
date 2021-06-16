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

package io.hkhc.gradle.internal.android

import io.hkhc.gradle.DocDeclaration
import io.hkhc.gradle.RepoDeclaration
import io.hkhc.gradle.VariantStrategy
import io.hkhc.gradle.android.AndroidJarbirdPlugin.Companion.LOG_PREFIX
import io.hkhc.gradle.internal.JarbirdPubImpl
import io.hkhc.gradle.internal.ProjectProperty
import io.hkhc.gradle.internal.SourceSetModel
import org.gradle.api.Project

class AndroidJarbirdPubImpl(
    project: Project,
    parentVariantStrategy: VariantStrategy,
    parentRepoDeclaration: RepoDeclaration,
    parentDocDeclaration: DocDeclaration,
    projectProperty: ProjectProperty,
    variant: String = ""
) : JarbirdPubImpl(
    project,
    projectProperty,
    variant,
    parentVariantStrategy,
    parentRepoDeclaration,
    parentDocDeclaration
) {

    private var libraryVariant: LibraryVariant? = null

    /**
     * provide information on how the project is build. The parameter could be instance of
     * - SoftwareComponent
     * - SourceSet
     * - Android LibraryVariant
     */
    override fun from(source: Any) {
        LibraryVariant.implemented(source)?.let { variant ->
            variant.getName()?.let { name ->
                project.logger.debug("$LOG_PREFIX publish library variant $name")
                libraryVariant = variant
                mComponent = project.components.getAt(name)
            }
        } ?: run {
            project.logger.debug("$LOG_PREFIX library variant not found, fallback")
            super.from(source)
        }
    }

    // As initial value, we don't use getter wrapper to access component and sourceSet.
    override fun sourceSetModel(): SourceSetModel? =
        libraryVariant?.let { AndroidLibraryVariantSourceSetModel(project, it) } ?: super.sourceSetModel()
}
