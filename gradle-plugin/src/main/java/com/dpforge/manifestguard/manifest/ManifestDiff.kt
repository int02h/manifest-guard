package com.dpforge.manifestguard.manifest

import java.io.File

class ManifestDiff(
    val oldFile: File,
    val newFile: File,
    private val items: List<Entry>
) : List<ManifestDiff.Entry> by items {

    sealed class Entry {
        class ItemAdded(val item: ManifestItem) : Entry()
        class ItemChanged(val oldItem: ManifestItem, val newItem: ManifestItem, val changes: List<Change>) : Entry()
        class ItemRemoved(val item: ManifestItem) : Entry()
    }

    sealed class Change {

        class AttributeRemoved(
            val name: String,
            val value: String
        ) : Change()

        class AttributeChanged(
            val name: String,
            val oldValue: String,
            val newValue: String
        ) : Change()

        class AttributeAdded(
            val name: String,
            val value: String
        ) : Change()

        class ValueChanged(
            val oldValue: String,
            val newValue: String
        ) : Change()

    }

}