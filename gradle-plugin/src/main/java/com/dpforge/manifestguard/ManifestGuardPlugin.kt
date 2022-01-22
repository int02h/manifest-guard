package com.dpforge.manifestguard

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
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
                project.extensions.getByType(AndroidComponentsExtension::class.java)

            androidComponents.onVariants { variant ->

                project.tasks.register(
                    "compare${variant.name}MergedManifest",
                    CompareMergedManifestTask::class.java,
                ) { task ->
                    task.mergedManifestFileIn.set(variant.artifacts.get(SingleArtifact.MERGED_MANIFEST))
                    task.referenceManifestFileIn.set(extension.referenceFile)
                    task.htmlDiffFileIn.set(extension.htmlDiffFile)
                }
            }
        }
    }
}
