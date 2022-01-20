package com.dpforge.manifestguard

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.tasks.ProcessApplicationManifest
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

@ExperimentalStdlibApi
class ManifestGuardPlugin : Plugin<Project> {

    private lateinit var extension: ManifestGuardExtension

    override fun apply(project: Project) {
        extension = project.extensions.create("manifestGuard", ManifestGuardExtension::class.java)
        project.afterEvaluate {
            onProjectEvaluate(it)
        }
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

        project.allprojects.forEach { p ->
            p.afterEvaluate { onSubprojectEvaluate(p) }
        }
    }

    private fun onSubprojectEvaluate(p: Project) {
        p.tasks.withType(ProcessApplicationManifest::class.java)
            .forEach { mergeTask ->
                val variant = mergeTask.variantName.replaceFirstChar { it.uppercase() }
                val mergedManifestFile = mergeTask.mergedManifest.orNull?.asFile ?: error("No File for merged manifest")
                val storeTask = p.tasks.create(
                    "compare${variant}MergedManifest",
                    CompareMergedManifestTask::class.java,
                    buildCompareTaskArgs(p, mergedManifestFile),
                )
                mergeTask.finalizedBy(storeTask)
            }
    }

    private fun buildCompareTaskArgs(project: Project, mergedManifestFile: File) =
        CompareMergedManifestTask.Args(
            mergedManifestFile = mergedManifestFile,
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
