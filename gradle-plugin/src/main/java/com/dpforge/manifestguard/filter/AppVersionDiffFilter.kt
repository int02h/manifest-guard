package com.dpforge.manifestguard.filter

import com.dpforge.manifestguard.manifest.ManifestDiff
import com.dpforge.manifestguard.manifest.ManifestDiffEntryFilter

internal class AppVersionDiffFilter : ManifestDiffEntryFilter {

    override fun shouldKeepEntry(entry: ManifestDiff.Entry): Boolean {
        if (entry !is ManifestDiff.Entry.ItemChanged){
            return true
        }
        if (entry.oldItem.path != "manifest") {
            return true
        }
        val attributeChanges = entry.changes.mapNotNull { it as? ManifestDiff.Change.AttributeChanged }
        if (attributeChanges.size != entry.changes.size) { // there are other changes
            return true
        }
        val attributeNames = attributeChanges.map { it.name }.toMutableSet()
        attributeNames.remove("android:versionCode")
        attributeNames.remove("android:versionName")
        return attributeNames.isNotEmpty()
    }

}
