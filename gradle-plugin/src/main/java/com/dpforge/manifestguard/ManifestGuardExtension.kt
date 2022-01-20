package com.dpforge.manifestguard

import org.gradle.api.provider.Property
import java.io.File

interface ManifestGuardExtension {
    val referenceFile: Property<File>
    val htmlDiffFile: Property<File>
}