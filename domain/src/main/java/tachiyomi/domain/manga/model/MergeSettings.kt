package tachiyomi.domain.manga.model

/**
 * SY-specific file
 */
data class MergeSettings(
    val mangaId: Long,
    val chapterSortMode: Int,
    val infoMangaId: Long?,
) {
    companion object {
        const val CHAPTER_SORT_NONE = 0
        const val CHAPTER_SORT_NO_DEDUPE = 1
        const val CHAPTER_SORT_PRIORITY = 2
        const val CHAPTER_SORT_MOST_CHAPTERS = 3
        const val CHAPTER_SORT_HIGHEST_CHAPTER_NUMBER = 4
    }
}
