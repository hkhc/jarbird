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

import io.hkhc.utils.test.createMockProjectTree
import io.hkhc.utils.tree.RoundTheme
import io.hkhc.utils.tree.stringTreeOf
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.gradle.api.Task

class TaskInfoTest : FunSpec({

    class TestTaskInfo : TaskInfo() {
        override val name = "newTask"
        override val description = "newDescription"
    }



    beforeTest {


    }

    test("register task") {

        TaskInfo.eagar = false

        val projectMap = createMockProjectTree(stringTreeOf(RoundTheme) {
            "app" ()
        })
        val project = projectMap.get("app")!!

        var callbackExecuted = false
        lateinit var task: Task

        val taskContainer = TestTaskInfo().register(project) {
//            task = this
            callbackExecuted = true
        }

        callbackExecuted shouldBe false

        task = taskContainer.get()

        callbackExecuted shouldBe true
        task.name shouldBe "newTask"
        task.description shouldBe "newDescription"

    }


})

