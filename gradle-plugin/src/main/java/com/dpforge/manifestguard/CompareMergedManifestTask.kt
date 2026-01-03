package com.dpforge.manifestguard

import com.dpforge.manifestguard.filter.AppVersionDiffFilter
import com.dpforge.manifestguard.manifest.ManifestDiffBuilder
import com.dpforge.manifestguard.manifest.ManifestDiffEntryFilter
import com.dpforge.manifestguard.report.HtmlDiffWriter
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

// todo @CacheableTask with test
abstract class CompareMergedManifestTask : DefaultTask() {

    @get:InputFile
    abstract val mergedManifestFile: RegularFileProperty

    // Use @InputFile instead of @InputFiles (https://github.com/gradle/gradle/issues/2016)
    @get:InputFiles
    abstract val referenceManifestFile: RegularFileProperty

    @get:OutputFile
    abstract val htmlDiffFile: RegularFileProperty

    @get:Nested
    abstract val ignoreConfig: Property<IgnoreParams>

    @TaskAction
    fun execute() {
        val htmlDiffFile = htmlDiffFile.get().asFile
        htmlDiffFile.delete()

        val referenceManifestFile = referenceManifestFile.get().asFile
        val mergedManifestFile = mergedManifestFile.get().asFile

        logger.info("Merged AndroidManifest.xml: $mergedManifestFile")
        logger.info("Reference AndroidManifest.xml: $referenceManifestFile")

        if (referenceManifestFile.exists()) {
            compareManifests(
                referenceManifestFile = referenceManifestFile,
                mergedManifestFile = mergedManifestFile,
                htmlDiffFile = htmlDiffFile,
            )
        } else {
            logger.info("Reference AndroidManifest.xml does not exist. Just creating it.")
            mergedManifestFile.copyTo(referenceManifestFile)
        }
    }

    private fun compareManifests(
        referenceManifestFile: File,
        mergedManifestFile: File,
        htmlDiffFile: File,
    ) {
        val diff = ManifestDiffBuilder(
            filters = buildDiffFilters()
        ).build(referenceManifestFile, mergedManifestFile)

        if (diff.isEmpty()) {
            logger.lifecycle("AndroidManifests are the same")
        } else {
            logger.error("AndroidManifests are different")
            HtmlDiffWriter(htmlDiffFile).write(diff)
            throw GradleException(
                "AndroidManifest.xml has changed. Refer to '${htmlDiffFile.absolutePath}' for more details"
            )
        }
    }

    private fun buildDiffFilters(): List<ManifestDiffEntryFilter> {
        val filters = mutableListOf<ManifestDiffEntryFilter>()
        val ignoreConfig = ignoreConfig.get()
        if (ignoreConfig.ignoreAppVersionChanges.get()) {
            filters += AppVersionDiffFilter()
        }
        return filters
    }

}
