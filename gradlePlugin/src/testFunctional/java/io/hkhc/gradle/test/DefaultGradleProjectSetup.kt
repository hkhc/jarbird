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

package io.hkhc.gradle.test

import io.hkhc.utils.PropertiesEditor
import java.io.File

open class DefaultGradleProjectSetup(val projectDir: File) {

    /* Sub-project directories */
    var subProjDirs = arrayOf<String>()

    /* directory to place file signing keystore */
    var keystoreTemplateDir = "functionalTestData/keystore"

    /* directories to copy source code to project, or subprojects if any */
    var sourceSetTemplateDirs = arrayOf("functionalTestData/lib")

    /* directory for local repository */
    var localRepoDir = "localRepo"

    /* environment variables to run Gradle */
    var envs = defaultEnvs(projectDir)

    /* mock server to mimic repository servers to accept requests for test */
    var mockServers = mutableListOf<BaseMockRepositoryServer>()

    var mavenMockServer: MockMavenRepositoryServer? = null
        set(value) {
            if (value != null) {
                mockServers.add(value)
            }
            field = value
        }

    var bintrayMockServer: MockBintrayRepositoryServer? = null
        set(value) {
            if (value != null) {
                mockServers.add(value)
            }
            field = value
        }

    var artifactoryMockServer: MockArtifactoryRepositoryServer? = null
        set(value) {
            if (value != null) {
                mockServers.add(value)
            }
            field = value
        }

    /* expect list of tasks executed in the Gradle run, should be prefixed by ':' or ':proj:' */
    lateinit var expectedTaskList: List<String>

    val localRepoDirFile = File(projectDir, localRepoDir)

    fun setupKeystore() {
        File(keystoreTemplateDir).copyRecursively(projectDir)
    }

    fun setupSourceSets() {
        if (subProjDirs.isEmpty()) {
            sourceSetTemplateDirs.forEach { source ->
                File(source).copyRecursively(projectDir)
            }
        } else {
            sourceSetTemplateDirs.zip(subProjDirs).forEach { (source, proj) ->
                File(source).copyRecursively(File(projectDir, proj))
            }
        }
    }

    fun setupLocalRepo() {
        localRepoDirFile.mkdirs()
        System.setProperty("maven.repo.local", localRepoDirFile.absolutePath)
    }

    fun setupGradleProperties(block: PropertiesEditor.() -> Unit = {}) {

        PropertiesEditor("$projectDir/gradle.properties") {
            "org.gradle.jvmargs" to "-Xmx2000m"
            setupKeyStore(projectDir)
            block.invoke(this)
        }
    }

    fun setupSettingsGradle(base: String = "") {

        if (subProjDirs.size >= 1) {
            val projList = subProjDirs.joinToString(",") { "\":$it\"" }
            writeFile(
                "settings.gradle",
                "$base\n" +
                    """
                    include($projList)
                    """.trimIndent()
            )
        }
    }

    fun setup(block: DefaultGradleProjectSetup.() -> Unit = {}): DefaultGradleProjectSetup {
        block.invoke(this)
        subProjDirs.forEach {
            File("$projectDir/$it").mkdirs()
        }
        setupKeystore()
        setupSourceSets()
        setupLocalRepo()
        setupSettingsGradle()
        return this
    }

    fun writeFile(relativePath: String, content: String) {
        File("$projectDir/$relativePath").writeText(content)
    }

    fun getGradleTaskTester(): GradleTaskTester {

        return GradleTaskTester(
            projectDir,
            envs
        )
    }
}
