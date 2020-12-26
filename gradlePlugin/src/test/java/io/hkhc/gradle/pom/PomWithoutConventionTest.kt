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

import io.hkhc.utils.test.MockProjectInfo
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.gradle.api.Project
import java.util.Calendar
import java.util.GregorianCalendar

// @ExtendWith(MockKExtension::class)
class PomWithoutConventionTest : StringSpec({

    lateinit var project: Project

    beforeTest {
        project = mockk(relaxed = true)
    }

    "Pom shall be sync with project object without convention" {

        // GIVEN
        Pom.setDateHandler { GregorianCalendar.getInstance().apply { set(Calendar.YEAR, 1999) } }

        val pom = Pom()
        pom.licenses.add(License("Apache-2.0"))
        pom.scm.repoType = "github.com"
        pom.scm.repoName = "hkhc/mylib"

        val mockProject = MockProjectInfo("io.hkhc", "mylib", "1.0", "desc")

        // WHEN
        pom.syncWith(mockProject)

        // THEN
        pom.group shouldBe "io.hkhc"
        pom.name shouldBe "mylib"
        pom.version shouldBe "1.0"
        pom.description shouldBe "desc"

        pom.inceptionYear shouldBe 1999
        pom.packaging shouldBe "jar"
        pom.licenses[0].url shouldBe "http://www.apache.org/licenses/LICENSE-2.0.txt"

        pom.scm.url shouldBe "https://github.com/hkhc/mylib"
        pom.url shouldBe "https://github.com/hkhc/mylib"
        pom.web.url shouldBe "https://github.com/hkhc/mylib"
    }
})
