package eu.kanade.presentation.library.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.PagerState
import eu.kanade.domain.category.model.Category
import eu.kanade.presentation.category.visualName
import eu.kanade.presentation.components.DownloadedOnlyModeBanner
import eu.kanade.presentation.components.IncognitoModeBanner
import eu.kanade.presentation.components.TabIndicator
import eu.kanade.presentation.components.TabText
import kotlinx.coroutines.launch

@Composable
fun LibraryTabs(
    state: PagerState,
    categories: List<Category>,
    showMangaCount: Boolean,
    isDownloadOnly: Boolean,
    isIncognitoMode: Boolean,
    getNumberOfMangaForCategory: @Composable (Long) -> State<Int?>,
    // SY -->
    getCategoryName: @Composable (Category, String) -> String,
    // SY <--
) {
    val scope = rememberCoroutineScope()

    Column {
        ScrollableTabRow(
            selectedTabIndex = state.currentPage.coerceAtMost(categories.lastIndex),
            edgePadding = 0.dp,
            indicator = { TabIndicator(it[state.currentPage.coerceAtMost(categories.lastIndex)]) },
        ) {
            categories.forEachIndexed { index, category ->
                val count by if (showMangaCount) {
                    getNumberOfMangaForCategory(category.id)
                } else {
                    remember { mutableStateOf<Int?>(null) }
                }
                Tab(
                    selected = state.currentPage == index,
                    onClick = { scope.launch { state.animateScrollToPage(index) } },
                    text = {
                        TabText(
                            // SY -->
                            text = getCategoryName(category, category.visualName),
                            // SY <--
                            count,
                            state.currentPage == index,
                        )
                    },
                )
            }
        }
        if (isDownloadOnly) {
            DownloadedOnlyModeBanner()
        }
        if (isIncognitoMode) {
            IncognitoModeBanner()
        }
    }
}