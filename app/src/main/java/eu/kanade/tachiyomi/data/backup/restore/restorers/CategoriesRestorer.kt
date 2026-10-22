package eu.kanade.tachiyomi.data.backup.restore.restorers

import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import eu.kanade.tachiyomi.data.backup.models.BackupCategory
import tachiyomi.domain.category.interactor.GetCategories
import tachiyomi.domain.category.repository.CategoryRepository
import tachiyomi.domain.library.service.LibraryPreferences

@AssistedInject
class CategoriesRestorer(
// SY -->
    @Assisted private val isSync: Boolean,
    private val categoryRepository: CategoryRepository,
    // SY <--
    private val getCategories: GetCategories,
    private val libraryPreferences: LibraryPreferences,
) {
    // SY -->
    @AssistedFactory
    fun interface Factory {
        fun create(isSync: Boolean): CategoriesRestorer
    }
    // SY <--

    suspend operator fun invoke(backupCategories: List<BackupCategory>) {
        if (backupCategories.isNotEmpty()) {
            val dbCategories = getCategories.await()
            val dbCategoriesByName = dbCategories.associateBy { it.name }
            // SY -->
            val dbCategoriesByUid = dbCategories.associateBy { it.uid } // Map by UID
            // SY <--

            var nextOrder = dbCategories.maxOfOrNull { it.order }?.plus(1) ?: 0

            val categories = backupCategories
                .sortedBy { it.order }
// SY -->
                .map { backupCategory ->
                    var dbCategory = if (backupCategory.uid != 0L) {
                        dbCategoriesByUid[backupCategory.uid]
                    } else {
                        null
                    }

                    if (dbCategory == null) {
                        dbCategory = dbCategoriesByName[backupCategory.name]
                    }

                    if (dbCategory != null) {
                        categoryRepository.updateSync(
                            name = backupCategory.name,
                            order = backupCategory.order,
                            flags = backupCategory.flags,
                            version = backupCategory.version,
                            uid = if (backupCategory.uid != 0L) backupCategory.uid else dbCategory.uid,
                            lastModifiedAt = backupCategory.lastModifiedAt,
                            categoryId = dbCategory.id,
                        )
                        return@map dbCategory
                    }

                    // A sync delta is a converging replica, not an import: new
                    // categories must land at the position the server holds or
                    // reorders never converge across devices.
                    val order = if (isSync) backupCategory.order else nextOrder++
                    categoryRepository.insertWithSync(
                        name = backupCategory.name,
                        order = order,
                        flags = backupCategory.flags,
                        version = backupCategory.version,
                        uid = backupCategory.uid,
                        lastModifiedAt = backupCategory.lastModifiedAt,
                    ).let { id -> backupCategory.toCategory(id).copy(order = order) }
                }

            categoryRepository.resetIsSyncing()
            // SY <--

            libraryPreferences.categorizedDisplaySettings.set(
                (dbCategories + categories)
                    .distinctBy { it.flags }
                    .size > 1,
            )
        }
    }
}
