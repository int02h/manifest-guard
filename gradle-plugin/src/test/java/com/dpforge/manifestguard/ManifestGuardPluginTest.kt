package com.dpforge.manifestguard

import com.dpforge.manifestguard.extensions.ensureDirectoryExist
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
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
    private lateinit var manifestFile: File

    @BeforeEach
    fun setup() {
        projectDirectory.ensureDirectoryExist()
        generator = TestProjectGenerator(projectDirectory)
        defaultReferenceFile = generator.appDirectory.resolve("GuardedAndroidManifest.xml")
        defaultHtmlDiffFile = generator.appDirectory.resolve("build/outputs/manifest_guard/diff.html")
        manifestFile = generator.appManifestFile
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
                        guardVariant("debug") {
                            referenceFile = new File(projectDir, "manifest/original.xml")     
                        }
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
        assertFalse(defaultHtmlDiffFile.exists())
        assertTrue(result.output.contains("AndroidManifests are the same"))
    }

    @Test
    fun `compare - has changes, default path for html report`() {
        generator.generate()
        assembleTestApp() // create reference file
        modifyManifestLabel()

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
                        guardVariant("debug") {
                            htmlDiffFile = new File(projectDir, "manifest-diff.html")     
                        }
                    }
                """.trimIndent()
        }
        assembleTestApp() // create reference file
        modifyManifestLabel()

        val result = assembleTestApp(failureExpected = true)

        assertEquals(TaskOutcome.FAILED, result.compareDebugMergedManifestOutcome())
        assertTrue(result.output.contains("AndroidManifests are different"))
        assertTrue(generator.appDirectory.resolve("manifest-diff.html").exists())
    }

    @Test
    fun `compare - no changes, ignore version code and name`() {
        generator.generate {
            manifestGuardExtension =
                """
                    manifestGuard {
                        guardVariant("debug") {
                            ignore {
                                ignoreAppVersionChanges = true       
                            }
                        }
                    }
                """.trimIndent()
        }
        assembleTestApp() // create reference file
        modifyManifestAppVersion()

        val result = assembleTestApp()

        assertEquals(TaskOutcome.SUCCESS, result.compareDebugMergedManifestOutcome())
        assertTrue(result.output.contains("AndroidManifests are the same"))
        assertFalse(defaultHtmlDiffFile.exists())
    }

    @Test
    fun `compare - has changes, another change besides version`() {
        generator.generate {
            manifestGuardExtension =
                """
                    manifestGuard {
                        guardVariant("debug") {
                            ignore {
                                ignoreAppVersionChanges = true       
                            }
                        }
                    }
                """.trimIndent()
        }
        assembleTestApp() // create reference file
        modifyManifestLabel()
        modifyManifestAppVersion()

        val result = assembleTestApp(failureExpected = true)

        assertEquals(TaskOutcome.FAILED, result.compareDebugMergedManifestOutcome())
        assertTrue(result.output.contains("AndroidManifests are different"))
        assertTrue(defaultHtmlDiffFile.exists())
    }

    @Test
    fun `compare - has changes but compareOnAssemble is false`() {
        generator.generate {
            manifestGuardExtension =
                """
                    manifestGuard {
                        guardVariant("debug") {
                            compareOnAssemble = false
                        }
                    }
                """.trimIndent()
        }

        val result = assembleTestApp()

        assertNull(result.task(":test-app:compareDebugMergedManifest"))
        assertFalse(defaultReferenceFile.exists())
    }

    @Test
    fun `compare - no changes in tags without attributes`() {
        generator.generate {
            manifestApplicationContent = """
                <activity android:name=".MainActivity" android:exported="true">
                    <intent-filter>
                        <action android:name="android.intent.action.SEND"/>
                        <category android:name="android.intent.category.DEFAULT"/>
                        <data android:mimeType="text/plain"/>
                    </intent-filter>
                    
                    <intent-filter>
                        <action android:name="android.intent.action.VIEW"/>
                        <category android:name="android.intent.category.DEFAULT"/>
                        <data android:mimeType="image/*"/>
                    </intent-filter>
                </activity>
                """.trimIndent()
        }
        assembleTestApp() // create reference file

        modifyManifest { content ->
            content.replace("android.intent.action.SEND", "android.intent.action.SEND_MULTIPLE")
        }

        val result = assembleTestApp(failureExpected = true)

        assertEquals(TaskOutcome.FAILED, result.compareDebugMergedManifestOutcome())
        assertTrue(result.output.contains("AndroidManifests are different"))
        assertTrue(defaultHtmlDiffFile.exists())
    }

    @Test
    fun `update - non existing reference`() {
        generator.generate()

        val result = updateReferenceManifest()

        assertEquals(TaskOutcome.SUCCESS, result.updateDebugReferenceManifestOutcome())
        assertTrue(defaultReferenceFile.exists())
    }

    @Test
    fun `update - existing reference`() {
        generator.generate()

        assembleTestApp() // create reference file
        modifyManifestLabel()

        val result = updateReferenceManifest()

        assertEquals(TaskOutcome.SUCCESS, result.updateDebugReferenceManifestOutcome())
        assertTrue(defaultReferenceFile.exists())

        val referenceManifestContent = defaultReferenceFile.readText()
        assertFalse(referenceManifestContent.contains("android:label=\"test-app\""))
        assertTrue(referenceManifestContent.contains("android:label=\"changed\""))
    }

    @Test
    fun `update - compare is not called`() {
        generator.generate()

        val result = updateReferenceManifest()

        assertEquals(TaskOutcome.SUCCESS, result.updateDebugReferenceManifestOutcome())
        assertNull(result.task(":test-app:compareDebugMergedManifest"))
    }

    @Test
    fun `configuration - default config enabled=false`() {
        generator.generate {
            manifestGuardExtension = """
                |manifestGuard {
                |        defaultConfig {
                |           enabled = false
                |        }
                |    }
            """.trimMargin()
        }

        val result = tasks()

        assertFalse(result.output.contains("compareDebugMergedManifest"))
        assertFalse(result.output.contains("compareReleaseMergedManifest"))
        assertFalse(result.output.contains("updateDebugReferenceManifest"))
        assertFalse(result.output.contains("updateReleaseReferenceManifest"))
    }

    @Test
    fun `configuration - no build variants in the extension`() {
        generator.generate()

        val result = tasks()

        assertTrue(result.output.contains("compareDebugMergedManifest"))
        assertTrue(result.output.contains("compareReleaseMergedManifest"))
        assertTrue(result.output.contains("updateDebugReferenceManifest"))
        assertTrue(result.output.contains("updateReleaseReferenceManifest"))
    }

    @Test
    fun `configuration - only debug build variant enabled`() {
        generator.generate {
            manifestGuardExtension = """
                |manifestGuard {
                |        defaultConfig {
                |           enabled = false
                |        }
                |        guardVariant("debug") {
                |           enabled = true
                |        }
                |    }
            """.trimMargin()
        }

        val result = tasks()

        assertTrue(result.output.contains("compareDebugMergedManifest"))
        assertFalse(result.output.contains("compareReleaseMergedManifest"))
    }

    private fun modifyManifest(modificationBlock: (String) -> String) {
        with(manifestFile) { writeText(modificationBlock(readText())) }
    }

    private fun modifyManifestLabel() {
        modifyManifest { content ->
            content.replace("android:label=\"test-app\"", "android:label=\"changed\"")
        }
    }

    private fun modifyManifestAppVersion() {
        modifyManifest { content ->
            content.replace("android:versionCode=\"1\"", "android:versionCode=\"2\"")
                .replace("android:versionName=\"1.0\"", "android:versionName=\"2.0\"")
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

    private fun tasks(): BuildResult =
        GradleRunner.create()
            .withProjectDir(projectDirectory)
            .withPluginClasspath()
            .withArguments(":test-app:tasks")
            .build()

    private fun updateReferenceManifest(): BuildResult =
        GradleRunner.create()
            .withProjectDir(projectDirectory)
            .withPluginClasspath()
            .withArguments(":test-app:updateDebugReferenceManifest", "--info")
            .build()

    private fun BuildResult.compareDebugMergedManifestOutcome() =
        task(":test-app:compareDebugMergedManifest")!!.outcome

    private fun BuildResult.updateDebugReferenceManifestOutcome() =
        task(":test-app:updateDebugReferenceManifest")!!.outcome

}