package com.dpforge.manifestguard.manifest

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ManifestParserTest {

    @TempDir
    lateinit var testDir: File

    private lateinit var testFile: File

    @BeforeEach
    fun setup() {
        testFile = testDir.resolve("test.xml")
    }

    @Test
    fun `manifest item - path`() {
        testFile.writeText(
            """
                <root>
                    <a>
                        <b>
                            <c/>
                        </b>
                    </a>
                </root>
            """.trimIndent()
        )

        val root = parse()
        assertNull(root.path)
        assertEquals("root", root.requireChildByName("a").path)
        assertEquals("root/a", root.requireChildByName("b").path)
        assertEquals("root/a/b", root.requireChildByName("c").path)
    }

    @Test
    fun `manifest item - parent`() {
        testFile.writeText(
            """
                <root>
                    <a>
                        <b>
                            <c/>
                        </b>
                    </a>
                </root>
            """.trimIndent()
        )

        val root = parse()
        assertNull(root.parent)
        assertEquals("root", root.requireChildByName("a").parent?.name)
        assertEquals("a", root.requireChildByName("b").parent?.name)
        assertEquals("b", root.requireChildByName("c").parent?.name)
    }

    @Test
    fun `manifest item - children`() {
        testFile.writeText(
            """
                <root>
                    <a/>
                    <b/>
                    <c/>
                </root>
            """.trimIndent()
        )

        val root = parse()
        assertEquals(3, root.children.size)
        assertEquals(listOf("a", "b", "c"), root.children.map { it.name })
    }

    private fun parse(): ManifestItem {
        val item = ManifestParser().parse(testFile)
        assertNotNull(item)
        return item!!
    }

    private fun ManifestItem.requireChildByName(name: String): ManifestItem {
        val child = findChildByName(name)
        assertNotNull(child)
        return child!!
    }


    private fun ManifestItem.findChildByName(name: String): ManifestItem? {
        for (item in children) {
            if (item.name == name) {
                return item
            }
            return item.findChildByName(name) ?: continue
        }
        return null
    }
}