package com.dpforge.manifestguard.manifest

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

internal class ManifestHandler : DefaultHandler() {

    private var currentItem: ManifestItem? = null

    var rootItem: ManifestItem? = null
        private set

    private val valueBuilder = StringBuilder()

    override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
        currentItem = ManifestItem(qName, currentItem, attributes.toMap())
        if (rootItem == null) {
            rootItem = currentItem
        }
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        currentItem = currentItem?.parent
    }

    override fun characters(ch: CharArray, start: Int, length: Int) {
        valueBuilder.setLength(0)
        valueBuilder.append(ch, start, length)
        currentItem?.value = valueBuilder.trim().toString()
    }

    private fun Attributes.toMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for (i in 0 until length) {
            map[getQName(i)] = getValue(i)
        }
        return map
    }
}