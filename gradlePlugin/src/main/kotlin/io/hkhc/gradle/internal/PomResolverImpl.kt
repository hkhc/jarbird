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

import io.hkhc.gradle.pom.Pom
import io.hkhc.gradle.pom.internal.PomGroupFactory
import org.gradle.api.GradleException

class PomResolverImpl(private val projectInfo: ProjectInfo) : PomResolver {

    private val pomGroup = PomGroupFactory.resolvePomGroup(projectInfo.rootDir, projectInfo.projectDir)

    override fun resolve(variant: String): Pom {

        lateinit var pom: Pom

        // find the pom of particular variant. Normally pom of a variant is a combination of pom spec of that
        // particulat variant and the non-variant pom. However, if no variant pom spec is found, we use
        // the non-variant pom spec directly
        pomGroup[variant].also { variantPom ->
            if (variantPom == null) {
                pomGroup[""]?.let { pom = it } ?: throw GradleException("Variant '$variant' is not found")
            } else {
                pom = variantPom
            }
        }

        // TODO we ignore that pom overwrite some project properties in the mean time.
        // need to properly take care of it.
        pom.syncWith(projectInfo)

        "Aggregated POM configuration:\n${pomGroup.formattedDump()}".lines().filter { it.trim() != "" }.forEach {
            JarbirdLogger.logger.debug("$LOG_PREFIX $it")
        }

        return pom
    }
}
