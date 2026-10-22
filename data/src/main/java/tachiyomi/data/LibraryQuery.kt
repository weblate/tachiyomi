package tachiyomi.data

import app.cash.sqldelight.ExecutableQuery
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import exh.source.MERGED_SOURCE_ID
import tachiyomi.view.LibraryView

private val mapper = { cursor: SqlCursor ->
    LibraryView(
        _id = cursor.getLong(0)!!,
        source = cursor.getLong(1)!!,
        url = cursor.getString(2)!!,
        artist = cursor.getString(3),
        author = cursor.getString(4),
        description = cursor.getString(5),
        genre = cursor.getString(6)?.let(StringListColumnAdapter::decode),
        title = cursor.getString(7)!!,
        status = cursor.getLong(8)!!,
        thumbnail_url = cursor.getString(9),
        last_update = cursor.getLong(10),
        next_update = cursor.getLong(11),
        initialized = cursor.getLong(12)!! == 1L,
        viewer = cursor.getLong(13)!!,
        chapter_flags = cursor.getLong(14)!!,
        cover_last_modified = cursor.getLong(15)!!,
        filtered_scanlators = cursor.getString(16),
        update_strategy = UpdateStrategyColumnAdapter.decode(cursor.getLong(17)!!),
        calculate_interval = cursor.getLong(18)!!,
        last_modified_at = cursor.getLong(19)!!,
        version = cursor.getLong(20)!!,
        is_syncing = cursor.getLong(21)!!,
        notes = cursor.getString(22)!!,
        memo = MemoColumnAdapter.decode(cursor.getBytes(23)!!),
        favorite_at = cursor.getLong(24) ?: 0L,
        totalCount = cursor.getLong(25)!!,
        readCount = cursor.getDouble(26)!!,
        latestUpload = cursor.getLong(27)!!,
        chapterFetchedAt = cursor.getLong(28)!!,
        lastRead = cursor.getLong(29)!!,
        bookmarkCount = cursor.getDouble(30)!!,
        categories = cursor.getString(31)!!,
    )
}

fun getLibraryQuery(driver: SqlDriver, condition: String = "M.favorite_at IS NOT NULL"): LibraryQuery {
    return LibraryQuery(
        driver,
        condition,
    )
}

class LibraryQuery(
    val driver: SqlDriver,
    val condition: String = "M.favorite_at IS NOT NULL",
) : ExecutableQuery<LibraryView>(mapper) {

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> {
        return driver.executeQuery(
            null,
            """
            SELECT
                M.*,
                coalesce(C.total, 0) AS totalCount,
                coalesce(C.readCount, 0) AS readCount,
                coalesce(C.latestUpload, 0) AS latestUpload,
                coalesce(C.fetchedAt, 0) AS chapterFetchedAt,
                coalesce(C.lastRead, 0) AS lastRead,
                coalesce(C.bookmarkCount, 0) AS bookmarkCount,
                coalesce(MC.categories, '0') AS categories
            FROM mangas M
            LEFT JOIN (
                SELECT
                    chapters.manga_id,
                    count(*) AS total,
                    sum(read) AS readCount,
                    coalesce(max(chapters.date_upload), 0) AS latestUpload,
                    coalesce(max(history.last_read), 0) AS lastRead,
                    coalesce(max(chapters.date_fetch), 0) AS fetchedAt,
                    sum(chapters.bookmark) AS bookmarkCount
                FROM chapters
                LEFT JOIN excluded_scanlators
                ON chapters.manga_id = excluded_scanlators.manga_id
                AND chapters.scanlator = excluded_scanlators.scanlator
                LEFT JOIN history
                ON chapters._id = history.chapter_id
                WHERE excluded_scanlators.scanlator IS NULL
                GROUP BY chapters.manga_id
            ) AS C
            ON M._id = C.manga_id
            LEFT JOIN (
                SELECT manga_id, group_concat(category_id) AS categories
                FROM mangas_categories
                GROUP BY manga_id
            ) AS MC
            ON MC.manga_id = M._id
            WHERE $condition AND M.source <> $MERGED_SOURCE_ID
            UNION
            SELECT
                M.*,
                coalesce(C.total, 0) AS totalCount,
                coalesce(C.readCount, 0) AS readCount,
                coalesce(C.latestUpload, 0) AS latestUpload,
                coalesce(C.fetchedAt, 0) AS chapterFetchedAt,
                coalesce(C.lastRead, 0) AS lastRead,
                coalesce(C.bookmarkCount, 0) AS bookmarkCount,
                coalesce(MC.categories, '0') AS categories
            FROM mangas M
            LEFT JOIN (
                SELECT merged.manga_id,merged.merge_id
                FROM merged
                GROUP BY merged.merge_id
            ) as ME
            ON ME.merge_id = M._id
            LEFT JOIN (
                SELECT
                    ME.merge_id,
                    count(*) AS total,
                    sum(read) AS readCount,
                    coalesce(max(chapters.date_upload), 0) AS latestUpload,
                    coalesce(max(history.last_read), 0) AS lastRead,
                    coalesce(max(chapters.date_fetch), 0) AS fetchedAt,
                    sum(chapters.bookmark) AS bookmarkCount
                FROM chapters
                LEFT JOIN excluded_scanlators
                ON chapters.manga_id = excluded_scanlators.manga_id
                AND chapters.scanlator = excluded_scanlators.scanlator
                LEFT JOIN history
                ON chapters._id = history.chapter_id
                LEFT JOIN merged as ME
                ON ME.manga_id = chapters.manga_id
                WHERE excluded_scanlators.scanlator IS NULL
                GROUP BY ME.merge_id
            ) AS C
            ON ME.merge_id = C.merge_id
            LEFT JOIN (
                SELECT manga_id, group_concat(category_id) AS categories
                FROM mangas_categories
                GROUP BY manga_id
            ) AS MC
            ON MC.manga_id = M._id
            WHERE $condition AND M.source = $MERGED_SOURCE_ID;
            """.trimIndent(),
            mapper,
            parameters = 0,
        )
    }

    override fun toString(): String = "LibraryQuery.sq:get"
}
