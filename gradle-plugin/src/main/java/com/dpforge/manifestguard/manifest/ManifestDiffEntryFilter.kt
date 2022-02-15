package com.dpforge.manifestguard.manifest

internal interface ManifestDiffEntryFilter {
    fun shouldKeepEntry(entry: ManifestDiff.Entry): Boolean
}