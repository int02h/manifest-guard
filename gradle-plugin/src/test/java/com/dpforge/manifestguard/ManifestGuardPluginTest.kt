package com.dpforge.manifestguard

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ManifestGuardPluginTest {

    @TempDir
    lateinit var projectDirectory: File

    private lateinit var generator: TestProjectGenerator

    private lateinit var defaultReferenceFile: File
    private lateinit var defaultHtmlDiffFile: File

    @BeforeEach
    fun setup() {
        generator = TestProjectGenerator(projectDirectory)
        defaultReferenceFile = generator.appDirectory.resolve("GuardedAndroidManifest.xml")
        defaultHtmlDiffFile = generator.appDirectory.resolve("build/outputs/manifest_guard/diff.html")
    }

    @Test
    fun `reference file - default path`() {
        generator.generate()

        assembleTestApp()

        assertTrue(defaultReferenceFile.exists())
    }

    @Test
    fun `reference file - custom path`() {
        generator.generate {
            manifestGuardExtension =
                """
                    manifestGuard {
                       referenceFile = new File(projectDir, "manifest/original.xml")
                    }
                """.trimIndent()
        }

        assembleTestApp()

        assertFalse(defaultReferenceFile.exists())
        assertTrue(generator.appDirectory.resolve("manifest/original.xml").exists())
    }

    @Test
    fun `compare - no changes`() {
        generator.generate()
        assembleTestApp() // create reference file

        val result = assembleTestApp()

        assertEquals(TaskOutcome.SUCCESS, result.compareDebugMergedManifestOutcome())
        assertTrue(result.output.contains("AndroidManifests are the same"))
        assertFalse(defaultHtmlDiffFile.exists())
    }

    @Test
    fun `compare - has changes, default path for html report`() {
        generator.generate()
        assembleTestApp() // create reference file
        modifyDefaultReferenceFile()

        val result = assembleTestApp(failureExpected = true)

        assertEquals(TaskOutcome.FAILED, result.compareDebugMergedManifestOutcome())
        assertTrue(result.output.contains("AndroidManifests are different"))
        assertTrue(defaultHtmlDiffFile.exists())
    }

    @Test
    fun `compare - has changes, custom path for html report`() {
        generator.generate {
            manifestGuardExtension =
                """
                    manifestGuard {
                       htmlDiffFile = new File(projectDir, "manifest-diff.html")
                    }
                """.trimIndent()
        }
        assembleTestApp() // create reference file
        modifyDefaultReferenceFile()

        val result = assembleTestApp(failureExpected = true)

        assertEquals(TaskOutcome.FAILED, result.compareDebugMergedManifestOutcome())
        assertTrue(result.output.contains("AndroidManifests are different"))
        assertTrue(generator.appDirectory.resolve("manifest-diff.html").exists())
    }

    private fun modifyDefaultReferenceFile() {
        with(defaultReferenceFile) {
            writeText(
                readText().replace("android:label=\"test-app\"", "android:label=\"changed\"")
            )
        }
    }

    private fun assembleTestApp(failureExpected: Boolean = false): BuildResult =
        GradleRunner.create()
            .withProjectDir(projectDirectory)
            .withPluginClasspath()
            .withArguments(":test-app:assembleDebug", "--info")
            .run {
                if (failureExpected) {
                    buildAndFail()
                } else {
                    build()
                }
            }

    private fun BuildResult.compareDebugMergedManifestOutcome() =
        task(":test-app:compareDebugMergedManifest")!!.outcome

}