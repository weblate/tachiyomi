package tachiyomi.domain.manga.repository

import tachiyomi.domain.manga.model.CustomMangaInfo

/**
 * SY-specific file
 */
interface CustomMangaRepository {

    fun get(mangaId: Long): CustomMangaInfo?

    fun set(mangaInfo: CustomMangaInfo)
}
