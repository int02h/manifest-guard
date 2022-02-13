package com.dpforge.manifestguard.report

import com.dpforge.manifestguard.manifest.ManifestDiff
import com.dpforge.manifestguard.manifest.ManifestItem
import java.io.File
import java.io.FileOutputStream

internal class HtmlDiffWriter(
    private val output: File,
) {

    fun write(diff: ManifestDiff) {
        FileOutputStream(output).bufferedWriter().use { writer ->
            val htmlWriter = HtmlWriter(writer)

            htmlWriter.div(display = "inline-block") {

                a(href = diff.oldFile.toURI()) {
                    text { "Reference AndroidManifest.xml" }
                }
                br()
                a(href = diff.newFile.toURI()) {
                    text { "Merged AndroidManifest.xml" }
                }

                val removed = diff.filterIsInstance<ManifestDiff.Entry.ItemRemoved>()
                htmlWriter.writeRemoved(removed)

                val added = diff.filterIsInstance<ManifestDiff.Entry.ItemAdded>()
                htmlWriter.writeAdded(added)

                val changed = diff.filterIsInstance<ManifestDiff.Entry.ItemChanged>()
                htmlWriter.writeChanged(changed)
            }
        }
    }

    private fun HtmlWriter.writeRemoved(removed: List<ManifestDiff.Entry.ItemRemoved>) {
        if (removed.isEmpty()) {
            return
        }
        h3 { "Removed AndroidManifest.xml items:" }
        removed.forEach {
            ul {
                li {
                    text { it.item.path }
                    br()
                    div(background = "#ffe6e6") {
                        writeItem(it.item)
                    }
                }
            }
        }
    }

    private fun HtmlWriter.writeAdded(added: List<ManifestDiff.Entry.ItemAdded>) {
        if (added.isEmpty()) {
            return
        }
        h3 { "Added AndroidManifest.xml items:" }
        added.forEach {
            ul {
                li {
                    text { it.item.path }
                    br()
                    div(background = "#e6ffe6") {
                        writeItem(it.item)
                    }
                }
            }
        }
    }

    private fun HtmlWriter.writeChanged(changed: List<ManifestDiff.Entry.ItemChanged>) {
        if (changed.isEmpty()) {
            return
        }
        h3 { "Changed AndroidManifest.xml items:" }
        changed.forEach {
            ul {
                li {
                    text { it.oldItem.path }
                    br()
                    writeChangedItem(it)
                }
            }
        }
    }

    private fun HtmlWriter.writeChangedItem(changedItem: ManifestDiff.Entry.ItemChanged) {
        writeItem(changedItem.oldItem, changedItem.changes)
    }

    private fun HtmlWriter.writeItem(item: ManifestItem) {
        pre {
            text { buildItemString(item) }
        }
    }

    private fun buildItemString(item: ManifestItem) = buildString {
        append("<${item.name}")
        item.attributes.forEach { (name, value) ->
            append("\n    $name=\"$value\"")
        }
        if (item.value.isBlank()) {
            append(" />")
        } else {
            append(" >")
        }
    }

    private fun HtmlWriter.writeItem(item: ManifestItem, changes: List<ManifestDiff.Change>) {
        val removedAttributes = changes.filterIsInstance<ManifestDiff.Change.AttributeRemoved>()
            .associateBy { it.name }
        val addedAttributes = changes.filterIsInstance<ManifestDiff.Change.AttributeAdded>()
            .associateBy { it.name }
        val changedAttributes = changes.filterIsInstance<ManifestDiff.Change.AttributeChanged>()
            .associateBy { it.name }
        val changedValue = changes.filterIsInstance<ManifestDiff.Change.ValueChanged>().firstOrNull()

        val attributes = (item.attributes + addedAttributes.values.associate { it.name to it.value }).toSortedMap()

        pre {
            text { "<${item.name}" }

            attributes.forEach { (name, value) ->
                text { "\n    " }
                when {
                    removedAttributes.containsKey(name) -> del { text { "$name=\"$value\"" } }
                    addedAttributes.containsKey(name) -> ins { text { "$name=\"$value\"" } }
                    changedAttributes.containsKey(name) -> {
                        val changed = changedAttributes.getValue(name)
                        text { "$name=\"" }
                        del { text { changed.oldValue } }
                        ins { text { changed.newValue } }
                        text { "\"" }
                    }
                    else -> text { "$name=\"$value\"" }
                }

            }

            if (changedValue != null) {
                text { ">" }
                del { text { changedValue.oldValue } }
                ins { text { changedValue.newValue } }
                text { "</${item.name}>" }
            } else {
                if (item.value.isBlank()) {
                    text { "/>" }
                } else {
                    text { ">" }
                }
            }
        }
    }

}