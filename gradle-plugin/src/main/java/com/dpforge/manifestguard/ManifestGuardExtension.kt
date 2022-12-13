package com.dpforge.manifestguard

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

abstract class ManifestGuardExtension @Inject constructor(
    objects: ObjectFactory,
) {

    private val defaultConfig = objects.newInstance(
        DefaultManifestGuardConfiguration::class.java,
    )

    private val variantsConfiguration = objects.domainObjectContainer(
        BuildVariantManifestGuardConfiguration::class.java
    ) { name ->
        objects.newInstance(BuildVariantManifestGuardConfiguration::class.java, name, defaultConfig)
    }

    @Suppress("unused")
    fun defaultConfig(action: Action<ManifestGuardConfiguration>) {
        action.execute(defaultConfig)
    }

    @Suppress("unused")
    fun guardVariant(variantName: String, action: Action<ManifestGuardConfiguration>) {
        variantsConfiguration.register(variantName).configure(action)
    }

    internal fun getVariantConfiguration(name: String): BuildVariantManifestGuardConfiguration {
        return variantsConfiguration.maybeCreate(name)
    }
}
