package com.dpforge.manifestguard.manifest

import org.xml.sax.SAXParseException
import java.io.File
import javax.xml.parsers.SAXParserFactory

internal class ManifestParser {

    fun parse(file: File): ManifestItem? {
        val parser = SAXParserFactory.newInstance().newSAXParser()
        val xmlHandler = ManifestHandler()
        try {
            parser.parse(file, xmlHandler)
        } catch (e: SAXParseException) {
            // TODO add logging
        }
        return xmlHandler.rootItem
    }

}