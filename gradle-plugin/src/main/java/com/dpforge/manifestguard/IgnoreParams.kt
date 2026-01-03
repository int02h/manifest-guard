package com.dpforge.manifestguard

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import java.io.Serializable

interface IgnoreParams : Serializable {
    @get:Input
    val ignoreAppVersionChanges: Property<Boolean>
}
