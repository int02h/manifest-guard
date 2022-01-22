package com.dpforge.manifestguard

import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

abstract class ManifestGuardExtension @Inject constructor(
    objects: ObjectFactory,
    layout: ProjectLayout
) {
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
}
