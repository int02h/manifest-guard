package com.dpforge.manifestguard

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class UpdateReferenceManifestTask : DefaultTask() {

    @get:InputFile
    abstract val mergedManifestFile: RegularFileProperty

    @get:OutputFile
    abstract val referenceManifestFile: RegularFileProperty

    @TaskAction
    fun execute() {
        mergedManifestFile.get().asFile.copyTo(referenceManifestFile.get().asFile, overwrite = true)
        logger.info("Reference AndroidManifest.xml has been updated")
    }
}