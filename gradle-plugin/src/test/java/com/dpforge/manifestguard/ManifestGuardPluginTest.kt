package com.dpforge.manifestguard

import com.android.Version.ANDROID_GRADLE_PLUGIN_VERSION
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ManifestGuardPluginTest {

    @TempDir
    lateinit var projectFolder: File

    private lateinit var settingsFile: File
    private lateinit var projectBuildFile: File

    private lateinit var testAppFolder: File
    private lateinit var testAppBuildFile: File
    private lateinit var testAppMainSrcFolder: File
    private lateinit var testAppAndroidManifestFile: File

    @BeforeEach
    fun setup() {
        settingsFile = File(projectFolder, "settings.gradle")
        projectBuildFile = File(projectFolder, "build.gradle")

        File(projectFolder, "gradle.properties").writeText(
            """
                android.useAndroidX=true
            """.trimIndent()
        )

        testAppFolder = File(projectFolder, "test-app")
        testAppFolder.mkdirs()

        testAppBuildFile = File(testAppFolder, "build.gradle")
        testAppMainSrcFolder = File(testAppFolder, "src/main")
        testAppMainSrcFolder.mkdirs()
        testAppAndroidManifestFile = File(testAppMainSrcFolder, "AndroidManifest.xml")

    }

    @Test
    fun test() {
        withSettings {
            """                
                pluginManagement {
                    resolutionStrategy {
                        eachPlugin {
                            if (requested.id.id.startsWith("com.android.")) {
                                useModule("com.android.tools.build:gradle:$ANDROID_GRADLE_PLUGIN_VERSION")
                            }
                        }
                    }
                    repositories {
                        google()
                    }
                }
                
                rootProject.name = "test-project"
                
                include ':test-app'
                
                dependencyResolutionManagement {
                    repositories {
                        mavenCentral()
                        google()
                    }
                }
            """.trimIndent()
        }

        withTestAppBuildFile {
            """
                plugins {
                    id 'com.android.application'
                    id 'com.dpforge.manifestguard'
                }
                
                android {
                    compileSdk 31
                
                    defaultConfig {
                        applicationId "com.dpforge.testapp"
                        minSdk 21
                        targetSdk 31
                        versionCode 1
                        versionName "1.0"
                    }
                }
                
                dependencies {
                    implementation 'androidx.appcompat:appcompat:1.4.1'
                    implementation 'com.google.android.material:material:1.5.0'
                }
            """.trimIndent()
        }

        withTestAndroidManifest {
            """
                <?xml version="1.0" encoding="utf-8"?>
                <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                    package="com.dpforge.testapp">
                
                    <application
                        android:label="test-app">
                    </application>
                </manifest>
            """.trimIndent()
        }

        val result = GradleRunner.create()
            .withProjectDir(projectFolder)
            .withPluginClasspath()
            .withArguments(":test-app:assembleDebug")
            .build()

        println(result.output)
        assertTrue(File(testAppFolder, "GuardedAndroidManifest.xml").exists())
    }

    private fun withSettings(content: () -> String): Unit = settingsFile.writeText(content())

    private fun withProjectBuildFile(content: () -> String): Unit =
        projectBuildFile.writeText(content())

    private fun withTestAppBuildFile(content: () -> String): Unit =
        testAppBuildFile.writeText(content())

    private fun withTestAndroidManifest(content: () -> String): Unit =
        testAppAndroidManifestFile.writeText(content())
}