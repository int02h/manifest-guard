package com.dpforge.manifestguard.dsl

import org.gradle.api.Action
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

interface ManifestGuardConfiguration {
    val enabled: Property<Boolean>
    val compareOnAssemble: Property<Boolean>
    val referenceFile: RegularFileProperty
    val htmlDiffFile: RegularFileProperty

    @get:Nested
    val ignore: IgnoreConfig

    @Suppress("unused")
    fun ignore(action: Action<IgnoreConfig>) {
        action.execute(ignore)
    }
}