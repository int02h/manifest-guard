package com.dpforge.manifestguard.dsl

import org.gradle.api.provider.Property

interface IgnoreConfig {
    val ignoreAppVersionChanges: Property<Boolean>
}
