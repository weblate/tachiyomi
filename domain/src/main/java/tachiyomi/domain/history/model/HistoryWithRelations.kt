package tachiyomi.domain.history.model

import tachiyomi.domain.manga.model.CustomMangaInfo
import tachiyomi.domain.manga.model.MangaCover
import java.util.Date

data class HistoryWithRelations(
    val id: Long,
    val chapterId: Long,
    val mangaId: Long,
    // SY -->
    val ogTitle: String,
    val customMangaInfo: CustomMangaInfo? = null,
    // SY <--
    val chapterNumber: Double,
    val readAt: Date?,
    val readDuration: Long,
    val coverData: MangaCover,
) {
    // SY -->
    val title: String = customMangaInfo?.title ?: ogTitle
    // SY <--
}
