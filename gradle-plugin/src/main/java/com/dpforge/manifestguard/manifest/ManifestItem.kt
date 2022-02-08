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

    // TODO handle two or more children with the same name
    private val childrenInternal = mutableMapOf<String, ManifestItem>()

    val children: Map<String, ManifestItem>
        get() = childrenInternal

    init {
        parent?.childrenInternal?.put(name, this)
    }

    override fun toString(): String = "<$name>"
}