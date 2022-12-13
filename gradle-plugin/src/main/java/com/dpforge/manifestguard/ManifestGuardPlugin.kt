package com.dpforge.manifestguard

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.Variant
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
@ExperimentalStdlibApi
class ManifestGuardPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension =
            project.extensions.create("manifestGuard", ManifestGuardExtension::class.java)

        project.plugins.withType(AppPlugin::class.java) {
            val androidComponents =
                project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)

            androidComponents.onVariants { appVariant ->
                val guardConfig = extension.getVariantConfiguration(appVariant.name)
                if (guardConfig.enabled.get()) {
                    handleApplicationVariant(project, appVariant, guardConfig)
                } else {
                    project.logger.lifecycle("Skip guarding build variant: ${appVariant.name}")
                }
            }
        }
    }

    private fun handleApplicationVariant(
        project: Project,
        variant: ApplicationVariant,
        extension: BuildVariantManifestGuardConfiguration
    ) {
        registerCompareTask(project, variant, extension)
        registerUpdateReferenceTask(project, variant, extension)
    }

    private fun registerCompareTask(
        project: Project,
        variant: ApplicationVariant,
        extension: BuildVariantManifestGuardConfiguration,
    ) {
        val taskProvider = project.tasks.register(
            "compare${variant.capitalizedName()}MergedManifest",
            CompareMergedManifestTask::class.java,
        ) { task ->
            task.group = "Manifest guard"
            task.description = "Compare ${variant.capitalizedName()} manifest with reference manifest"
            task.mergedManifestFile.set(variant.artifacts.get(SingleArtifact.MERGED_MANIFEST))
            task.referenceManifestFile.set(extension.referenceFile)
            task.htmlDiffFile.set(extension.htmlDiffFile)
            task.ignoreConfig.set(extension.ignoreConfig)
        }

        if (extension.compareOnAssemble.get()) {
            project.afterEvaluate {
                project.tasks.named("assemble${variant.capitalizedName()}").configure {
                    it.finalizedBy(taskProvider.get())
                }
            }
        }
    }

    private fun registerUpdateReferenceTask(
        project: Project,
        variant: ApplicationVariant,
        extension: BuildVariantManifestGuardConfiguration,
    ) {
        project.tasks.register(
            "update${variant.capitalizedName()}ReferenceManifest",
            UpdateReferenceManifestTask::class.java,
        ) { task ->
            task.group = "Manifest guard"
            task.description = "Update manifest guard reference with ${variant.capitalizedName()}"
            task.mergedManifestFile.set(variant.artifacts.get(SingleArtifact.MERGED_MANIFEST))
            task.referenceManifestFile.set(extension.referenceFile)
        }
    }

    private fun Variant.capitalizedName(): String = name.replaceFirstChar { it.uppercaseChar() }
}
