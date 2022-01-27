package com.dpforge.manifestguard.extensions

import java.io.File

fun File.ensureParentsExist() {
    parentFile.ensureDirectoryExist()
}

fun File.ensureDirectoryExist() {
    if (!exists()) {
        if (!mkdirs()) {
            error("Failed to create dirs for '${this}'")
        }
    }
}