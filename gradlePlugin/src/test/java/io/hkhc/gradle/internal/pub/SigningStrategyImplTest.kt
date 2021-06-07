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

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SigningStrategyImplTest : FunSpec({

  test("Default value") {
      SigningStrategyImpl().apply {
          shouldSignOrNot() shouldBe true
          isSignWithKeybox() shouldBe true
          isSignWithKeyring() shouldBe false
      }
  }

    test("Do not sign") {

        SigningStrategyImpl().apply {
            doNotSign()
            shouldSignOrNot() shouldBe false
            isSignWithKeybox() shouldBe false
            isSignWithKeyring() shouldBe false
        }
    }

    test("Need signing") {

        SigningStrategyImpl().apply {
            needSign()
            shouldSignOrNot() shouldBe true
            isSignWithKeybox() shouldBe true
            isSignWithKeyring() shouldBe false
        }
    }

    test("sign with keybox") {
        SigningStrategyImpl().apply {
            needSign()
            signWithKeybox()
            isSignWithKeybox() shouldBe true
            isSignWithKeyring() shouldBe false
        }
    }

    test("sign with keyring") {
        SigningStrategyImpl().apply {
            needSign()
            signWithKeyring()
            isSignWithKeybox() shouldBe false
            isSignWithKeyring() shouldBe true
        }
    }

})
