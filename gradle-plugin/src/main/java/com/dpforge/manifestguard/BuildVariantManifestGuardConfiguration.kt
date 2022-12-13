package com.dpforge.manifestguard

import com.dpforge.manifestguard.ManifestGuardConfiguration.IgnoreConfig
import org.gradle.api.Action
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class BuildVariantManifestGuardConfiguration @Inject constructor(
    val name: String,
    default: ManifestGuardConfiguration,
    objects: ObjectFactory,
) : ManifestGuardConfiguration {

    override val enabled: Property<Boolean> = objects.property(Boolean::class.java).convention(default.enabled)

    override val compareOnAssemble: Property<Boolean> =
        objects.property(Boolean::class.java).convention(default.compareOnAssemble)

    override val referenceFile: RegularFileProperty = objects.fileProperty().convention(default.referenceFile)

    override val htmlDiffFile: RegularFileProperty = objects.fileProperty().convention(default.htmlDiffFile)

    override val ignoreConfig: IgnoreConfig = objects.newInstance(IgnoreConfig::class.java).also {
        it.ignoreAppVersionChanges.set(default.ignoreConfig.ignoreAppVersionChanges)
    }

    override fun ignore(action: Action<IgnoreConfig>) {
        action.execute(ignoreConfig)
    }
}