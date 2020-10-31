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

    private val rawGroup = mutableMapOf<String, Pom>().apply {
        pomIterable.forEach { add(this, it) }
    }

    private val group: MutableMap<String, Pom> = mutableMapOf<String, Pom>().apply {

        val defaultPom = rawGroup[Pom.DEFAULT_VARIANT]

        rawGroup.forEach {
            val newPom = Pom(variant = it.key)
            defaultPom?.overlayTo(newPom)
            if (it.key != Pom.DEFAULT_VARIANT) {
                it.value.overlayTo(newPom)
            }
            add(this, newPom)
        }
    }

    private fun add(groups: MutableMap<String, Pom>, pom: Pom) {
        if (groups.containsKey(pom.variant)) {
            if (pom.variant == Pom.DEFAULT_VARIANT) {
                throw IllegalArgumentException("POM with default variant has already existed.")
            } else {
                throw IllegalArgumentException("POM with variant '${pom.variant}' has already existed.")
            }
        }
        groups[pom.variant] = pom
    }

    operator fun get(variant: String) = if (variant == "") group[Pom.DEFAULT_VARIANT] else group[variant]

    fun getDefault() = group[Pom.DEFAULT_VARIANT] ?: Pom()

    override fun overlayTo(other: Overlayable): Overlayable {

        (other as? PomGroup)?.overlaidFrom(this)

        return other
    }

    fun overlaidFrom(upstream: PomGroup) {
        val upstreamDefault = upstream.rawGroup[Pom.DEFAULT_VARIANT]
        val rawDefault = rawGroup[Pom.DEFAULT_VARIANT]
        val newDefault = Pom()

        rawDefault?.let {
            it.overlayTo(newDefault)
            group[Pom.DEFAULT_VARIANT] = newDefault
        }
        upstreamDefault?.let {
            it.overlayTo(newDefault)
            group[Pom.DEFAULT_VARIANT] = newDefault
        }

        val newDefault2 = group[Pom.DEFAULT_VARIANT]

        rawGroup.forEach {
            val newPom = Pom(variant = it.key)
            newDefault2?.overlayTo(newPom)
            if (it.key != Pom.DEFAULT_VARIANT) {
                it.value.overlayTo(newPom)
                upstream.rawGroup[it.key]?.overlayTo(newPom)
            }
            group[it.key] = newPom
        }
    }

    /* for unit test only */
    internal fun getMap(): Map<String, Pom> = group

    fun involveGradlePlugin() = group.any { it.value.isGradlePlugin() }

}
