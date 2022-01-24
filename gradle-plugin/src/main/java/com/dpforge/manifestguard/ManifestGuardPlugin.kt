package com.dpforge.manifestguard

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class ManifestGuardPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension =
            project.extensions.create("manifestGuard", ManifestGuardExtension::class.java)

        project.plugins.withType(AppPlugin::class.java) {

            val androidComponents =
                project.extensions.getByType(AndroidComponentsExtension::class.java)

            androidComponents.onVariants { variant ->
                val taskProvider = project.tasks.register(
                    "compare${variant.name}MergedManifest",
                    CompareMergedManifestTask::class.java,
                ) { task ->
                    task.referenceManifestFileIn.set(extension.referenceFile)
                    task.htmlDiffFileIn.set(extension.htmlDiffFile)
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
}
