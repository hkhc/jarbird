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

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.AntBuilder
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.logging.LoggingManager
import org.gradle.api.plugins.Convention
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.TaskDependency
import org.gradle.api.tasks.TaskDestroyables
import org.gradle.api.tasks.TaskInputs
import org.gradle.api.tasks.TaskLocalState
import org.gradle.api.tasks.TaskOutputs
import org.gradle.api.tasks.TaskState
import java.io.File
import java.time.Duration

@Suppress("UnsafeCallOnNullableType")
class MockTask : Task {

    lateinit var mockProject: Project
    var mockName: String = ""
    var mockGroup: String? = null
    var mockDescription: String? = null
    var mockDependsTask = mutableSetOf<Any>()

    override fun compareTo(other: Task?): Int {
        TODO("Not yet implemented")
    }

    override fun getExtensions(): ExtensionContainer {
        TODO("Not yet implemented")
    }

    override fun getName() = mockName

    override fun getProject() = mockProject

    override fun getActions(): MutableList<Action<in Task>> {
        TODO("Not yet implemented")
    }

    override fun setActions(actions: MutableList<Action<in Task>>) {
        TODO("Not yet implemented")
    }

    override fun getTaskDependencies(): TaskDependency {
        TODO("Not yet implemented")
    }

    override fun getDependsOn() = mockDependsTask
    @Suppress("UNCHECKED_CAST")
    override fun setDependsOn(dependsOnTasks: MutableIterable<*>) {
        mockDependsTask = mutableSetOf<Any>().apply {
            addAll(dependsOnTasks as MutableIterable<Any>)
        }
    }

    override fun dependsOn(vararg paths: Any?): Task {
        paths.filterNotNull().forEach {
            mockDependsTask.add(it)
        }
        return this
    }

    override fun onlyIf(onlyIfClosure: Closure<*>) {
        TODO("Not yet implemented")
    }

    override fun onlyIf(onlyIfSpec: Spec<in Task>) {
        TODO("Not yet implemented")
    }

    override fun setOnlyIf(onlyIfClosure: Closure<*>) {
        TODO("Not yet implemented")
    }

    override fun setOnlyIf(onlyIfSpec: Spec<in Task>) {
        TODO("Not yet implemented")
    }

    override fun getState(): TaskState {
        TODO("Not yet implemented")
    }

    override fun setDidWork(didWork: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getDidWork(): Boolean {
        TODO("Not yet implemented")
    }

    private fun getPathPrefix(p: Project): String {
        return if (p.rootProject !== p) {
            "${getPathPrefix(p.parent!!)}:${p.name}"
        } else {
            ""
        }
    }

    override fun getPath(): String {
        return "${getPathPrefix(project)}:$name"
    }

    override fun doFirst(action: Action<in Task>): Task {
        TODO("Not yet implemented")
    }

    override fun doFirst(action: Closure<*>): Task {
        TODO("Not yet implemented")
    }

    override fun doFirst(actionName: String, action: Action<in Task>): Task {
        TODO("Not yet implemented")
    }

    override fun doLast(action: Action<in Task>): Task {
        TODO("Not yet implemented")
    }

    override fun doLast(actionName: String, action: Action<in Task>): Task {
        TODO("Not yet implemented")
    }

    override fun doLast(action: Closure<*>): Task {
        TODO("Not yet implemented")
    }

    override fun getEnabled(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setEnabled(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun configure(configureClosure: Closure<*>): Task {
        TODO("Not yet implemented")
    }

    override fun getAnt(): AntBuilder {
        TODO("Not yet implemented")
    }

    override fun getLogger(): Logger {
        TODO("Not yet implemented")
    }

    override fun getLogging(): LoggingManager {
        TODO("Not yet implemented")
    }

    override fun property(propertyName: String): Any? {
        TODO("Not yet implemented")
    }

    override fun hasProperty(propertyName: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun setProperty(name: String, value: Any) {
        TODO("Not yet implemented")
    }

    override fun getConvention(): Convention {
        TODO("Not yet implemented")
    }

    override fun getDescription() = mockDescription
    override fun setDescription(description: String?) {
        mockDescription = description
    }

    override fun getGroup() = mockGroup
    override fun setGroup(group: String?) {
        mockGroup = group
    }

    override fun getInputs(): TaskInputs {
        TODO("Not yet implemented")
    }

    override fun getOutputs(): TaskOutputs {
        TODO("Not yet implemented")
    }

    override fun getDestroyables(): TaskDestroyables {
        TODO("Not yet implemented")
    }

    override fun getLocalState(): TaskLocalState {
        TODO("Not yet implemented")
    }

    override fun getTemporaryDir(): File {
        TODO("Not yet implemented")
    }

    override fun mustRunAfter(vararg paths: Any?): Task {
        TODO("Not yet implemented")
    }

    override fun setMustRunAfter(mustRunAfter: MutableIterable<*>) {
        TODO("Not yet implemented")
    }

    override fun getMustRunAfter(): TaskDependency {
        TODO("Not yet implemented")
    }

    override fun finalizedBy(vararg paths: Any?): Task {
        TODO("Not yet implemented")
    }

    override fun setFinalizedBy(finalizedBy: MutableIterable<*>) {
        TODO("Not yet implemented")
    }

    override fun getFinalizedBy(): TaskDependency {
        TODO("Not yet implemented")
    }

    override fun shouldRunAfter(vararg paths: Any?): TaskDependency {
        TODO("Not yet implemented")
    }

    override fun setShouldRunAfter(shouldRunAfter: MutableIterable<*>) {
        TODO("Not yet implemented")
    }

    override fun getShouldRunAfter(): TaskDependency {
        TODO("Not yet implemented")
    }

    override fun getTimeout(): Property<Duration> {
        TODO("Not yet implemented")
    }

    override fun usesService(service: Provider<out BuildService<*>>) {
        TODO("Not yet implemented")
    }
}
