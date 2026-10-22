package tachiyomi.data.manga

import dev.zacsweers.metro.Inject
import eu.kanade.tachiyomi.source.model.UpdateStrategy
import kotlinx.serialization.json.JsonObject
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.manga.interactor.GetCustomMangaInfo
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.model.MangaWithChapterCount
import tachiyomi.view.LibraryView

@Inject
class MangaMapper(
    private val getCustomMangaInfo: GetCustomMangaInfo,
) {
    @Suppress("UNUSED_PARAMETER")
    fun mapManga(
        id: Long,
        source: Long,
        url: String,
        artist: String?,
        author: String?,
        description: String?,
        genre: List<String>?,
        title: String,
        status: Long,
        thumbnailUrl: String?,
        lastUpdate: Long?,
        nextUpdate: Long?,
        initialized: Boolean,
        viewerFlags: Long,
        chapterFlags: Long,
        coverLastModified: Long,
        // SY -->
        @Suppress("UNUSED_PARAMETER")
        filteredScanlators: String?,
        // SY <--
        updateStrategy: UpdateStrategy,
        calculateInterval: Long,
        // SY -->
        lastModifiedAt: Long,
        version: Long,
        isSyncing: Long,
        // SY <--
        notes: String,
        memo: JsonObject,
        favoriteAt: Long?,
    ): Manga = Manga(
        id = id,
        source = source,
        lastUpdate = lastUpdate ?: 0,
        nextUpdate = nextUpdate ?: 0,
        fetchInterval = calculateInterval.toInt(),
        favoriteAt = favoriteAt,
        viewerFlags = viewerFlags,
        chapterFlags = chapterFlags,
        coverLastModified = coverLastModified,
        url = url,
        // SY -->
        ogTitle = title,
        ogArtist = artist,
        ogAuthor = author,
        ogThumbnailUrl = thumbnailUrl,
        ogDescription = description,
        ogGenre = genre,
        ogStatus = status,
        // SY <--
        updateStrategy = updateStrategy,
        initialized = initialized,
// SY -->
        lastModifiedAt = lastModifiedAt,
        version = version,
        // SY <--
        notes = notes,
        memo = memo,
        // SY -->
        customMangaInfo = if (favoriteAt != null) getCustomMangaInfo.get(id) else null,
        // SY <--
    )

    fun mapLibraryManga(
        id: Long,
        source: Long,
        url: String,
        artist: String?,
        author: String?,
        description: String?,
        genre: List<String>?,
        title: String,
        status: Long,
        thumbnailUrl: String?,
        lastUpdate: Long?,
        nextUpdate: Long?,
        initialized: Boolean,
        viewerFlags: Long,
        chapterFlags: Long,
        coverLastModified: Long,
        // SY -->
        @Suppress("UNUSED_PARAMETER")
        filteredScanlators: String?,
        // SY <--
        updateStrategy: UpdateStrategy,
        calculateInterval: Long,
        // SY -->
        lastModifiedAt: Long,
        version: Long,
        isSyncing: Long,
        // SY <--
        notes: String,
        memo: JsonObject,
        favoriteAt: Long?,
        totalCount: Long,
        readCount: Double,
        latestUpload: Long,
        chapterFetchedAt: Long,
        lastRead: Long,
        bookmarkCount: Double,
        categories: String,
    ): LibraryManga = LibraryManga(
        manga = mapManga(
            id,
            source,
            url,
            artist,
            author,
            description,
            genre,
            title,
            status,
            thumbnailUrl,
            lastUpdate,
            nextUpdate,
            initialized,
            viewerFlags,
            chapterFlags,
            coverLastModified,
            // SY -->
            null,
            // SY <--
            updateStrategy,
            calculateInterval,
            // SY -->
            lastModifiedAt,
            version,
            isSyncing,
            // SY <--
            notes,
            memo,
            favoriteAt,
        ),
        categories = categories.split(",").map { it.toLong() },
        totalChapters = totalCount,
        readCount = readCount.toLong(),
        bookmarkCount = bookmarkCount.toLong(),
        latestUpload = latestUpload,
        chapterFetchedAt = chapterFetchedAt,
        lastRead = lastRead,
    )

    fun mapMangaWithChapterCount(
        id: Long,
        source: Long,
        url: String,
        artist: String?,
        author: String?,
        description: String?,
        genre: List<String>?,
        title: String,
        status: Long,
        thumbnailUrl: String?,
        lastUpdate: Long?,
        nextUpdate: Long?,
        initialized: Boolean,
        viewerFlags: Long,
        chapterFlags: Long,
        coverLastModified: Long,
        // SY -->
        @Suppress("UNUSED_PARAMETER")
        filteredScanlators: String?,
        // SY <--
        updateStrategy: UpdateStrategy,
        calculateInterval: Long,
        // SY -->
        lastModifiedAt: Long,
        version: Long,
        isSyncing: Long,
        // SY <--
        notes: String,
        memo: JsonObject,
        favoriteAt: Long?,
        totalCount: Long,
    ): MangaWithChapterCount = MangaWithChapterCount(
        manga = mapManga(
            id,
            source,
            url,
            artist,
            author,
            description,
            genre,
            title,
            status,
            thumbnailUrl,
            lastUpdate,
            nextUpdate,
            initialized,
            viewerFlags,
            chapterFlags,
            coverLastModified,
            // SY -->
            null,
            // SY <--
            updateStrategy,
            calculateInterval,
            // SY -->
            lastModifiedAt,
            version,
            isSyncing,
            // SY <--
            notes,
            memo,
            favoriteAt,
        ),
        chapterCount = totalCount,
    )

    fun mapLibraryView(libraryView: LibraryView): LibraryManga {
        return LibraryManga(
            manga = Manga(
                id = libraryView._id,
                source = libraryView.source,
                favoriteAt = libraryView.favorite_at,
                lastUpdate = libraryView.last_update ?: 0,
                nextUpdate = libraryView.next_update ?: 0,
                viewerFlags = libraryView.viewer,
                chapterFlags = libraryView.chapter_flags,
                coverLastModified = libraryView.cover_last_modified,
                url = libraryView.url,
                ogTitle = libraryView.title,
                ogArtist = libraryView.artist,
                ogAuthor = libraryView.author,
                ogDescription = libraryView.description,
                ogGenre = libraryView.genre,
                ogStatus = libraryView.status,
                ogThumbnailUrl = libraryView.thumbnail_url,
                updateStrategy = libraryView.update_strategy,
                initialized = libraryView.initialized,
                fetchInterval = libraryView.calculate_interval.toInt(),
                // SY -->
                lastModifiedAt = libraryView.last_modified_at,
                version = libraryView.version,
                // SY <--
                notes = libraryView.notes,
                memo = libraryView.memo,
                // SY -->
                customMangaInfo = if (libraryView.favorite_at > 0) getCustomMangaInfo.get(libraryView._id) else null,
                // SY <--
            ),
            categories = libraryView.categories.split(",").map { it.toLong() },
            totalChapters = libraryView.totalCount,
            readCount = libraryView.readCount.toLong(),
            bookmarkCount = libraryView.bookmarkCount.toLong(),
            latestUpload = libraryView.latestUpload,
            chapterFetchedAt = libraryView.chapterFetchedAt,
            lastRead = libraryView.lastRead,
        )
    }
}
