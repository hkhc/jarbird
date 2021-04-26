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

package io.hkhc.gradle.pom

class PomGroup(pomIterable: Iterable<Pom> = listOf()) : Overlayable {

    private var defaultPom = Pom()

    /**
     * Directly map the initial POMs, default POM is not included
     */
    private val rawGroup = mutableMapOf<String, Pom>()

    /* Flattened groups */
    /**
     * merge the groups with default group. Default group is not present here,
     */
    private val group: MutableMap<String, Pom> = mutableMapOf()

    init {
        pomIterable.forEach { add(it) }
    }

    // defaultPom overlayTo raw group item to create new group item
    private fun updatePom(key: String) {
        val newPom = Pom(variant = key)
        rawGroup[key]?.let { rawRom ->
            defaultPom.overlayTo(newPom)
            rawRom.overlayTo(newPom)
            group[key] = newPom
        }
    }

    private fun add(pom: Pom) {
        if (pom.variant == Pom.DEFAULT_VARIANT) {
            defaultPom = pom
        } else {
            if (rawGroup.containsKey(pom.variant)) {
                throw IllegalArgumentException("POM with variant '${pom.variant}' has already existed.")
            }
            rawGroup[pom.variant] = pom
            updatePom(pom.variant)
        }
    }

    operator fun get(variant: String): Pom? = (if (variant == "") defaultPom else group[variant])

    fun getDefault() = defaultPom

    override fun overlayTo(other: Overlayable): Overlayable {

        (other as? PomGroup)?.overlaidFrom(this)

        return other
    }

    fun overlaidFrom(upstream: PomGroup) {

        upstream.defaultPom.overlayTo(defaultPom)

        rawGroup.forEach { rawEntry ->
            upstream.rawGroup[rawEntry.key]?.overlayTo(rawEntry.value)
            updatePom(rawEntry.key)
        }
        upstream.rawGroup.entries.filter { !rawGroup.containsKey(it.key) }.forEach {
            val newRawPom = Pom(variant = it.key)
            it.value.overlayTo(newRawPom)
            rawGroup[it.key] = newRawPom
            updatePom(it.key)
        }
    }

    /* for unit test only */
    internal fun getMap(): Map<String, Pom> = group

    fun involveGradlePlugin() = group.any { it.value.isGradlePlugin() }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("Default POM : $defaultPom\n")
        rawGroup.forEach {
            builder.append("Raw Group [${it.key}] : $it.value\n")
        }
        group.forEach {
            builder.append("Group [${it.key}] : $it.value\n")
        }
        return builder.toString()
    }

    fun formattedDump(): String{
        val builder = StringBuilder()
        builder.append("------ Default POM\n")
        builder.append(defaultPom.formattedDump())
        builder.append("\n")
        rawGroup.map { "------ Raw Group [${it.key}] : \n${it.value.formattedDump()}" }.forEach {
            builder.append(it)
        }
        builder.append("\n")
        group.map { "------ Group [${it.key}] : \n${it.value.formattedDump()}" }.forEach {
            builder.append(it)
        }
        builder.append("\n")
        return builder.toString()
    }

}
