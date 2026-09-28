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
        updateStrategy: UpdateStrategy,
        calculateInterval: Long,
        notes: String,
        memo: JsonObject,
        favoriteAt: Long?,
        // SY -->
        lastModifiedAt: Long,
        version: Long,
        isSyncing: Long,
        // SY <--
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
        updateStrategy: UpdateStrategy,
        calculateInterval: Long,
        notes: String,
        memo: JsonObject,
        favoriteAt: Long?,
        // SY -->
        lastModifiedAt: Long,
        version: Long,
        isSyncing: Long,
        // SY <--
        totalCount: Long,
        readCount: Double,
        latestUpload: Long,
        chapterFetchedAt: Long,
        lastRead: Long,
        bookmarkCount: Double,
        categories: String,
    ): LibraryManga = LibraryManga(
        manga = mapManga(
            id = id,
            source = source,
            url = url,
            artist = artist,
            author = author,
            description = description,
            genre = genre,
            title = title,
            status = status,
            thumbnailUrl = thumbnailUrl,
            lastUpdate = lastUpdate,
            nextUpdate = nextUpdate,
            initialized = initialized,
            viewerFlags = viewerFlags,
            chapterFlags = chapterFlags,
            coverLastModified = coverLastModified,
            updateStrategy = updateStrategy,
            calculateInterval = calculateInterval,
            // SY -->
            lastModifiedAt = lastModifiedAt,
            version = version,
            isSyncing = isSyncing,
            // SY <--
            notes = notes,
            memo = memo,
            favoriteAt = favoriteAt,
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
        updateStrategy: UpdateStrategy,
        calculateInterval: Long,
        notes: String,
        memo: JsonObject,
        favoriteAt: Long?,
        // SY -->
        lastModifiedAt: Long,
        version: Long,
        isSyncing: Long,
        // SY <--
        totalCount: Long,
    ): MangaWithChapterCount = MangaWithChapterCount(
        manga = mapManga(
            id = id,
            source = source,
            url = url,
            artist = artist,
            author = author,
            description = description,
            genre = genre,
            title = title,
            status = status,
            thumbnailUrl = thumbnailUrl,
            lastUpdate = lastUpdate,
            nextUpdate = nextUpdate,
            initialized = initialized,
            viewerFlags = viewerFlags,
            chapterFlags = chapterFlags,
            coverLastModified = coverLastModified,
            updateStrategy = updateStrategy,
            calculateInterval = calculateInterval,
            // SY -->
            lastModifiedAt = lastModifiedAt,
            version = version,
            isSyncing = isSyncing,
            // SY <--
            notes = notes,
            memo = memo,
            favoriteAt = favoriteAt,
        ),
        chapterCount = totalCount,
    )

    fun mapLibraryView(libraryView: LibraryView): LibraryManga {
        return LibraryManga(
            manga = Manga(
                id = libraryView.id,
                source = libraryView.source_id,
                favoriteAt = libraryView.user_favorite_at,
                lastUpdate = libraryView.state_chapter_last_update ?: 0,
                nextUpdate = libraryView.state_chapter_next_update ?: 0,
                viewerFlags = libraryView.user_reader_flags,
                chapterFlags = libraryView.user_chapter_flags,
                coverLastModified = libraryView.state_cover_last_modified,
                url = libraryView.remote_url,
                ogTitle = libraryView.remote_title,
                ogArtist = libraryView.remote_artist,
                ogAuthor = libraryView.remote_author,
                ogDescription = libraryView.remote_description,
                ogGenre = libraryView.remote_genre,
                ogStatus = libraryView.remote_status,
                ogThumbnailUrl = libraryView.remote_cover,
                updateStrategy = libraryView.remote_update_strategy,
                initialized = libraryView.state_initialized,
                fetchInterval = libraryView.state_chapter_fetch_interval.toInt(),
                // SY -->
                lastModifiedAt = libraryView.state_last_modified_at,
                version = libraryView.state_version,
                // SY <--
                notes = libraryView.user_notes,
                memo = libraryView.remote_memo,
                // SY -->
                customMangaInfo = if (libraryView.user_favorite_at != null) getCustomMangaInfo.get(libraryView.id) else null,
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
