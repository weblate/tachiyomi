package eu.kanade.presentation.browse

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import eu.kanade.domain.manga.model.Manga
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.LoadingScreen
import eu.kanade.presentation.components.Scaffold
import eu.kanade.presentation.components.ScrollbarLazyColumn
import eu.kanade.presentation.components.SearchToolbar
import eu.kanade.presentation.util.plus
import eu.kanade.presentation.util.topPaddingValues
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.browse.source.feed.SourceFeedPresenter
import exh.savedsearches.models.FeedSavedSearch
import exh.savedsearches.models.SavedSearch

sealed class SourceFeedUI {
    abstract val id: Long

    abstract val title: String
        @Composable
        @ReadOnlyComposable
        get

    abstract val results: List<Manga>?

    abstract fun withResults(results: List<Manga>?): SourceFeedUI

    data class Latest(override val results: List<Manga>?) : SourceFeedUI() {
        override val id: Long = -1
        override val title: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(R.string.latest)

        override fun withResults(results: List<Manga>?): SourceFeedUI {
            return copy(results = results)
        }
    }
    data class Browse(override val results: List<Manga>?) : SourceFeedUI() {
        override val id: Long = -2
        override val title: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(R.string.browse)

        override fun withResults(results: List<Manga>?): SourceFeedUI {
            return copy(results = results)
        }
    }
    data class SourceSavedSearch(
        val feed: FeedSavedSearch,
        val savedSearch: SavedSearch,
        override val results: List<Manga>?,
    ) : SourceFeedUI() {
        override val id: Long
            get() = feed.id

        override val title: String
            @Composable
            @ReadOnlyComposable
            get() = savedSearch.name

        override fun withResults(results: List<Manga>?): SourceFeedUI {
            return copy(results = results)
        }
    }
}

@Composable
fun SourceFeedScreen(
    presenter: SourceFeedPresenter,
    onFabClick: () -> Unit,
    onClickBrowse: () -> Unit,
    onClickLatest: () -> Unit,
    onClickSavedSearch: (SavedSearch) -> Unit,
    onClickDelete: (FeedSavedSearch) -> Unit,
    onClickManga: (Manga) -> Unit,
    onClickSearch: () -> Unit,
) {
    Scaffold(
        topBar = { scrollBehavior ->
            SourceFeedToolbar(
                title = presenter.source.name,
                state = presenter,
                scrollBehavior = scrollBehavior,
                incognitoMode = presenter.isIncognitoMode,
                downloadedOnlyMode = presenter.isDownloadOnly,
                onClickSearch = onClickSearch,
            )
        },
        floatingActionButton = {
            BrowseSourceFloatingActionButton(
                isVisible = presenter.filterItems.isNotEmpty(),
                onFabClick = onFabClick,
            )
        },
    ) { paddingValues ->
        Crossfade(targetState = presenter.isLoading) { state ->
            when (state) {
                true -> LoadingScreen()
                false -> {
                    SourceFeedList(
                        state = presenter,
                        paddingValues = paddingValues,
                        getMangaState = { presenter.getManga(it) },
                        onClickBrowse = onClickBrowse,
                        onClickLatest = onClickLatest,
                        onClickSavedSearch = onClickSavedSearch,
                        onClickDelete = onClickDelete,
                        onClickManga = onClickManga,
                    )
                }
            }
        }
    }
}

@Composable
fun SourceFeedList(
    state: SourceFeedState,
    paddingValues: PaddingValues,
    getMangaState: @Composable ((Manga) -> State<Manga>),
    onClickBrowse: () -> Unit,
    onClickLatest: () -> Unit,
    onClickSavedSearch: (SavedSearch) -> Unit,
    onClickDelete: (FeedSavedSearch) -> Unit,
    onClickManga: (Manga) -> Unit,
) {
    ScrollbarLazyColumn(
        contentPadding = paddingValues + topPaddingValues,
    ) {
        items(
            state.items.orEmpty(),
            key = { it.id },
        ) { item ->
            SourceFeedItem(
                modifier = Modifier.animateItemPlacement(),
                item = item,
                getMangaState = getMangaState,
                onClickTitle = when (item) {
                    is SourceFeedUI.Browse -> onClickBrowse
                    is SourceFeedUI.Latest -> onClickLatest
                    is SourceFeedUI.SourceSavedSearch -> {
                        { onClickSavedSearch(item.savedSearch) }
                    }
                },
                onClickDelete = onClickDelete,
                onClickManga = onClickManga,
            )
        }
    }
}

@Composable
fun SourceFeedItem(
    modifier: Modifier,
    item: SourceFeedUI,
    getMangaState: @Composable ((Manga) -> State<Manga>),
    onClickTitle: () -> Unit,
    onClickDelete: (FeedSavedSearch) -> Unit,
    onClickManga: (Manga) -> Unit,
) {
    Column(
        modifier then Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .let {
                    if (item is SourceFeedUI.SourceSavedSearch) {
                        it.combinedClickable(
                            onLongClick = {
                                onClickDelete(item.feed)
                            },
                            onClick = onClickTitle,
                        )
                    } else {
                        it.clickable(onClick = onClickTitle)
                    }
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.padding(start = 16.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_arrow_forward_24dp),
                contentDescription = stringResource(R.string.label_more),
                modifier = Modifier.padding(16.dp),
            )
        }
        val results = item.results
        when {
            results == null -> {
                CircularProgressIndicator()
            }
            results.isEmpty() -> {
                Text(stringResource(R.string.no_results_found), modifier = Modifier.padding(bottom = 16.dp))
            }
            else -> {
                LazyRow(
                    Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                ) {
                    items(results) {
                        val manga by getMangaState(it)
                        FeedCardItem(
                            manga = manga,
                            onClickManga = onClickManga,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SourceFeedToolbar(
    title: String,
    state: SourceFeedState,
    scrollBehavior: TopAppBarScrollBehavior,
    incognitoMode: Boolean,
    downloadedOnlyMode: Boolean,
    onClickSearch: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    when {
        state.searchQuery != null -> SearchToolbar(
            searchQuery = state.searchQuery!!,
            onChangeSearchQuery = { state.searchQuery = it },
            onClickCloseSearch = { state.searchQuery = null },
            onClickResetSearch = { state.searchQuery = "" },
            scrollBehavior = scrollBehavior,
            incognitoMode = incognitoMode,
            downloadedOnlyMode = downloadedOnlyMode,
            placeholderText = stringResource(R.string.action_search_hint),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onClickSearch()
                    focusManager.clearFocus()
                    keyboardController?.hide()
                },
            ),
        )
        else -> AppBar(
            title = title,
            incognitoMode = incognitoMode,
            downloadedOnlyMode = downloadedOnlyMode,
            scrollBehavior = scrollBehavior,
            actions = {
                IconButton(onClick = { state.searchQuery = "" }) {
                    Icon(Icons.Outlined.Search, contentDescription = stringResource(R.string.action_search))
                }
            },
        )
    }
}