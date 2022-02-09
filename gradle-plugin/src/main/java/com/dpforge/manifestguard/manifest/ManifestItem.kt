package com.dpforge.manifestguard.manifest

class ManifestItem(
    val name: String,
    val parent: ManifestItem?,
    val attributes: Map<String, String>
) {
    val path: String? by lazy {
        val parentPath = parent?.path
        val parentName = parent?.name
        if (parentPath != null) {
            "$parentPath/$parentName"
        } else {
            parentName
        }
    }

    var value: String = ""
        internal set

    private val childrenInternal = mutableListOf<ManifestItem>()

    val children: List<ManifestItem>
        get() = childrenInternal

    init {
        parent?.childrenInternal?.add(this)
    }

    override fun toString(): String = "<$name>"
}