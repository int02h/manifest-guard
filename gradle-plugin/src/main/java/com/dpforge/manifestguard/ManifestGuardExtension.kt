package com.dpforge.manifestguard

import com.android.build.api.variant.VariantExtension
import com.android.build.api.variant.VariantExtensionConfig
import com.dpforge.manifestguard.dsl.ManifestGuardConfiguration
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import java.io.Serializable
import javax.inject.Inject

abstract class ManifestGuardExtension @Inject constructor(
    objects: ObjectFactory,
    layout: ProjectLayout,
    extension: VariantExtensionConfig<*>
) : VariantExtension, Serializable {
    val enabled: Property<Boolean> = objects.property(Boolean::class.java)
    val compareOnAssemble: Property<Boolean> = objects.property(Boolean::class.java)
    val referenceFile: RegularFileProperty = objects.fileProperty()
    val htmlDiffFile: RegularFileProperty = objects.fileProperty()
    val ignoreAppVersionChanges: Property<Boolean> = objects.property(Boolean::class.java)

    /**
     * Initializes the resolved state for this variant by merging configurations
     * from the Variant Hierarchy.
     *
     * This block implements a "Cascade Resolution Strategy" to determine the final
     * value of each property. The priority order is defined as follows (Highest to Lowest):
     *
     * 1. **Product Flavors**: Configuration specific to the active flavor (e.g., `demo`, `paid`).
     * - If multiple flavor dimensions exist, they are prioritized by their dimension order.
     * 2. **Build Type**: Configuration specific to the active build type (e.g., `debug`, `release`).
     * 3. **Default Value**: The fallback value defined in the `chain` function if no other configuration is set.
     *
     * The `.chain()` helper method links these providers using `orElse()`, ensuring
     * that the first configured value in the hierarchy is the one used.
     */
    init {
        val buildConfig = extension.buildTypeExtension(ManifestGuardConfiguration::class.java)
        val productConfigs = extension.productFlavorsExtensions(ManifestGuardConfiguration::class.java)
        val allConfigs = productConfigs + buildConfig

        layout.mergeConfigs(allConfigs)
    }

    private fun ProjectLayout.mergeConfigs(configs: Collection<ManifestGuardConfiguration>) {
        enabled.convention(
            configs.chain(
                default = true,
                selector = ManifestGuardConfiguration::enabled)
        )
        compareOnAssemble.convention(
            configs.chain(
                default = true,
                selector = ManifestGuardConfiguration::compareOnAssemble
            )
        )
        referenceFile.convention(
            configs.chain(
                default = projectDirectory.file("GuardedAndroidManifest.xml"),
                selector = ManifestGuardConfiguration::referenceFile
            )
        )
        htmlDiffFile.convention(
            configs.chain(
                default = buildDirectory.file("outputs/manifest_guard/diff.html"),
                selector = ManifestGuardConfiguration::htmlDiffFile
            )
        )
        ignoreAppVersionChanges.convention(
            configs.chain(default = false) { ignore.ignoreAppVersionChanges }
        )
    }

    private inline fun <T> Collection<ManifestGuardConfiguration>.chain(
        default: T & Any,
        selector: ManifestGuardConfiguration.() -> Provider<T>
    ) = map { it.selector() }.reduce { acc, next -> acc.orElse(next) }.orElse(default)

    private inline fun <T> Collection<ManifestGuardConfiguration>.chain(
        default: Provider<T>,
        selector: ManifestGuardConfiguration.() -> Provider<T>
    ) = map { it.selector() }.reduce { acc, next -> acc.orElse(next) }.orElse(default)
}
