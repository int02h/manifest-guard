package com.dpforge.manifestguard

import org.gradle.api.Action
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import javax.inject.Inject

interface ManifestGuardConfiguration {
    val enabled: Property<Boolean>
    val compareOnAssemble: Property<Boolean>
    val referenceFile: RegularFileProperty
    val htmlDiffFile: RegularFileProperty
    val ignoreConfig: IgnoreConfig

    abstract class IgnoreConfig @Inject constructor(objects: ObjectFactory) {
        @Input
        val ignoreAppVersionChanges: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    }

    fun ignore(action: Action<IgnoreConfig>)
}