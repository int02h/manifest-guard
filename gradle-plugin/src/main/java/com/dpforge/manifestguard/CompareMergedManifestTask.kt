package com.dpforge.manifestguard

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.LinkedList
import name.fraser.neil.plaintext.diff_match_patch as DiffMatchPatch

// todo @CacheableTask with test
abstract class CompareMergedManifestTask : DefaultTask() {

    @get:InputFile
    abstract val mergedManifestFileIn: RegularFileProperty

    @get:InputFile
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
        val dmp = DiffMatchPatch()
        val diffList = dmp.diff_main(
            referenceManifestFile.readText(),
            mergedManifestFile.readText(),
        )
        if (diffList.hasOnlyEqual) {
            logger.info("AndroidManifests are the same")
        } else {
            logger.error("AndroidManifests are different")
            writeHtmlReport(dmp, diffList, htmlDiffFile)
            throw GradleException(
                "AndroidManifest.xml has changed. " +
                        "Refer to '${htmlDiffFile.absolutePath}' for more details"
            )
        }
    }

    private fun writeHtmlReport(
        dmp: DiffMatchPatch,
        diffList: LinkedList<DiffMatchPatch.Diff>,
        htmlDiffFile: File
    ) {
        dmp.diff_cleanupSemantic(diffList)
        htmlDiffFile.ensureParentsExist()
        val html = buildString {
            append("<pre>")
            append(dmp.diff_prettyHtml(diffList))
            append("</pre>")
        }
        htmlDiffFile.writeText(html)
    }

    private val List<DiffMatchPatch.Diff>.hasOnlyEqual: Boolean
        get() = all { it.operation == DiffMatchPatch.Operation.EQUAL }

    private fun File.ensureParentsExist() {
        if (!parentFile.exists()) {
            if (!parentFile.mkdirs()) {
                error("Failed to create dirs for file '${this}'")
            }
        }
    }
}
