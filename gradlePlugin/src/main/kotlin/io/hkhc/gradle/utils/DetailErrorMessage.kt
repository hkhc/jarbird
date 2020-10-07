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

package io.hkhc.gradle.utils

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logger

fun detailMessageError(logger: Logger, title: String, detail: String) {
    detailMessage(logger, logger::error, "ERROR", title, detail)
}

fun detailMessageWarning(logger: Logger, title: String, detail: String) {
    detailMessage(logger, logger::warn, "WARNING", title, detail)
}

fun detailMessage(logger: Logger, loggerBlock: (String) -> Unit, levelTag: String, title: String, detail: String) {
    loggerBlock.invoke("$levelTag: $LOG_PREFIX $title")
    logger.info("$LOG_PREFIX ------ Proposed action")
    detail.lineSequence().forEach { line ->
        logger.info("$LOG_PREFIX $line")
    }
    logger.info("$LOG_PREFIX ------")
}

fun fatalMessage(project: Project, msg: String) {
    project.logger.error("ERROR: $LOG_PREFIX $msg")
    throw GradleException(msg)
}
