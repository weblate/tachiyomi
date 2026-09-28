package tachiyomi.data.category

import tachiyomi.domain.category.model.Category

object CategoryMapper {
    fun mapCategory(
        id: Long,
        name: String,
        order: Long,
        flags: Long,
        // SY -->
        version: Long,
        uid: Long,
        lastModifiedAt: Long,
        @Suppress("UNUSED_PARAMETER")
        isSyncing: Long,
        // SY <--
    ): Category {
        return Category(
            id = id,
            name = name,
            order = order,
            flags = flags,
            // SY -->
            version = version,
            uid = uid,
            lastModifiedAt = lastModifiedAt,
            // SY <--
        )
    }
}
