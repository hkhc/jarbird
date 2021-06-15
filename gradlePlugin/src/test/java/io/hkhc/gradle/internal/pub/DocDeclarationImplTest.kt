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

package io.hkhc.gradle.internal.pub

import io.hkhc.gradle.JarbirdPub
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.jetbrains.dokka.gradle.DokkaTask

class DocDeclarationImplTest : FunSpec({

    test("default value") {

        var configExecuted = false
        val mockedPub = mockk<JarbirdPub>()
        val mockedDokkaTask = mockk<DokkaTask>()

        DocDeclarationImpl { pub ->
            this shouldBe mockedDokkaTask
            pub shouldBe mockedPub
            configExecuted = true
        }.apply {
            dokkaConfig.invoke(mockedDokkaTask, mockedPub)
        }

        configExecuted shouldBe true
    }

    test("Specify a config") {

        var defaultConfigExecuted = false
        var customConfigExecuted = false
        val mockedPub = mockk<JarbirdPub>()
        val mockedDokkaTask = mockk<DokkaTask>()

        DocDeclarationImpl { pub ->
            defaultConfigExecuted = true
        }.apply {
            dokkaConfig { pub ->
                this shouldBe mockedDokkaTask
                pub shouldBe mockedPub
                customConfigExecuted = true
            }
            dokkaConfig.invoke(mockedDokkaTask, mockedPub)
        }

        defaultConfigExecuted shouldBe false
        customConfigExecuted shouldBe true
    }

    test("parent declaration with config and have local config") {

        var parentConfigExecuted = false
        var defaultConfigExecuted = false
        val mockedPub = mockk<JarbirdPub>()
        val mockedDokkaTask = mockk<DokkaTask>()

        val parent = DocDeclarationImpl().apply {
            dokkaConfig { pub ->
                parentConfigExecuted = true
            }
        }

        DocDeclarationImpl(parent).apply {
            dokkaConfig { pub ->
                defaultConfigExecuted = true
            }
            dokkaConfig.invoke(mockedDokkaTask, mockedPub)
        }

        parentConfigExecuted shouldBe false
        defaultConfigExecuted shouldBe true
    }

    test("with parent declaration default config and no local config") {

        var defaultConfigExecuted = false
        var parentConfigExecuted = false
        val mockedPub = mockk<JarbirdPub>()
        val mockedDokkaTask = mockk<DokkaTask>()

        val parent = DocDeclarationImpl { pub ->
            this shouldBe mockedDokkaTask
            pub shouldBe mockedPub
            parentConfigExecuted = true
        }

        DocDeclarationImpl(parent) { pub ->
            defaultConfigExecuted = true
        }.apply {
            dokkaConfig.invoke(mockedDokkaTask, mockedPub)
        }

        defaultConfigExecuted shouldBe false
        parentConfigExecuted shouldBe true
    }

    test("with parent declaration custom config and no local config") {

        var defaultConfigExecuted = false
        var parentConfigExecuted = false
        val mockedPub = mockk<JarbirdPub>()
        val mockedDokkaTask = mockk<DokkaTask>()

        val parent = DocDeclarationImpl().apply {
            dokkaConfig { pub ->
                this shouldBe mockedDokkaTask
                pub shouldBe mockedPub
                parentConfigExecuted = true
            }
        }

        DocDeclarationImpl(parent) { pub ->
            defaultConfigExecuted = true
        }.apply {
            dokkaConfig.invoke(mockedDokkaTask, mockedPub)
        }

        defaultConfigExecuted shouldBe false
        parentConfigExecuted shouldBe true
    }

    test("with parent declaration custom config and local custom config") {

        var defaultConfigExecuted = false
        var customConfigExecuted = false
        var parentConfigExecuted = false
        val mockedPub = mockk<JarbirdPub>()
        val mockedDokkaTask = mockk<DokkaTask>()

        val parent = DocDeclarationImpl().apply {
            dokkaConfig { pub ->
                parentConfigExecuted = true
            }
        }

        DocDeclarationImpl(parent) { pub ->
            defaultConfigExecuted = true
        }.apply {
            dokkaConfig { pub ->
                this shouldBe mockedDokkaTask
                pub shouldBe mockedPub
                customConfigExecuted = true
            }
            dokkaConfig.invoke(mockedDokkaTask, mockedPub)
        }

        defaultConfigExecuted shouldBe false
        customConfigExecuted shouldBe true
        parentConfigExecuted shouldBe false
    }
})
