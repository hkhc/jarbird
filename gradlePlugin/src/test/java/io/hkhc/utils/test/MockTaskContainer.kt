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
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.Action
import org.gradle.api.DomainObjectCollection
import org.gradle.api.NamedDomainObjectCollectionSchema
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Namer
import org.gradle.api.Project
import org.gradle.api.Rule
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.invoke
import java.util.SortedMap
import java.util.SortedSet

class MockTaskContainer(val mockProject: Project): TaskContainer {

    var mockTasks = mutableListOf<Task>()

    var mockWithTypeTasks: Map<Class<out Task>,Task> = mutableMapOf()

    override fun add(element: Task): Boolean {
        TODO("Not yet implemented")
    }

    override fun addAll(elements: Collection<Task>): Boolean {
        TODO("Not yet implemented")
    }

    override fun contains(element: Task?): Boolean {
        TODO("Not yet implemented")
    }

    override fun containsAll(elements: Collection<Task>): Boolean {
        TODO("Not yet implemented")
    }

    override fun isEmpty(): Boolean {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun iterator(): MutableIterator<Task> {
        TODO("Not yet implemented")
    }

    override fun remove(element: Task?): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeAll(elements: Collection<Task>): Boolean {
        TODO("Not yet implemented")
    }

    override fun retainAll(elements: Collection<Task>): Boolean {
        TODO("Not yet implemented")
    }

    override fun addLater(provider: Provider<out Task>) {
        TODO("Not yet implemented")
    }

    override fun addAllLater(provider: Provider<out MutableIterable<Task>>) {
        TODO("Not yet implemented")
    }

    @Suppress(
        "NULLABLE_TYPE_PARAMETER_AGAINST_NOT_NULL_TYPE_PARAMETER",
        "UNCHECKED_CAST",
        "TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING"
    )
    override fun <S : Task?> withType(type: Class<S>): TaskCollection<S> {
        return mockk {
            every { configureEach(any()) } answers {
                val action = firstArg<Action<S>>()
                action.execute(mockWithTypeTasks[type] as S)
            }
        }
    }

    override fun <S : Task?> withType(type: Class<S>, configureAction: Action<in S>): DomainObjectCollection<S> {
        TODO("Not yet implemented")
    }

    override fun <S : Task?> withType(type: Class<S>, configureClosure: Closure<*>): DomainObjectCollection<S> {
        TODO("Not yet implemented")
    }

    override fun matching(spec: Spec<in Task>): TaskCollection<Task> {
        TODO("Not yet implemented")
    }

    override fun matching(closure: Closure<*>): TaskCollection<Task> {
        TODO("Not yet implemented")
    }

    override fun whenObjectAdded(action: Action<in Task>): Action<in Task> {
        TODO("Not yet implemented")
    }

    override fun whenObjectAdded(action: Closure<*>) {
        TODO("Not yet implemented")
    }

    override fun whenObjectRemoved(action: Action<in Task>): Action<in Task> {
        TODO("Not yet implemented")
    }

    override fun whenObjectRemoved(action: Closure<*>) {
        TODO("Not yet implemented")
    }

    override fun all(action: Action<in Task>) {
        TODO("Not yet implemented")
    }

    override fun all(action: Closure<*>) {
        TODO("Not yet implemented")
    }

    override fun configureEach(action: Action<in Task>) {
        TODO("Not yet implemented")
    }

    override fun findAll(spec: Closure<*>): MutableSet<Task> {
        TODO("Not yet implemented")
    }

    override fun getNamer(): Namer<Task> {
        TODO("Not yet implemented")
    }

    override fun getAsMap(): SortedMap<String, Task> {
        TODO("Not yet implemented")
    }

    override fun getNames(): SortedSet<String> {
        TODO("Not yet implemented")
    }

    override fun findByName(name: String): Task? {
        return mockTasks.find { it.name == name }
    }

    override fun getByName(name: String, configureClosure: Closure<*>): Task {
        TODO("Not yet implemented")
    }

    override fun getByName(name: String): Task {
        TODO("Not yet implemented")
    }

    override fun getByName(name: String, configureAction: Action<in Task>): Task {
        TODO("Not yet implemented")
    }

    override fun getAt(name: String): Task {
        TODO("Not yet implemented")
    }

    override fun addRule(rule: Rule): Rule {
        TODO("Not yet implemented")
    }

    override fun addRule(description: String, ruleAction: Closure<*>): Rule {
        TODO("Not yet implemented")
    }

    override fun addRule(description: String, ruleAction: Action<String>): Rule {
        TODO("Not yet implemented")
    }

    override fun getRules(): MutableList<Rule> {
        TODO("Not yet implemented")
    }

    override fun named(name: String): TaskProvider<Task> {
        TODO("Not yet implemented")
    }

    override fun named(name: String, configurationAction: Action<in Task>): TaskProvider<Task> {
        TODO("Not yet implemented")
    }

    override fun <S : Task?> named(name: String, type: Class<S>): TaskProvider<S> {
        TODO("Not yet implemented")
    }

    override fun <S : Task?> named(name: String, type: Class<S>, configurationAction: Action<in S>): TaskProvider<S> {
        TODO("Not yet implemented")
    }

    override fun getCollectionSchema(): NamedDomainObjectCollectionSchema {
        TODO("Not yet implemented")
    }

    override fun whenTaskAdded(action: Action<in Task>): Action<in Task> {
        TODO("Not yet implemented")
    }

    override fun whenTaskAdded(closure: Closure<*>) {
        TODO("Not yet implemented")
    }

    override fun configure(configureClosure: Closure<*>): NamedDomainObjectContainer<Task> {
        TODO("Not yet implemented")
    }

    override fun create(options: MutableMap<String, *>): Task {
        TODO("Not yet implemented")
    }

    override fun create(options: MutableMap<String, *>, configureClosure: Closure<*>): Task {
        TODO("Not yet implemented")
    }

    override fun create(name: String, configureClosure: Closure<*>): Task {
        TODO("Not yet implemented")
    }

    override fun create(name: String): Task {
        TODO("Not yet implemented")
    }

    override fun <T : Task?> create(name: String, type: Class<T>): T {
        TODO("Not yet implemented")
    }

    override fun <T : Task?> create(name: String, type: Class<T>, vararg constructorArgs: Any?): T {
        TODO("Not yet implemented")
    }

    override fun <T : Task?> create(name: String, type: Class<T>, configuration: Action<in T>): T {
        TODO("Not yet implemented")
    }

    override fun create(name: String, configureAction: Action<in Task>): Task {
        TODO("Not yet implemented")
    }

    override fun <U : Task?> maybeCreate(name: String, type: Class<U>): U {
        TODO("Not yet implemented")
    }

    override fun maybeCreate(name: String): Task {
        TODO("Not yet implemented")
    }

    override fun register(name: String, configurationAction: Action<in Task>): TaskProvider<Task> {
        return mockk {
            every { getName() } returns name
            every { get() } answers {
                val task = MockTask()
                task.mockName = name
                task.mockProject = mockProject
                mockTasks.add(task)
                configurationAction.invoke(task)
                task
            }
        }
    }

    override fun <T : Task?> register(
        name: String,
        type: Class<T>,
        configurationAction: Action<in T>
    ): TaskProvider<T> {
        return mockk {
            every { getName() } returns name
            every { get() } answers {
                val task = MockTask()
                task.mockName = name
                task.mockProject = mockProject
                mockTasks.add(task)
                configurationAction.invoke(task as T)
                task
            }
        }
    }

    override fun <T : Task?> register(name: String, type: Class<T>): TaskProvider<T> {
        TODO("Not yet implemented")
    }

    override fun <T : Task?> register(name: String, type: Class<T>, vararg constructorArgs: Any?): TaskProvider<T> {
        TODO("Not yet implemented")
    }

    override fun register(name: String): TaskProvider<Task> {
        TODO("Not yet implemented")
    }

    override fun <U : Task?> containerWithType(type: Class<U>): NamedDomainObjectContainer<U> {
        TODO("Not yet implemented")
    }

    override fun findByPath(path: String): Task? {
//        println("findByPath ${path} in ${mockProject.name}")
//        println(" tasks " + mockTasks.joinToString(separator = ",") { it.path })
        return if (!path.contains(":"))
            findByName(path)
        else
            mockTasks.find { it.path == path }
    }

    override fun getByPath(path: String): Task {
        TODO("Not yet implemented")
    }

    override fun replace(name: String): Task {
        TODO("Not yet implemented")
    }

    override fun <T : Task?> replace(name: String, type: Class<T>): T {
        TODO("Not yet implemented")
    }

    override val size: Int
        get() = mockTasks.size
}
