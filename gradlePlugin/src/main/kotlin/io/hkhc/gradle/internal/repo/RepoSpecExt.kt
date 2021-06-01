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

package io.hkhc.gradle.internal.repo

import io.hkhc.gradle.JarbirdPub
import org.gradle.api.GradleException

fun RemoteRepoSpec.effectiveUrl(pub: JarbirdPub): String {
    return if (pub.pom.isSnapshot()) {
        snapshotUrl.ifEmpty {
            throw GradleException("Snapshot URL of the repo '$id' is not provided")
        }
    } else {
        releaseUrl.ifEmpty {
            throw GradleException("Release URL of the repo '$id' is not provided")
        }
    }
}
