package com.dpforge.manifestguard

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

@ExperimentalStdlibApi
class ManifestGuardPlugin : Plugin<Project> {

    private lateinit var extension: ManifestGuardExtension

    override fun apply(project: Project) {
        extension = project.extensions.create("manifestGuard", ManifestGuardExtension::class.java)

        // This works
        project.afterEvaluate {
            onProjectEvaluate(it)
        }

        // This doesn't work
//        onProjectEvaluate(project)
    }

    private fun onProjectEvaluate(project: Project) {
        project.logger.debug("Manifest Guard plugin has been applied")

        val isAndroid = project.plugins.withType(AppPlugin::class.java).isNotEmpty()
        if (!isAndroid) {
            project.logger.error(
                "Manifest Guard plugin can be applied only to Android application modules. " +
                        "Make sure you've applied 'com.android.application' plugin."
            )
        }

        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            val taskProvider = project.tasks.register(
                "compare${variant.name}MergedManifest",
                CompareMergedManifestTask::class.java,
                buildCompareTaskArgs(project),
            )

            variant.artifacts.use(taskProvider)
                .wiredWithFiles(
                    CompareMergedManifestTask::mergedManifestFileIn,
                    CompareMergedManifestTask::mergedManifestFileOut
                )
                .toTransform(SingleArtifact.MERGED_MANIFEST)
        }
    }

    private fun buildCompareTaskArgs(project: Project) =
        CompareMergedManifestTask.Args(
            referenceManifestFile = extension.referenceFile.orNull ?: Defaults.defaultReferenceManifestFile(project),
            htmlDiffFile = extension.htmlDiffFile.orNull ?: Defaults.defaultHtmlDiffFile(project)
        )

    private object Defaults {
        fun defaultReferenceManifestFile(project: Project) =
            project.rootDir.resolve("GuardedAndroidManifest.xml")

        fun defaultHtmlDiffFile(project: Project) =
            project.buildDir.resolve("outputs").resolve("manifest_guard").resolve("diff.html")
    }

}
