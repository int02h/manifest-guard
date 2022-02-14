package com.dpforge.manifestguard

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
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
                val taskProvider = project.tasks.register(
                    "compare${variant.capitalizedName()}MergedManifest",
                    CompareMergedManifestTask::class.java,
                ) { task ->
                    task.referenceManifestFile.set(extension.referenceFile)
                    task.htmlDiffFile.set(extension.htmlDiffFile)
                    task.ignoreConfig.set(extension.ignore)
                }

                // The task is registered as artifact transformer because it's the only way not to rely on
                // implementation details (like merge task name or type)
                variant.artifacts.use(taskProvider)
                    .wiredWithFiles(
                        CompareMergedManifestTask::mergedManifestFileIn,
                        CompareMergedManifestTask::mergedManifestFileOut,
                    )
                    .toTransform(SingleArtifact.MERGED_MANIFEST)
            }
        }
    }

    private fun Variant.capitalizedName(): String = name.replaceFirstChar { it.uppercaseChar() }
}
