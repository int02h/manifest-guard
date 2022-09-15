package com.dpforge.manifestguard

import com.android.Version
import com.dpforge.manifestguard.extensions.ensureDirectoryExist
import java.io.File

class TestProjectGenerator(
    val projectDirectory: File,
) {

    val appDirectory = File(projectDirectory, "test-app")
    val appMainSrcDirectory = File(appDirectory, "src/main")
    val appManifestFile = File(appMainSrcDirectory, "AndroidManifest.xml")

    fun generate(
        configBlock: AppConfig.() -> Unit = {}
    ) {
        writeSettings()
        writeGradleProperties()
        writeLocalProperties()

        val config = AppConfig()
        config.configBlock()
        writeTestApp(config)
    }

    private fun writeGradleProperties() {
        File(projectDirectory, "gradle.properties").writeText(
            """
                android.useAndroidX=true
            """.trimIndent()
        )
    }

    private fun writeSettings() {
        File(projectDirectory, "settings.gradle").writeText(
            """                
                pluginManagement {
                    resolutionStrategy {
                        eachPlugin {
                            if (requested.id.id.startsWith("com.android.")) {
                                useModule("com.android.tools.build:gradle:${Version.ANDROID_GRADLE_PLUGIN_VERSION}")
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
        )
    }

    private fun writeLocalProperties() {
        val localProperties = File(projectDirectory, "local.properties")
        val androidHome = System.getenv("ANDROID_HOME")
        localProperties.writeText("sdk.dir=$androidHome")
    }

    private fun writeTestApp(config: AppConfig) {
        appDirectory.ensureDirectoryExist()
        writeAppBuildScript(config)

        appMainSrcDirectory.ensureDirectoryExist()
        writeAppManifest(config)
    }

    private fun writeAppBuildScript(config: AppConfig) {
        File(appDirectory, "build.gradle").writeText(
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
                
                ${config.manifestGuardExtension}
            """.trimIndent()
        )
    }

    private fun writeAppManifest(config: AppConfig) {
        appManifestFile.writeText(
            """
                <?xml version="1.0" encoding="utf-8"?>
                <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                    package="com.dpforge.testapp">
                
                    <application
                        android:label="test-app">
                        ${config.manifestApplicationContent}
                    </application>
                </manifest>
            """.trimIndent().trimStart()
        )
    }

    class AppConfig {
        var manifestGuardExtension: String = ""
        var manifestApplicationContent: String = ""
    }
}