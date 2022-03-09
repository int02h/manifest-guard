package com.dpforge.manifestguard

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.Variant
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

@ExperimentalStdlibApi
class ManifestGuardPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension =
            project.extensions.create("manifestGuard", ManifestGuardExtension::class.java)

        project.plugins.withType(AppPlugin::class.java) {

            val androidComponents =
                project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)

            androidComponents.onVariants { variant ->
                handleApplicationVariant(project, variant, extension)
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
            "compare${variant.capitalizedName()}MergedManifest",
            CompareMergedManifestTask::class.java,
        ) { task ->
            task.mergedManifestFile.set(variant.artifacts.get(SingleArtifact.MERGED_MANIFEST))
            task.referenceManifestFile.set(extension.referenceFile)
            task.htmlDiffFile.set(extension.htmlDiffFile)
            task.ignoreConfig.set(extension.ignore)
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
        extension: ManifestGuardExtension,
    ) {
        project.tasks.register(
            "update${variant.capitalizedName()}ReferenceManifest",
            UpdateReferenceManifestTask::class.java,
        ) { task ->
            task.mergedManifestFile.set(variant.artifacts.get(SingleArtifact.MERGED_MANIFEST))
            task.referenceManifestFile.set(extension.referenceFile)
        }
    }

    private fun Variant.capitalizedName(): String = name.replaceFirstChar { it.uppercaseChar() }
}
