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

import io.hkhc.utils.test.MockProjectProperty
import io.hkhc.utils.test.tempDirectory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.gradle.api.Project
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.tasks.SourceSet
import java.io.File

class JarbirdPubImplTest : FunSpec({

    lateinit var project: Project
    lateinit var ext: JarbirdExtensionImpl
    lateinit var projectProperty: ProjectProperty

    beforeTest {

        project = mockk(relaxed = true) {
            every { components } returns mockk {
                every { getByName("java") } returns mockk()
            }
        }

        JarbirdLogger.logger = project.logger

        projectProperty = MockProjectProperty(
            mapOf(
                "repository.artifactory.mock.release" to "https://release",
                "repository.artifactory.mock.snapshot" to "https://snapshot",
                "repository.artifactory.mock.repoKey" to "oss-snapshot-local"
            )
        )

        ext = mockk()


    }

    test("Default value") {
        val pub = JarbirdPubImpl(project, ext, ext, ext, projectProperty)

        val component: SoftwareComponent = mockk()

        pub.from(component)
        pub.component shouldBe component
        pub.sourceSet.shouldBeNull()

    }


    test("Setup component") {
        val pub = JarbirdPubImpl(project, ext, ext, ext, projectProperty)

        val component: SoftwareComponent = mockk()

        pub.from(component)
        pub.component shouldBe component
        pub.sourceSet.shouldBeNull()

    }

    test("Setup sourceSet") {
        val pub = JarbirdPubImpl(project, ext, ext, ext, projectProperty)

        val sourceSet: SourceSet = mockk()

        pub.from(sourceSet)
        pub.sourceSet shouldBe sourceSet
        pub.component.shouldBeNull()

    }


})
