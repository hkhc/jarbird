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

import io.hkhc.gradle.pom.Scm
import io.hkhc.utils.test.`Field perform overlay properly`
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue

class PomScmTest : StringSpec({

    "Scm shall be a data class so that we may assume 'equals' logic is provided" {
        Scm::class.isData.shouldBeTrue()
    }

    // Have one line per property in the class
    "Scm shall overlay properly" {

        `Field perform overlay properly`(::Scm, Scm::url, "value")
        `Field perform overlay properly`(::Scm, Scm::connection, "value")
        `Field perform overlay properly`(::Scm, Scm::developerConnection, "value")
        `Field perform overlay properly`(::Scm, Scm::repoType, "value")
        `Field perform overlay properly`(::Scm, Scm::repoName, "value")
        `Field perform overlay properly`(::Scm, Scm::issueType, "value")
        `Field perform overlay properly`(::Scm, Scm::issueUrl, "value")
        `Field perform overlay properly`(::Scm, Scm::tag, "value")
    }
})
