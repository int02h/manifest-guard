package com.dpforge.manifestguard

import org.gradle.api.Action
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import java.io.Serializable
import javax.inject.Inject

abstract class ManifestGuardExtension @Inject constructor(
    objects: ObjectFactory,
    layout: ProjectLayout
) {

    val compareOnAssemble: Property<Boolean> = objects.property(Boolean::class.java).convention(true)

    val referenceFile: RegularFileProperty = objects.fileProperty().convention(
        layout.projectDirectory.file(
            "GuardedAndroidManifest.xml"
        )
    )

    val htmlDiffFile: RegularFileProperty = objects.fileProperty().convention(
        layout.buildDirectory.file(
            "outputs/manifest_guard/diff.html"
        )
    )

    val ignore: IgnoreConfig = objects.newInstance(IgnoreConfig::class.java)

    fun ignore(action: Action<IgnoreConfig>) {
        action.execute(ignore)
    }

    open class IgnoreConfig : Serializable {
        var ignoreAppVersionChanges: Boolean = false
    }

}
