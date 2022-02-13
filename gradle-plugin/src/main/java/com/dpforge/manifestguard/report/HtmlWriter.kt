package com.dpforge.manifestguard.report

import org.apache.commons.text.StringEscapeUtils
import java.io.Writer
import java.net.URI

internal class HtmlWriter(
    private val writer: Writer,
) {

    fun pre(block: HtmlWriter.() -> Unit) {
        writeln("<pre>")
        block()
        writeln("</pre>")
    }

    fun text(value: () -> String) {
        write(StringEscapeUtils.escapeHtml4(value()))
    }

    fun h3(value: () -> String) {
        write("<h3>")
        write(StringEscapeUtils.escapeHtml4(value()))
        writeln("</h3>")
    }

    fun br() {
        writeln("<br/>")
    }

    fun del(background: String = "#ffe6e6", block: HtmlWriter.() -> Unit) {
        write("<del style=\"background:$background;\">")
        block()
        write("</del>")
    }

    fun ins(background: String = "#e6ffe6", block: HtmlWriter.() -> Unit) {
        write("<ins style=\"background:$background;\">")
        block()
        write("</ins>")
    }

    fun div(
        background: String? = null,
        display: String? = null,
        block: HtmlWriter.() -> Unit
    ) {
        val style = buildStyle(
            "background" to background,
            "display" to display
        )
        if (style.isEmpty()) {
            writeln("<div>")
        } else {
            writeln("<div style=\"$style\">")
        }
        block()
        writeln("</div>")
    }

    fun ul(block: HtmlWriter.() -> Unit) {
        writeln("<ul>")
        block()
        writeln("</ul>")
    }

    fun li(block: HtmlWriter.() -> Unit) {
        writeln("<li>")
        block()
        writeln("</li>")
    }

    fun a(href: URI, target: String = "_blank", block: HtmlWriter.() -> Unit) {
        write("<a href=\"${href.toASCIIString()}\" target=\"$target\">")
        block()
        writeln("</a>")
    }

    private fun buildStyle(vararg pairs: Pair<String, String?>): String = buildString {
        pairs.forEach { pair ->
            if (pair.second != null) {
                append(pair.first)
                append(':')
                append(pair.second)
                append(';')
            }
        }
    }

    private fun write(value: String) {
        writer.write(value)
    }

    private fun writeln(line: String) {
        writer.write(line)
        writer.write("\n")
    }

}