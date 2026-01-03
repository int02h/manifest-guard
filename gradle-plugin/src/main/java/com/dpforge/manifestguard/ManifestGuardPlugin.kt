package com.dpforge.manifestguard

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.DslExtension
import com.android.build.api.variant.Variant
import com.android.build.gradle.AppPlugin
import com.dpforge.manifestguard.dsl.ManifestGuardConfiguration
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
@ExperimentalStdlibApi
class ManifestGuardPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.withType(AppPlugin::class.java) {
            val androidComponents =
                project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)

            androidComponents.registerExtension(
                DslExtension.Builder("manifestGuard")
                    .extendBuildTypeWith(ManifestGuardConfiguration::class.java)
                    .extendProductFlavorWith(ManifestGuardConfiguration::class.java)
                    .build()
            ) { variantExt ->
                project.objects.newInstance(ManifestGuardExtension::class.java, variantExt)
            }

            androidComponents.onVariants { appVariant ->
                val guardExtension = appVariant.getExtension(ManifestGuardExtension::class.java) ?: error("Can't find plugin extension")
                val guardEnabled = guardExtension.enabled.get()

                if (guardEnabled) {
                    handleApplicationVariant(project, appVariant, guardExtension)
                } else {
                    project.logger.lifecycle("Skip guarding build variant: ${appVariant.name}")
                }
            }
        }
    }

    private fun handleApplicationVariant(
        project: Project,
        variant: ApplicationVariant,
        extension: ManifestGuardExtension
    ) {
        registerCompareTask(project, variant, extension)
        registerUpdateReferenceTask(project, variant, extension)
    }

    private fun registerCompareTask(
        project: Project,
        variant: ApplicationVariant,
        extension: ManifestGuardExtension,
    ) {
        val taskProvider = project.tasks.register(
            "compare${variant.capitalizedName}MergedManifest",
            CompareMergedManifestTask::class.java,
        ) { task ->
            val ignoreParms = project.objects.newInstance(IgnoreParams::class.java).apply {
                ignoreAppVersionChanges.set(extension.ignoreAppVersionChanges)
            }

            task.group = "Manifest guard"
            task.description = "Compare ${variant.capitalizedName} manifest with reference manifest"
            task.mergedManifestFile.set(variant.artifacts.get(SingleArtifact.MERGED_MANIFEST))
            task.referenceManifestFile.set(extension.referenceFile)
            task.htmlDiffFile.set(extension.htmlDiffFile)
            task.ignoreConfig.set(ignoreParms)
        }

        if (extension.compareOnAssemble.get()) {
            project.afterEvaluate {
                project.tasks.named("assemble${variant.capitalizedName}").configure {
                    it.finalizedBy(taskProvider.get())
                }
            }
        }
    }

    private fun registerUpdateReferenceTask(
        project: Project,
        variant: ApplicationVariant,
        extension: ManifestGuardExtension,
    ) {
        project.tasks.register(
            "update${variant.capitalizedName}ReferenceManifest",
            UpdateReferenceManifestTask::class.java,
        ) { task ->
            task.group = "Manifest guard"
            task.description = "Update manifest guard reference with ${variant.capitalizedName}"
            task.mergedManifestFile.set(variant.artifacts.get(SingleArtifact.MERGED_MANIFEST))
            task.referenceManifestFile.set(extension.referenceFile)
        }
    }

    private val Variant.capitalizedName: String
        get() = name.replaceFirstChar(Char::uppercaseChar)
}
