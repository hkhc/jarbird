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

package io.hkhc.gradle.pom

import io.hkhc.gradle.internal.getGradleUserHome
import org.gradle.api.Project
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import java.io.File

// TODO get rid of reference to project
class PomGroupFactory(val project: Project) {

    /**
     * read POM spec from a YAML file
     */
    fun readPom(path: String): PomGroup {
        return readPom(File(path))
    }

    fun loadYaml(file: File): PomGroup {
        val yaml = Yaml(Constructor(Pom::class.java))
        val poms = yaml.loadAll(file.readText())?.map { it as Pom }
        return if (poms == null) {
            PomGroup()
        } else {
            PomGroup(poms)
        }
    }

    fun readPom(file: File): PomGroup {
        return if (file.exists()) {
//            project.logger.debug("$LOG_PREFIX File '${file.absolutePath}' found")
            // it is possible that yaml.load return null even if file exists and
            // is a valid yaml file. For example, a YAML file could be fill of comment
            // and have no real tag.
            return loadYaml(file)
        } else {
//            project.logger.debug("$LOG_PREFIX File '${file.absolutePath}' does not exist")
            PomGroup()
        }
    }

    /**
     * Get the list of File of POM file in order of being resolved.
     */
    fun getPomFileList(): List<File> {
        return mutableListOf<File>().apply {
            System.getProperty("pomFile")?.let { add(File(it)) }
            if (project.projectDir != project.rootDir) {
                add(File(pomPath(project.projectDir.absolutePath)))
            }
            add(File(pomPath(project.rootDir.absolutePath)))
            getGradleUserHome()?.let { add(File(pomPath(it))) }
        }
    }

    fun resolvePomGroup(files: List<File>): PomGroup {

        return files
            .map { readPom(it) }
            .fold(PomGroup()) { acc, currPomGroup -> acc.also { currPomGroup.overlayTo(it) } }
    }

    /**
     * resolve POM spec via a series of possible location and accumulate the details
     */
    fun resolvePomGroup() = resolvePomGroup(getPomFileList())
}
