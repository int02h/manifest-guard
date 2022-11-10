package com.dpforge.manifestguard

import com.dpforge.manifestguard.ManifestGuardConfiguration.IgnoreConfig
import org.gradle.api.Action
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class DefaultManifestGuardConfiguration @Inject constructor(
    objects: ObjectFactory,
    layout: ProjectLayout
) : ManifestGuardConfiguration {

    override val enabled: Property<Boolean> = objects.property(Boolean::class.java).convention(true)

    override val compareOnAssemble: Property<Boolean> = objects.property(Boolean::class.java).convention(true)

    override val referenceFile: RegularFileProperty = objects.fileProperty().convention(
        layout.projectDirectory.file(
            "GuardedAndroidManifest.xml"
        )
    )

    override val htmlDiffFile: RegularFileProperty = objects.fileProperty().convention(
        layout.buildDirectory.file(
            "outputs/manifest_guard/diff.html"
        )
    )

    override val ignoreConfig: IgnoreConfig = objects.newInstance(IgnoreConfig::class.java)

    override fun ignore(action: Action<IgnoreConfig>) {
        action.execute(ignoreConfig)
    }
}