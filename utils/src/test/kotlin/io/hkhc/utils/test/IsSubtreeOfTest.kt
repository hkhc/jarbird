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

package io.hkhc.utils.test

import io.hkhc.utils.tree.isSubtreeOf
import io.hkhc.utils.tree.stringTreeOf
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class IsSubtreeOfTest: FunSpec( {

    test("single node") {
        stringTreeOf { +"Hello" }.isSubtreeOf(stringTreeOf { +"Hello" }) shouldBe true
        stringTreeOf { +"Hello" }.isSubtreeOf(stringTreeOf { +"HelloX" }) shouldBe false
    }

    test("single node is subtree of two level") {
        stringTreeOf { +"Hello" }.isSubtreeOf(stringTreeOf {
            "Hello" {
                +"World"
            }
        }) shouldBe true
        stringTreeOf { +"Hello" }.isSubtreeOf(stringTreeOf {
            "Hello" {
                +"WorldX"
            }
        }) shouldBe true
    }

    test("two-level tree is subtree of two-level subtree") {

        stringTreeOf {
            "Hello" {
                +"World"
            }
        }.isSubtreeOf(stringTreeOf {
            "Hello" {
                +"World"
            }
        }) shouldBe true

        stringTreeOf {
            "Hello" {
                +"Worldx"
            }
        }.isSubtreeOf(stringTreeOf {
            "Hello" {
                +"World"
            }
        }) shouldBe false


        stringTreeOf {
            "Hello" {
                +"World"
            }
        }.isSubtreeOf(stringTreeOf {
            "Hello" {
                +"World"
                +"Zero"
            }
        }) shouldBe true

        stringTreeOf {
            "Hello" {
                +"World"
                +"Zero"
            }
        }.isSubtreeOf(stringTreeOf {
            "Hello" {
                +"Zero"
                +"World"
            }
        }) shouldBe true

        stringTreeOf {
            "Hello" {
                +"WorldX"
                +"Zero"
            }
        }.isSubtreeOf(stringTreeOf {
            "Hello" {
                +"Zero"
                +"World"
            }
        }) shouldBe false

        stringTreeOf {
            "Hello" {
                +"World"
            }
        }.isSubtreeOf(stringTreeOf {
            "Hello" {
                +"WorldX"
            }
        }) shouldBe false
    }

    test("two-level tree is subtree of three-level subtree") {

        stringTreeOf {
            "Hello" {
                +"World"
            }
        }.isSubtreeOf(stringTreeOf {
            "Hello" {
                "World" {
                    "Apple"()
                }
                +"Zero"
            }
        }) shouldBe true

    }

    test("task tree") {

        stringTreeOf {
            ":jbPublishToMavenLocal SUCCESS" {
                ":jbPublishTestArtifactToMavenLocal SUCCESS" {
                    ":publishTestArtifactPublicationToMavenLocal SUCCESS" {
                        ":xgenerateMetadataFileForTestArtifactPublication SUCCESS" {
                            ":jar SUCCESS"()
                        }
                        ":generatePomFileForTestArtifactPublication SUCCESS"()
                        ":jar SUCCESS"()
                        ":jbDokkaJarTestArtifact SUCCESS" {
                            ":jbDokkaHtmlTestArtifact SUCCESS"()
                        }
                        ":signTestArtifactPublication SUCCESS" {
                            ":generateMetadataFileForTestArtifactPublication SUCCESS" {
                                ":jar SUCCESS"()
                            }
                            ":generatePomFileForTestArtifactPublication SUCCESS" {

                            }
                            ":jar SUCCESS"()
                            ":jbDokkaJarTestArtifact SUCCESS"()
                            ":sourcesJarTestArtifact SUCCESS"()
                        }
                        ":sourcesJarTestArtifact SUCCESS"()
                    }
                }
            }
        }.isSubtreeOf(
            stringTreeOf {
                ":jbPublishToMavenLocal SUCCESS" {
                    ":jbPublishTestArtifactToMavenLocal SUCCESS" {
                        ":publishTestArtifactPublicationToMavenLocal SUCCESS" {
                            ":generateMetadataFileForTestArtifactPublication SUCCESS" {
                                ":jar SUCCESS"()
                            }
                            ":generatePomFileForTestArtifactPublication SUCCESS"()
                            ":jar SUCCESS"()
                            ":jbDokkaJarTestArtifact SUCCESS" {
                                ":jbDokkaHtmlTestArtifact SUCCESS"()
                            }
                            ":signTestArtifactPublication SUCCESS" {
                                ":generateMetadataFileForTestArtifactPublication SUCCESS" {
                                    ":jar SUCCESS"()
                                }
                                ":generatePomFileForTestArtifactPublication SUCCESS" {

                                }
                                ":jar SUCCESS"()
                                ":jbDokkaJarTestArtifact SUCCESS"()
                                ":sourcesJarTestArtifact SUCCESS"()
                            }
                            ":sourcesJarTestArtifact SUCCESS"()
                        }
                    }
                }
            }
        ) shouldBe false
    }


})
