package com.dpforge.manifestguard.manifest

import com.dpforge.manifestguard.manifest.ManifestDiff.Change
import com.dpforge.manifestguard.manifest.ManifestDiff.Entry


import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ManifestDiffBuilderTest {

    @TempDir
    lateinit var testDir: File

    private lateinit var manifestFile1: File
    private lateinit var manifestFile2: File

    @BeforeEach
    fun setup() {
        manifestFile1 = testDir.resolve("manifest1.xml")
        manifestFile2 = testDir.resolve("manifest2.html")
    }

    @Test
    fun `children - no changes - different children order`() {
        manifestFile1.writeText(
            """
                <root>
                    <aaa/>
                    <bbb/>
                    <ccc/>
                </root>
            """.trimIndent()
        )
        manifestFile2.writeText(
            """
                <root>
                    <aaa/>
                    <ccc/>
                    <bbb/>
                </root>
            """.trimIndent()
        )
        val diff = buildDiff()
        assertTrue(diff.isEmpty())
    }

    @Test
    fun `children - changes - different children order`() {
        manifestFile1.writeText(
            """
                <root>
                    <aaa/>
                    <bbb/>
                    <ccc/>
                </root>
            """.trimIndent()
        )
        manifestFile2.writeText(
            """
                <root>
                    <ddd/>
                    <ccc/>
                    <aaa/>
                </root>
            """.trimIndent()
        )
        val diff = buildDiff()
        assertEquals(2, diff.size)
        assertEquals("bbb", diff.removedItems().first().item.name)
        assertEquals("ddd", diff.addedItems().first().item.name)
    }

    @Test
    fun `children - no changes - different attribute order`() {
        manifestFile1.writeText(
            """
                <root>
                    <child aaa="1" bbb="2" ccc="3" />
                </root>
            """.trimIndent()
        )
        manifestFile2.writeText(
            """
                <root>
                    <child aaa="1" ccc="3" bbb="2" />
                </root>
            """.trimIndent()
        )
        val diff = buildDiff()
        assertTrue(diff.isEmpty())
    }

    @Test
    fun `children - changes - different attribute order`() {
        manifestFile1.writeText(
            """
                <root>
                    <child aaa="1" bbb="2" ccc="3" />
                </root>
            """.trimIndent()
        )
        manifestFile2.writeText(
            """
                <root>
                    <child ddd="5" aaa="1" ccc="4" />
                </root>
            """.trimIndent()
        )
        val diff = buildDiff()

        val entry = assertSingleItemChange(diff)

        assertEquals("bbb", entry.removedAttributeChanges().first().name)

        assertEquals("ccc", entry.changedAttributeChanges().first().name)
        assertEquals("3", entry.changedAttributeChanges().first().oldValue)
        assertEquals("4", entry.changedAttributeChanges().first().newValue)

        assertEquals("ddd", entry.addedAttributeChanges().first().name)
    }

    @Test
    fun `children - changes - children with the same name`() {
        manifestFile1.writeText(
            """
                <root>
                    <a name="a1">Old</a>
                    <a name="a2">Fixed</a>
                </root>
            """.trimIndent()
        )
        manifestFile2.writeText(
            """
                <root>
                    <a name="a1">New</a>
                    <a name="a2">Fixed</a>
                </root>
            """.trimIndent()
        )

        val diff = buildDiff()

        val entry = assertSingleItemChange(diff)
        assertEquals("Old", entry.changedValues().first().oldValue)
        assertEquals("New", entry.changedValues().first().newValue)
    }

    @Test
    fun `children - changes - children with the same name 2`() {
        manifestFile1.writeText(
            """
                <root>
                    <a name="b">Old</a>
                    <a name="b">Fixed</a>
                </root>
            """.trimIndent()
        )
        manifestFile2.writeText(
            """
                <root>
                    <a name="b">New</a>
                    <a name="b">Fixed</a>
                </root>
            """.trimIndent()
        )

        val exception = assertThrows(IllegalStateException::class.java) {
            buildDiff()
        }
        assertEquals(
            "Manifest has items that have the same name and the same set of attributes. It's unsupported case for now",
            exception.message
        )
    }

    @Test
    fun `children - changes - value and child items`() {
        manifestFile1.writeText(
            """
                <root>
                    <a>Text</a>
                </root>
            """.trimIndent()
        )
        manifestFile2.writeText(
            """
                <root>
                    <a>                    
                        <b/>
                    </a>
                </root>
            """.trimIndent()
        )

        val diff = buildDiff()

        assertEquals(2, diff.size)
        assertEquals("b", diff.addedItems().first().item.name)

        val entry = diff.changedItems().first()
        assertEquals("Text", entry.changedValues().first().oldValue)
        assertEquals("", entry.changedValues().first().newValue)
    }

    @Test
    fun `root - changes - different name`() {
        manifestFile1.writeText(
            """
                <root1 />
            """.trimIndent()
        )
        manifestFile2.writeText(
            """
                <root2 />
            """.trimIndent()
        )
        val diff = buildDiff()

        assertEquals(2, diff.size)
        assertEquals("root1", diff.removedItems().first().item.name)
        assertEquals("root2", diff.addedItems().first().item.name)
    }

    @Test
    fun `root - changes - root added`() {
        manifestFile1.writeText("")
        manifestFile2.writeText(
            """
                <root />
            """.trimIndent()
        )
        val diff = buildDiff()

        assertEquals(1, diff.size)
        assertEquals("root", diff.addedItems().first().item.name)
    }

    @Test
    fun `root - changes - root removed`() {
        manifestFile1.writeText(
            """
                <root />
            """.trimIndent()
        )
        manifestFile2.writeText("")
        val diff = buildDiff()

        assertEquals(1, diff.size)
        assertEquals("root", diff.removedItems().first().item.name)
    }

    @Test
    fun `root - no changes - empty files`() {
        manifestFile1.writeText("")
        manifestFile2.writeText("")
        val diff = buildDiff()

        assertTrue(diff.isEmpty())
    }

    @Test
    fun `value - changes - different values`() {
        manifestFile1.writeText(
            """
                <root>
                    <a>Old</a>
                </root>
            """.trimIndent()
        )
        manifestFile2.writeText(
            """
                <root>
                    <a>New</a>
                </root>
            """.trimIndent()
        )
        val diff = buildDiff()

        val entry = assertSingleItemChange(diff)

        assertEquals("Old", entry.changedValues().first().oldValue)
        assertEquals("New", entry.changedValues().first().newValue)
    }

    @Test
    fun `value - no changes - same values`() {
        manifestFile1.writeText(
            """
                <root>
                    <a>bbb</a>
                </root>
            """.trimIndent()
        )
        manifestFile2.writeText(
            """
                <root>
                    <a>bbb</a>
                </root>
            """.trimIndent()
        )
        val diff = buildDiff()

        assertTrue(diff.isEmpty())
    }

    private fun assertSingleItemChange(diff: ManifestDiff): Entry.ItemChanged {
        assertEquals(1, diff.size)
        assertTrue(diff.first() is Entry.ItemChanged)
        return diff.first() as Entry.ItemChanged
    }

    private fun Entry.ItemChanged.removedAttributeChanges() = changes.mapNotNull { it as? Change.AttributeRemoved }

    private fun Entry.ItemChanged.changedAttributeChanges() = changes.mapNotNull { it as? Change.AttributeChanged }

    private fun Entry.ItemChanged.addedAttributeChanges() = changes.mapNotNull { it as? Change.AttributeAdded }

    private fun ManifestDiff.removedItems() =
        mapNotNull { it as? Entry.ItemRemoved }

    private fun ManifestDiff.addedItems() =
        mapNotNull { it as? Entry.ItemAdded }

    private fun ManifestDiff.changedItems() =
        mapNotNull { it as? Entry.ItemChanged }

    private fun Entry.ItemChanged.changedValues() = changes.mapNotNull { it as? Change.ValueChanged }

    private fun buildDiff() = ManifestDiffBuilder().build(manifestFile1, manifestFile2)
}