package eu.kanade.tachiyomi.extension.api

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import eu.kanade.domain.source.service.SourcePreferences
import eu.kanade.tachiyomi.extension.model.Extension
import exh.source.BlacklistedSources
import mihon.domain.extension.interactor.UpdateExtensionStores
import mihon.domain.extension.repository.ExtensionStoreRepository
import tachiyomi.core.common.util.lang.withIOContext

@Inject
@SingleIn(AppScope::class)
class ExtensionApi(
    private val repository: ExtensionStoreRepository,
    private val updateExtensionStores: UpdateExtensionStores,
    private val extensionUpdateNotifier: ExtensionUpdateNotifier,
    // SY -->
    private val sourcePreferences: SourcePreferences,
    // SY <--
) {

    suspend fun findExtensions(): List<Extension.Available> {
        return withIOContext { repository.fetchExtensions() }
    }

    /**
     * @param loadedExtensions Extensions already loaded by [eu.kanade.tachiyomi.extension.ExtensionManager].
     * Only their versions are read, so there's nothing to gain from loading them a second time.
     */
    suspend fun checkForUpdates(loadedExtensions: List<Extension.Loaded>) {
        updateExtensionStores()

        val extensions = findExtensions()

        // SY -->
        val blacklistEnabled = sourcePreferences.enableSourceBlacklist.get()
        // SY <--

        val extensionsWithUpdate = loadedExtensions.filter {
            /* SY --> */ !it.isBlacklisted(blacklistEnabled) && /* SY <-- */ it.findUpdate(extensions) != null
        }

        if (extensionsWithUpdate.isNotEmpty()) {
            extensionUpdateNotifier.promptUpdates(extensionsWithUpdate.map { it.name })
        }
    }

    // SY -->
    private fun Extension.isBlacklisted(
        blacklistEnabled: Boolean = sourcePreferences.enableSourceBlacklist.get(),
    ): Boolean {
        return pkgName in BlacklistedSources.BLACKLISTED_EXTENSIONS && blacklistEnabled
    }
    // SY <--
}
