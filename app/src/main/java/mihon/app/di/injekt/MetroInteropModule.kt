package mihon.app.di.injekt

import android.app.Application
import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import dev.zacsweers.metro.Inject
import eu.kanade.domain.chapter.interactor.SyncChaptersWithSource
import eu.kanade.domain.manga.interactor.UpdateManga
import eu.kanade.domain.source.service.SourcePreferences
import eu.kanade.domain.sync.SyncPreferences
import eu.kanade.domain.track.service.TrackPreferences
import eu.kanade.tachiyomi.core.security.SecurityPreferences
import eu.kanade.tachiyomi.data.cache.CoverCache
import eu.kanade.tachiyomi.data.cache.PagePreviewCache
import eu.kanade.tachiyomi.data.download.DownloadManager
import eu.kanade.tachiyomi.data.track.TrackerManager
import eu.kanade.tachiyomi.di.InjektModule
import eu.kanade.tachiyomi.di.addLazySingleton
import eu.kanade.tachiyomi.di.addSingleton
import eu.kanade.tachiyomi.extension.ExtensionManager
import eu.kanade.tachiyomi.network.JavaScriptEngine
import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.network.NetworkPreferences
import eu.kanade.tachiyomi.source.online.MetadataSource
import exh.GalleryAdder
import exh.eh.EHentaiUpdateHelper
import exh.pref.DelegateSourcePreferences
import exh.source.ExhPreferences
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import mihon.domain.chapter.interactor.FilterChaptersForDownload
import mihon.domain.source.interactor.UpdateMangaFromRemote
import nl.adaptivity.xmlutil.serialization.XML
import tachiyomi.core.common.preference.PreferenceStore
import tachiyomi.data.Database
import tachiyomi.domain.category.interactor.GetCategories
import tachiyomi.domain.download.service.DownloadPreferences
import tachiyomi.domain.manga.interactor.GetAllManga
import tachiyomi.domain.manga.interactor.GetCustomMangaInfo
import tachiyomi.domain.manga.interactor.GetExhFavoriteMangaWithMetadata
import tachiyomi.domain.manga.interactor.GetFavorites
import tachiyomi.domain.manga.interactor.GetFlatMetadataById
import tachiyomi.domain.manga.interactor.GetManga
import tachiyomi.domain.manga.interactor.GetMergedReferencesById
import tachiyomi.domain.manga.interactor.GetSearchMetadata
import tachiyomi.domain.manga.interactor.InsertFlatMetadata
import tachiyomi.domain.manga.interactor.NetworkToLocalManga
import tachiyomi.domain.source.service.SourceManager
import uy.kohesive.injekt.api.InjektRegistrar

/**
 * Instances are resolved on first `Injekt.get`, so creating the module at startup doesn't create them.
 */
@Inject
class MetroInteropModule(
    private val json: Lazy<Json>,
    private val protoBuf: Lazy<ProtoBuf>,
    private val xml: Lazy<XML>,

    private val networkHelper: Lazy<NetworkHelper>,
    private val javaScriptEngine: Lazy<JavaScriptEngine>,

    private val preferenceStore: Lazy<PreferenceStore>,
    private val trackPreferences: Lazy<TrackPreferences>,

    private val extensionManager: Lazy<ExtensionManager>,

    private val coverCache: Lazy<CoverCache>,

    // SY -->
    // Bridge the fork's dependencies into Injekt so the existing Injekt.get<T>()
    // call sites keep working now that the graph is Metro-backed.
    private val context: Context,

    private val sourcePreferences: Lazy<SourcePreferences>,

    private val sourceManager: Lazy<SourceManager>,
    private val trackerManager: Lazy<TrackerManager>,

    private val getFavorites: Lazy<GetFavorites>,
    private val networkToLocalManga: Lazy<NetworkToLocalManga>,

    private val database: Lazy<Database>,
    private val updateMangaFromRemote: Lazy<UpdateMangaFromRemote>,
    private val insertFlatMetadata: Lazy<InsertFlatMetadata>,
    private val getManga: Lazy<GetManga>,
    private val getMergedReferencesById: Lazy<GetMergedReferencesById>,
    private val syncChaptersWithSource: Lazy<SyncChaptersWithSource>,
    private val updateManga: Lazy<UpdateManga>,
    private val getCategories: Lazy<GetCategories>,
    private val downloadManager: Lazy<DownloadManager>,
    private val downloadPreferences: Lazy<DownloadPreferences>,
    private val filterChaptersForDownload: Lazy<FilterChaptersForDownload>,

    private val exhPreferences: Lazy<ExhPreferences>,
    private val syncPreferences: Lazy<SyncPreferences>,
    private val pagePreviewCache: Lazy<PagePreviewCache>,
    private val getAllManga: Lazy<GetAllManga>,
    private val getFlatMetadataById: Lazy<GetFlatMetadataById>,
    private val metadataSourceGetMangaId: Lazy<MetadataSource.GetMangaId>,
    private val metadataSourceInsertFlatMetadata: Lazy<MetadataSource.InsertFlatMetadata>,
    private val metadataSourceGetFlatMetadataById: Lazy<MetadataSource.GetFlatMetadataById>,
    private val galleryAdder: Lazy<GalleryAdder>,
    private val getExhFavoriteMangaWithMetadata: Lazy<GetExhFavoriteMangaWithMetadata>,
    private val getSearchMetadata: Lazy<GetSearchMetadata>,
    private val ehentaiUpdateHelper: Lazy<EHentaiUpdateHelper>,
    private val customMangaInfo: Lazy<GetCustomMangaInfo>,
    private val networkPreferences: Lazy<NetworkPreferences>,
    private val delegateSourcePreferences: Lazy<DelegateSourcePreferences>,
    private val sqlDriver: Lazy<SqlDriver>,
    private val securityPreferences: Lazy<SecurityPreferences>,
    // SY <--
) : InjektModule {

    override fun InjektRegistrar.registerInjectables() {
        addLazySingleton(json)
        addLazySingleton(protoBuf)
        addLazySingleton(xml)

        addLazySingleton(networkHelper)
        addLazySingleton(javaScriptEngine)

        addLazySingleton(preferenceStore)
        addLazySingleton(trackPreferences)

        addLazySingleton(extensionManager)

        addLazySingleton(coverCache)

        // SY -->
        addSingleton(context.applicationContext as Application)
        addSingleton(context)

        addLazySingleton(sourcePreferences)

        addLazySingleton(sourceManager)
        addLazySingleton(trackerManager)

        addLazySingleton(getFavorites)
        addLazySingleton(networkToLocalManga)

        addLazySingleton(database)
        addLazySingleton(updateMangaFromRemote)
        addLazySingleton(insertFlatMetadata)
        addLazySingleton(getManga)
        addLazySingleton(getMergedReferencesById)
        addLazySingleton(syncChaptersWithSource)
        addLazySingleton(updateManga)
        addLazySingleton(getCategories)
        addLazySingleton(downloadManager)
        addLazySingleton(downloadPreferences)
        addLazySingleton(filterChaptersForDownload)

        addLazySingleton(exhPreferences)
        addLazySingleton(syncPreferences)
        addLazySingleton(pagePreviewCache)
        addLazySingleton(getAllManga)
        addLazySingleton(getFlatMetadataById)
        addLazySingleton(galleryAdder)
        addLazySingleton(getExhFavoriteMangaWithMetadata)
        addLazySingleton(getSearchMetadata)
        addLazySingleton(ehentaiUpdateHelper)
        addLazySingleton(customMangaInfo)
        addLazySingleton(networkPreferences)
        addLazySingleton(delegateSourcePreferences)
        addLazySingleton(sqlDriver)
        addLazySingleton(securityPreferences)
        addLazySingleton(metadataSourceGetMangaId)
        addLazySingleton(metadataSourceInsertFlatMetadata)
        addLazySingleton(metadataSourceGetFlatMetadataById)
        // SY <--
    }
}
