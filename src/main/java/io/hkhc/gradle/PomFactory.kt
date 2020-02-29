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

import com.charleskorn.kaml.Yaml
import org.gradle.api.Project
import java.io.File

class PomFactory {

    /**
     * read POM spec from a YAML file
     */
    fun readPom(path: String): Pom {
        val file = File(path)
        return if (file.exists()) {
            Yaml.default.parse(Pom.serializer(), File(path).readText())
        } else {
            Pom()
        }
    }

    /**
     * resolve POM spec via a series of possible location and accumulate the details
     */
    fun resolvePom(project: Project): Pom {
        var pom = Pom()
        pom = System.getProperty("pomFile")?.let {
            pom.apply { merge(readPom(it)) }
        } ?: pom

        val gradleUserHomePath = System.getenv("GRADLE_USER_HOME") ?: "~/.gradle"
        val homePomFile = "$gradleUserHomePath/pom.yaml"

        pom.merge(readPom("$homePomFile/pom.yaml"))
        pom.merge(readPom("${project.buildDir}/pom.yaml"))
        pom.merge(readPom("${project.rootDir}/pom.yaml"))

        pom.getFrom(project)

        return pom
    }

    fun validatePom() {

//        with(pom) {
//
//        }

    }
}
