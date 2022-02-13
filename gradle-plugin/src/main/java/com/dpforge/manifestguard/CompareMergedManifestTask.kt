package com.dpforge.manifestguard

import com.dpforge.manifestguard.manifest.ManifestDiffBuilder
import com.dpforge.manifestguard.report.HtmlDiffWriter
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

// todo @CacheableTask with test
abstract class CompareMergedManifestTask : DefaultTask() {

    @get:InputFile
    abstract val mergedManifestFileIn: RegularFileProperty

    @get:OutputFile
    abstract val mergedManifestFileOut: RegularFileProperty

    // Use @InputFile instead of @InputFiles (https://github.com/gradle/gradle/issues/2016)
    @get:InputFiles
    abstract val referenceManifestFileIn: RegularFileProperty

    @get:OutputFile
    abstract val htmlDiffFileIn: RegularFileProperty

    @TaskAction
    fun execute() {
        val htmlDiffFile = htmlDiffFileIn.get().asFile
        htmlDiffFile.delete()

        val referenceManifestFile = referenceManifestFileIn.get().asFile
        val mergedManifestFile = mergedManifestFileIn.get().asFile

        logger.debug("Merged AndroidManifest.xml: $mergedManifestFile")
        logger.debug("Reference AndroidManifest.xml: $referenceManifestFile")

        // Just pass the manifest further
        mergedManifestFileIn.get().asFile.copyTo(mergedManifestFileOut.get().asFile, overwrite = true)

        if (referenceManifestFile.exists()) {
            compareManifests(
                referenceManifestFile = referenceManifestFile,
                mergedManifestFile = mergedManifestFile,
                htmlDiffFile = htmlDiffFile
            )
        } else {
            logger.info("Reference AndroidManifest.xml does not exist. Just creating it.")
            mergedManifestFile.copyTo(referenceManifestFile)
        }
    }

    private fun compareManifests(
        referenceManifestFile: File,
        mergedManifestFile: File,
        htmlDiffFile: File
    ) {
        val diff = ManifestDiffBuilder().build(referenceManifestFile, mergedManifestFile)
        if (diff.isEmpty()) {
            logger.info("AndroidManifests are the same")
        } else {
            logger.error("AndroidManifests are different")
            HtmlDiffWriter(htmlDiffFile).write(diff)
            throw GradleException(
                "AndroidManifest.xml has changed. Refer to '${htmlDiffFile.absolutePath}' for more details"
            )
        }
    }

}
