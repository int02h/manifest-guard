package com.dpforge.manifestguard.filter

import com.dpforge.manifestguard.manifest.ManifestDiff.Change
import com.dpforge.manifestguard.manifest.ManifestDiff.Entry
import com.dpforge.manifestguard.manifest.ManifestItem
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

import org.junit.jupiter.api.Test

internal class AppVersionDiffFilterTest {

    private val filter = AppVersionDiffFilter()

    @Test
    fun `shouldKeepEntry - item added`() {
        assertTrue(filter.shouldKeepEntry(Entry.ItemAdded(item = mockItem())))
    }

    @Test
    fun `shouldKeepEntry - not manifest`() {
        assertTrue(
            filter.shouldKeepEntry(
                Entry.ItemChanged(
                    oldItem = mockItem(name = "child", parent = mockItem("manifest")),
                    newItem = mockItem(name = "child", parent = mockItem("manifest")),
                    changes = listOf(
                        Change.AttributeChanged("android:versionCode", "1", "2")
                    )
                )
            )
        )
    }

    @Test
    fun `shouldKeepEntry - another changes`() {
        assertTrue(
            filter.shouldKeepEntry(
                Entry.ItemChanged(
                    oldItem = mockItem(name = "manifest"),
                    newItem = mockItem(name = "manifest"),
                    changes = listOf(
                        Change.AttributeChanged("android:versionCode", "1", "2"),
                        Change.AttributeChanged("android:versionName", "1.0", "2.0"),
                        Change.ValueChanged("old", "new"),
                    )
                )
            )
        )
    }

    @Test
    fun `shouldKeepEntry - another attribute changes`() {
        assertTrue(
            filter.shouldKeepEntry(
                Entry.ItemChanged(
                    oldItem = mockItem(name = "manifest"),
                    newItem = mockItem(name = "manifest"),
                    changes = listOf(
                        Change.AttributeChanged("android:versionCode", "1", "2"),
                        Change.AttributeChanged("android:versionName", "1.0", "2.0"),
                        Change.AttributeChanged("android:test", "false", "true"),
                    )
                )
            )
        )
    }

    @Test
    fun `shouldKeepEntry - only version code`() {
        assertFalse(
            filter.shouldKeepEntry(
                Entry.ItemChanged(
                    oldItem = mockItem(name = "manifest"),
                    newItem = mockItem(name = "manifest"),
                    changes = listOf(
                        Change.AttributeChanged("android:versionCode", "1", "2"),
                    )
                )
            )
        )
    }

    @Test
    fun `shouldKeepEntry - only version name`() {
        assertFalse(
            filter.shouldKeepEntry(
                Entry.ItemChanged(
                    oldItem = mockItem(name = "manifest"),
                    newItem = mockItem(name = "manifest"),
                    changes = listOf(
                        Change.AttributeChanged("android:versionName", "1.0", "2.0"),
                    )
                )
            )
        )
    }

    @Test
    fun `shouldKeepEntry - version code and name`() {
        assertFalse(
            filter.shouldKeepEntry(
                Entry.ItemChanged(
                    oldItem = mockItem(name = "manifest"),
                    newItem = mockItem(name = "manifest"),
                    changes = listOf(
                        Change.AttributeChanged("android:versionCode", "1", "2"),
                        Change.AttributeChanged("android:versionName", "1.0", "2.0"),
                    )
                )
            )
        )
    }

    private fun mockItem(
        name: String = "mock",
        parent: ManifestItem? = null,
        attributes: Map<String, String> = emptyMap()
    ) = ManifestItem(
        name = name,
        parent = parent,
        attributes = attributes
    )
}