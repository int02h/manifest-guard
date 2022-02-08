package com.dpforge.manifestguard.manifest

import com.dpforge.manifestguard.manifest.ManifestDiff.Change
import com.dpforge.manifestguard.manifest.ManifestDiff.Entry
import org.xml.sax.SAXParseException
import java.io.File
import javax.xml.parsers.SAXParserFactory

class ManifestDiffBuilder {

    private val diffEntries = mutableListOf<Entry>()

    fun build(file1: File, file2: File): ManifestDiff {
        val parser = ManifestParser()
        val root1 = parser.parse(file1)
        val root2 = parser.parse(file2)

        when {
            root1 == null && root2 != null -> diffEntries += Entry.ItemAdded(root2)
            root1 != null && root2 == null -> diffEntries += Entry.ItemRemoved(root1)
            root1 != null && root2 != null -> {
                if (root1.name != root2.name) {
                    diffEntries += Entry.ItemRemoved(root1)
                    diffEntries += Entry.ItemAdded(root2)
                } else {
                    compareItemsWithSameName(root1, root2)
                }
            }
        }

        return ManifestDiff(diffEntries)
    }

    private fun compareItemsWithSameName(item1: ManifestItem, item2: ManifestItem) {
        if (item1.name != item2.name) {
            error("Names are different: ${item1.name} and ${item2.name}")
        }

        val entryChanges = mutableListOf<Change>()

        compareAttributes(entryChanges, item1.attributes, item2.attributes)
        compareChildren(item1.children, item2.children)
        compareValues(entryChanges, item1.value, item2.value)

        if (entryChanges.isNotEmpty()) {
            diffEntries += Entry.ItemChanged(item1, item2, entryChanges)
        }
    }

    private fun compareAttributes(
        entryChanges: MutableList<Change>,
        attrs1: Map<String, String>,
        attrs2: Map<String, String>
    ) {
        val added = HashSet(attrs2.keys)

        attrs1.forEach { (name1, value1) ->
            val value2 = attrs2[name1]
            added.remove(name1)
            if (value2 == null) {
                entryChanges += Change.AttributeRemoved(name1, value1)
            } else {
                if (value1 != value2) {
                    entryChanges += Change.AttributeChanged(name = name1, oldValue = value1, newValue = value2)
                }
            }
        }

        added.forEach { name ->
            entryChanges += Change.AttributeAdded(name = name, value = attrs2.getValue(name))
        }
    }

    private fun compareChildren(children1: Map<String, ManifestItem>, children2: Map<String, ManifestItem>) {
        val added = HashSet(children2.keys)

        children1.forEach { (name1, child1) ->
            val child2 = children2[name1]
            added.remove(name1)
            if (child2 == null) {
                diffEntries += Entry.ItemRemoved(child1)
            } else {
                compareItemsWithSameName(child1, child2)
            }
        }

        added.forEach { name -> diffEntries += Entry.ItemAdded(children2.getValue(name)) }
    }

    private fun compareValues(entryChanges: MutableList<Change>, value1: String, value2: String) {
        if (value1 != value2) {
            entryChanges += Change.ValueChanged(oldValue = value1, newValue = value2)
        }
    }

}