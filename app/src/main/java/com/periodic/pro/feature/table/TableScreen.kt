package com.periodic.pro.feature.table

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.periodic.pro.R
import com.periodic.pro.data.element.model.Category
import com.periodic.pro.data.element.ElementRepository
import com.periodic.pro.data.favorites.FavoritesRepository
import com.periodic.pro.theme.Dimensions
import com.periodic.pro.theme.LocalCategoryColors
import com.periodic.pro.theme.forCategory
import com.periodic.pro.ui.components.CategoryChip
import com.periodic.pro.ui.components.PeriodicSearchBar
import org.koin.compose.koinInject

/**
 * 分类条目定义：ID（Category）与中文显示名。
 */
private data class CategoryEntry(
    val id: Category?,
    val labelRes: Int,
)

/**
 * 10 种元素分类 + "全部"。
 */
private val categoryEntries = listOf(
    CategoryEntry(null, R.string.category_all),
    CategoryEntry(Category.ALKALI_METAL, R.string.category_alkali_metal),
    CategoryEntry(Category.ALKALINE_EARTH, R.string.category_alkaline_earth_metal),
    CategoryEntry(Category.TRANSITION_METAL, R.string.category_transition_metal),
    CategoryEntry(Category.POST_TRANSITION, R.string.category_post_transition_metal),
    CategoryEntry(Category.METALLOID, R.string.category_metalloid),
    CategoryEntry(Category.NONMETAL, R.string.category_nonmetal),
    CategoryEntry(Category.HALOGEN, R.string.category_halogen),
    CategoryEntry(Category.NOBLE_GAS, R.string.category_noble_gas),
    CategoryEntry(Category.LANTHANIDE, R.string.category_lanthanide),
    CategoryEntry(Category.ACTINIDE, R.string.category_actinide),
)

/**
 * 周期表屏入口。
 *
 * @param initialQuery 初始搜索 query（从 Home 搜索进入时携带）
 * @param onNavigateToDetail 导航到元素详情
 * @param onNavigateToCompare 导航到元素对比
 * @param modifier Modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableScreen(
    initialQuery: String = "",
    enterMultiSelect: Boolean = false,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToCompare: (List<Int>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val elementRepo = koinInject<ElementRepository>()
    val favoritesRepo = koinInject<FavoritesRepository>()
    val viewModel = remember { TableViewModel(elementRepo, favoritesRepo) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    // 从 Home 搜索进入时自动填入搜索 query
    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotEmpty()) {
            viewModel.handle(TableIntent.Search(initialQuery))
        }
    }

    // 根据入口切换状态：对比→多选，其他→重置多选
    LaunchedEffect(enterMultiSelect) {
        if (enterMultiSelect) {
            viewModel.handle(TableIntent.EnterMultiSelect)
        } else {
            viewModel.handle(TableIntent.ExitMultiSelect)
        }
    }

    // 收集副作用（导航事件）
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TableEffect.NavigateToDetail -> onNavigateToDetail(effect.atomicNumber)
                is TableEffect.NavigateToCompare -> onNavigateToCompare(effect.ids)
            }
        }
    }

    // 多选模式下的返回键处理
    BackHandler(enabled = state.isMultiSelectMode) {
        viewModel.handle(TableIntent.ExitMultiSelect)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            if (state.isMultiSelectMode) {
                // 多选模式 ContextualTopAppBar
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.multiselect_selected, state.selectedIds.size),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.handle(TableIntent.ExitMultiSelect) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.multiselect_cancel),
                            )
                        }
                    },
                    actions = {
                        Button(
                            onClick = { viewModel.handle(TableIntent.ConfirmCompare) },
                            enabled = state.selectedIds.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) {
                            Text(
                                text = stringResource(R.string.multiselect_confirm),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            } else {
                // 正常模式 TopAppBar
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.screen_table),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // 搜索框
            PeriodicSearchBar(
                query = state.searchQuery,
                onQueryChange = { viewModel.handle(TableIntent.Search(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimensions.Dp16, vertical = Dimensions.Dp8),
                placeholder = stringResource(R.string.search_elements),
            )

            // 分类筛选 Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimensions.Dp16),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.Dp8),
            ) {
                items(categoryEntries, key = { it.id ?: "all" }) { entry ->
                    CategoryChip(
                        text = stringResource(entry.labelRes),
                        selected = state.selectedCategory == entry.id,
                        onClick = { viewModel.handle(TableIntent.FilterByCategory(entry.id)) },
                        selectedColor = if (entry.id != null) {
                            LocalCategoryColors.current.forCategory(entry.id)
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.Dp8))

            // 主体：周期表网格
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.loading),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else {
                Box(modifier = Modifier.weight(1f)) {
                    PeriodicTableGrid(
                        elements = state.elements,
                        zhMap = state.zhMap,
                        selectedCategory = state.selectedCategory,
                        searchQuery = state.searchQuery,
                        isMultiSelectMode = state.isMultiSelectMode,
                        selectedIds = state.selectedIds,
                        onElementClick = { viewModel.handle(TableIntent.OnElementClick(it)) },
                        onElementLongClick = { viewModel.handle(TableIntent.OnElementLongClick(it)) },
                        onSelectSeries = { viewModel.handle(TableIntent.SelectSeries(it)) },
                    )
                }
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TableScreenPreview() {
    com.periodic.pro.theme.PeriodicProTheme {
        TableScreen(
            onNavigateToDetail = {},
            onNavigateToCompare = {},
        )
    }
}
