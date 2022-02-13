package com.dpforge.manifestguard.manifest

import com.dpforge.manifestguard.manifest.ManifestDiff.Change
import com.dpforge.manifestguard.manifest.ManifestDiff.Entry
import java.io.File

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

        return ManifestDiff(
            oldFile = file1,
            newFile = file2,
            items = diffEntries
        )
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

    private fun compareChildren(children1: List<ManifestItem>, children2: List<ManifestItem>) {
        val map1 = buildChildMap(children1)
        val map2 = buildChildMap(children2)
        val added = HashSet(map2.keys)

        map1.forEach { (name1, child1) ->
            val child2 = map2[name1]
            added.remove(name1)
            if (child2 == null) {
                child1.forEach { diffEntries += Entry.ItemRemoved(it) }
            } else {
                compareChildrenUnique(child1, child2)
            }
        }

        added.forEach { name ->
            map2.getValue(name).forEach { diffEntries += Entry.ItemAdded(it) }
        }
    }

    private fun compareChildrenUnique(children1: List<ManifestItem>, children2: List<ManifestItem>) {
        if (children1.size == 1 && children2.size == 1) {
            compareItemsWithSameName(children1.first(), children2.first())
            return
        }
        val map1 = buildUniqueChildMap(children1)
        val map2 = buildUniqueChildMap(children2)
        val added = HashSet(map2.keys)

        map1.forEach { (name1, child1) ->
            val child2 = map2[name1]
            added.remove(name1)
            if (child2 == null) {
                diffEntries += Entry.ItemRemoved(child1)
            } else {
                compareItemsWithSameName(child1, child2)
            }
        }

        added.forEach { name -> diffEntries += Entry.ItemAdded(map2.getValue(name)) }
    }

    private fun buildChildMap(children: List<ManifestItem>): Map<String, List<ManifestItem>> {
        val map = mutableMapOf<String, MutableList<ManifestItem>>()
        children.forEach { child ->
            val list = map.getOrPut(child.name) { mutableListOf() }
            list += child
        }
        return map
    }

    private fun buildUniqueChildMap(children: List<ManifestItem>): Map<String, ManifestItem> {
        val map = children.associateBy(::createChildUniqueKey)
        if (map.size != children.size) {
            error(
                "Manifest has items that have the same name and the same set of attributes. " +
                        "It's unsupported case for now"
            )
        }
        return map
    }

    private fun createChildUniqueKey(item: ManifestItem): String = buildString {
        append(item.name)
        append(";")
        item.attributes.toSortedMap().forEach { (key, value) ->
            append(key)
            append(":")
            append(value)
        }
    }

    private fun compareValues(entryChanges: MutableList<Change>, value1: String, value2: String) {
        if (value1 != value2) {
            entryChanges += Change.ValueChanged(oldValue = value1, newValue = value2)
        }
    }

}