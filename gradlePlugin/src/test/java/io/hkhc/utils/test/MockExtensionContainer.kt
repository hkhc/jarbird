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

import org.gradle.api.Action
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.ExtensionsSchema
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.reflect.TypeOf

class MockExtensionContainer : ExtensionContainer {

    var mockExtensions: Map<Class<out Any>, Any> = mutableMapOf()

    override fun <T : Any?> add(publicType: Class<T>, name: String, extension: T) {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> add(publicType: TypeOf<T>, name: String, extension: T) {
        TODO("Not yet implemented")
    }

    override fun add(name: String, extension: Any) {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> create(
        publicType: Class<T>,
        name: String,
        instanceType: Class<out T>,
        vararg constructionArguments: Any?
    ): T {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> create(
        publicType: TypeOf<T>,
        name: String,
        instanceType: Class<out T>,
        vararg constructionArguments: Any?
    ): T {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> create(name: String, type: Class<T>, vararg constructionArguments: Any?): T {
        TODO("Not yet implemented")
    }

    override fun getExtensionsSchema(): ExtensionsSchema {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> getByType(type: Class<T>): T {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> getByType(type: TypeOf<T>): T {
        TODO("Not yet implemented")
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> findByType(type: Class<T>): T? {
        return mockExtensions[type] as T?
    }

    override fun <T : Any?> findByType(type: TypeOf<T>): T? {
        TODO("Not yet implemented")
    }

    override fun getByName(name: String): Any {
        TODO("Not yet implemented")
    }

    override fun findByName(name: String): Any? {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> configure(type: Class<T>, action: Action<in T>) {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> configure(type: TypeOf<T>, action: Action<in T>) {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> configure(name: String, action: Action<in T>) {
        TODO("Not yet implemented")
    }

    override fun getExtraProperties(): ExtraPropertiesExtension {
        TODO("Not yet implemented")
    }
}
