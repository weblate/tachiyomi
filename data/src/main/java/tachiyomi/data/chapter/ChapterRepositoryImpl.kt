package tachiyomi.data.chapter

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonObject
import logcat.LogPriority
import tachiyomi.core.common.util.lang.toLong
import tachiyomi.core.common.util.system.logcat
import tachiyomi.data.Database
import tachiyomi.data.chapter.ChapterMapper.mapChapter
import tachiyomi.data.subscribeToList
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.chapter.model.ChapterRemoteUpdate
import tachiyomi.domain.chapter.model.ChapterSummary
import tachiyomi.domain.chapter.model.ChapterUpdate
import tachiyomi.domain.chapter.repository.ChapterRepository

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class ChapterRepositoryImpl(
    private val database: Database,
) : ChapterRepository {

    // SY -->
    override suspend fun addAll(chapters: List<Chapter>): List<Chapter> {
        return try {
            database.transactionWithResult {
                chapters.map { chapter ->
                    val chapterId = database.chapterQueries.insertReturningId(
                        chapter.mangaId,
                        chapter.url,
                        chapter.name,
                        chapter.scanlator,
                        chapter.read,
                        chapter.bookmark,
                        chapter.lastPageRead,
                        chapter.chapterNumber,
                        chapter.sourceOrder,
                        chapter.dateFetch,
                        chapter.dateUpload,
                        chapter.memo,
                        /* SY --> */ chapter.lastModifiedAt, chapter.version,
                        /* SY <-- */
                    )
                        .awaitAsOne()
                    chapter.copy(id = chapterId)
                }
            }
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
            emptyList()
        }
    }

    override suspend fun removeChaptersWithIds(ids: List<Long>) {
        database.chapterQueries.removeChaptersWithIds(ids)
    }
    // SY <--

    override suspend fun update(chapterUpdate: ChapterUpdate) {
        partialUpdate(chapterUpdate)
    }

    override suspend fun updateAll(chapterUpdates: List<ChapterUpdate>) {
        partialUpdate(*chapterUpdates.toTypedArray())
    }

    override suspend fun updateFromRemote(
        removedIds: List<Long>,
        added: List<Chapter>,
        updated: List<ChapterRemoteUpdate>,
    ): List<Chapter> {
        return database.transactionWithResult {
            if (removedIds.isNotEmpty()) {
                database.chapterQueries.removeChaptersWithIds(removedIds)
            }
            val existing = added.map { it.mangaId }
                .distinct()
                .flatMap { mangaId ->
                    database.chapterQueries
                        .getChaptersByMangaId(
                            mangaId = mangaId,
                            applyScanlatorFilter = false.toLong(),
                            mapper = ::mapChapter,
                        )
                        .awaitAsList()
                        .map { mangaId to it.url }
                }
                .toMutableSet()
            val stored = added.filter { existing.add(it.mangaId to it.url) }.map { chapter ->
                val chapterId = database.chapterQueries.insertReturningId(
                    mangaId = chapter.mangaId,
                    url = chapter.url,
                    name = chapter.name,
                    scanlator = chapter.scanlator,
                    read = chapter.read,
                    bookmark = chapter.bookmark,
                    lastPageRead = chapter.lastPageRead,
                    chapterNumber = chapter.chapterNumber,
                    sourceOrder = chapter.sourceOrder,
                    dateFetch = chapter.dateFetch,
                    dateUpload = chapter.dateUpload,
                    memo = chapter.memo,
                    /* SY --> */ lastModifiedAt = chapter.lastModifiedAt, version = chapter.version,
                    /* SY <-- */
                )
                    .awaitAsOne()
                chapter.copy(id = chapterId)
            }
            updated.forEach { chapterUpdate ->
                database.chapterQueries.updateRemote(
                    name = chapterUpdate.name,
                    scanlator = chapterUpdate.scanlator,
                    chapterNumber = chapterUpdate.chapterNumber,
                    sourceOrder = chapterUpdate.sourceOrder,
                    dateUpload = chapterUpdate.dateUpload,
                    chapterId = chapterUpdate.id,
                    memo = chapterUpdate.memo,
                )
            }
            stored
        }
    }

    private suspend fun partialUpdate(vararg chapterUpdates: ChapterUpdate) {
        database.transaction {
            chapterUpdates.forEach { chapterUpdate ->
                database.chapterQueries.update(
                    read = chapterUpdate.read,
                    bookmark = chapterUpdate.bookmark,
                    lastPageRead = chapterUpdate.lastPageRead,
                    dateFetch = chapterUpdate.dateFetch,
                    chapterId = chapterUpdate.id,
                )
            }
        }
    }

    override suspend fun getChapterByMangaId(mangaId: Long, applyScanlatorFilter: Boolean): List<Chapter> {
        return database.chapterQueries
            .getChaptersByMangaId(
                mangaId = mangaId,
                applyScanlatorFilter = applyScanlatorFilter.toLong(),
                mapper = ::mapChapter,
            )
            .awaitAsList()
    }

    // SY -->
    override suspend fun getChaptersByMangaIds(mangaIds: List<Long>): List<Chapter> {
        return database.ehQueries
            .getChaptersByMangaIds(mangaIds, ::mapChapter)
            .awaitAsList()
    }
    // SY <--

    override suspend fun getChapterSummaryByMangaId(mangaId: Long): ChapterSummary {
        return database.chapterQueries
            .getChapterSummaryByMangaId(mangaId) { total, latestChapterNumber ->
                ChapterSummary(chapterCount = total, latestChapterNumber = latestChapterNumber)
            }
            .awaitAsOne()
    }

    override suspend fun getScanlatorsByMangaId(mangaId: Long): List<String> {
        return database.chapterQueries
            .getScanlatorsByMangaId(mangaId) { it.orEmpty() }
            .awaitAsList()
    }

    override fun getScanlatorsByMangaIdAsFlow(mangaId: Long): Flow<List<String>> {
        return database.chapterQueries
            .getScanlatorsByMangaId(mangaId) { it.orEmpty() }
            .subscribeToList()
    }

    override suspend fun getBookmarkedChaptersByMangaId(mangaId: Long): List<Chapter> {
        return database.chapterQueries
            .getBookmarkedChaptersByMangaId(mangaId, ::mapChapter)
            .awaitAsList()
    }

    override suspend fun getChapterById(id: Long): Chapter? {
        return database.chapterQueries
            .getChapterById(id, ::mapChapter)
            .awaitAsOneOrNull()
    }

    override suspend fun getChapterByMangaIdAsFlow(mangaId: Long, applyScanlatorFilter: Boolean): Flow<List<Chapter>> {
        return database.chapterQueries
            .getChaptersByMangaId(
                mangaId = mangaId,
                applyScanlatorFilter = applyScanlatorFilter.toLong(),
                mapper = ::mapChapter,
            )
            .subscribeToList()
    }

    override suspend fun getChapterByUrlAndMangaId(url: String, mangaId: Long): Chapter? {
        return database.chapterQueries
            .getChapterByUrlAndMangaId(url, mangaId, ::mapChapter)
            .awaitAsOneOrNull()
    }

    // SY -->
    override suspend fun getChapterByUrl(url: String): List<Chapter> {
        return database.chapterQueries
            .getChapterByUrl(url, ::mapChapter)
            .awaitAsList()
    }

    override suspend fun getMergedChapterByMangaId(mangaId: Long, applyScanlatorFilter: Boolean): List<Chapter> {
        return database.chapterQueries
            .getMergedChaptersByMangaId(
                mangaId,
                applyScanlatorFilter.toLong(),
                ::mapChapter,
            )
            .awaitAsList()
    }

    override suspend fun getMergedChapterByMangaIdAsFlow(
        mangaId: Long,
        applyScanlatorFilter: Boolean,
    ): Flow<List<Chapter>> {
        return database.chapterQueries
            .getMergedChaptersByMangaId(
                mangaId,
                applyScanlatorFilter.toLong(),
                ::mapChapter,
            )
            .subscribeToList()
    }

    override suspend fun getScanlatorsByMergeId(mangaId: Long): List<String> {
        return database.chapterQueries
            .getScanlatorsByMergeId(mangaId) { it.orEmpty() }
            .awaitAsList()
    }

    override fun getScanlatorsByMergeIdAsFlow(mangaId: Long): Flow<List<String>> {
        return database.chapterQueries
            .getScanlatorsByMergeId(mangaId) { it.orEmpty() }
            .subscribeToList()
    }

    override suspend fun resetIsSyncing() {
        database.chapterQueries.resetIsSyncing()
    }
    // SY <--
}
