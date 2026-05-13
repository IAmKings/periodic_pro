package com.periodic.pro.feature.category

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.Flare
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.PropaneTank
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.periodic.pro.R
import com.periodic.pro.data.element.model.Category
import com.periodic.pro.data.element.model.Element
import com.periodic.pro.theme.Dimensions
import com.periodic.pro.theme.Elevation
import com.periodic.pro.theme.LocalCategoryColors
import com.periodic.pro.theme.PeriodicProTheme
import com.periodic.pro.theme.forCategory
import com.periodic.pro.ui.components.ElementCard
import org.koin.androidx.compose.koinViewModel

/**
 * 分类屏入口。
 *
 * 显示分类入口 Grid 和分类详情列表两种状态，由 [selectedCategory] 状态切换。
 * 如果提供 [initialCategoryId]，则直接显示该分类的元素列表。
 *
 * @param onNavigateToDetail 导航到元素详情
 * @param initialCategoryId 初始分类 ID（可选，用于从外部导航到特定分类）
 * @param modifier Modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    onNavigateToDetail: (Int) -> Unit,
    initialCategoryId: String? = null,
    modifier: Modifier = Modifier,
) {
    val viewModel: CategoryViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    // 如果提供了 initialCategoryId 且当前没有选中分类且数据已加载，自动选择
    if (initialCategoryId != null && state.selectedCategory == null && !state.isLoading) {
        val category = Category.entries.find { it.id == initialCategoryId }
        if (category != null) {
            viewModel.handle(CategoryIntent.SelectCategory(category))
        }
    }

    if (state.isLoading) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.category_all)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            },
            modifier = modifier,
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.loading),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    } else if (state.selectedCategory != null) {
        // 分类详情列表
        CategoryDetailContent(
            category = state.selectedCategory!!,
            elements = state.categoryElements,
            zhMap = state.zhMap,
            onNavigateBack = { viewModel.handle(CategoryIntent.NavigateBack) },
            onElementClick = onNavigateToDetail,
            modifier = modifier,
        )
    } else {
        // 分类入口 Grid
        CategoryEntranceContent(
            categories = state.categories,
            elementCounts = state.elementCounts,
            onCategoryClick = { category ->
                viewModel.handle(CategoryIntent.SelectCategory(category))
            },
            modifier = modifier,
        )
    }
}

/**
 * 分类入口 Grid 内容。
 *
 * 2 列网格布局，每个卡片展示分类色条、图标、名称和元素数量。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryEntranceContent(
    categories: List<Category>,
    elementCounts: Map<Category, Int>,
    onCategoryClick: (Category) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.category_all),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        modifier = modifier,
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(Dimensions.Dp16),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Dp12),
            verticalArrangement = Arrangement.spacedBy(Dimensions.Dp12),
        ) {
            items(categories, key = { it.id }) { category ->
                CategoryEntranceCard(
                    category = category,
                    elementCount = elementCounts[category] ?: 0,
                    onClick = { onCategoryClick(category) },
                )
            }
        }
    }
}

/**
 * 分类入口卡片。
 *
 * 顶部 4dp 分类色条 + 图标 + 分类名 + 元素数量。
 */
@Composable
private fun CategoryEntranceCard(
    category: Category,
    elementCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categoryColor = LocalCategoryColors.current.forCategory(category)

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.Shadow1),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.Dp16),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 分类色条（4dp 高）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(categoryColor),
            )

            Spacer(modifier = Modifier.height(Dimensions.Dp4))

            // 图标
            Icon(
                imageVector = categoryIcon(category),
                contentDescription = category.displayName,
                tint = categoryColor,
                modifier = Modifier.height(Dimensions.Dp24),
            )

            Spacer(modifier = Modifier.height(Dimensions.Dp8))

            // 分类名称
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(Dimensions.Dp4))

            // 元素数量
            Text(
                text = "$elementCount 个元素",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * 分类详情列表内容。
 *
 * TopAppBar（分类名 + 返回按钮）+ LazyColumn（ElementCard 列表）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDetailContent(
    category: Category,
    elements: List<Element>,
    zhMap: Map<Int, String>,
    onNavigateBack: () -> Unit,
    onElementClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.detail_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        modifier = modifier,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                horizontal = Dimensions.Dp16,
                vertical = Dimensions.Dp8,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimensions.Dp8),
        ) {
            items(elements, key = { it.atomicNumber }) { element ->
                ElementCard(
                    element = element,
                    onClick = { onElementClick(element.atomicNumber) },
                    zhName = zhMap[element.atomicNumber],
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/**
 * 分类对应图标。
 *
 * 为每个分类分配一个 Material Icon 用于 Grid 卡片展示。
 */
private fun categoryIcon(category: Category): ImageVector = when (category) {
    Category.ALKALI_METAL -> Icons.Filled.Bolt
    Category.ALKALINE_EARTH -> Icons.Filled.Landscape
    Category.TRANSITION_METAL -> Icons.Filled.PropaneTank
    Category.POST_TRANSITION -> Icons.Filled.BubbleChart
    Category.METALLOID -> Icons.Filled.Science
    Category.NONMETAL -> Icons.Filled.Opacity
    Category.HALOGEN -> Icons.Filled.Radar
    Category.NOBLE_GAS -> Icons.Filled.WbSunny
    Category.LANTHANIDE -> Icons.Filled.Flare
    Category.ACTINIDE -> Icons.Filled.Radar
}

// ===== Preview Data =====

private val previewHydrogen = Element(
    atomicNumber = 1,
    symbol = "H",
    name = "Hydrogen",
    category = Category.NONMETAL,
)
private val previewHelium = Element(
    atomicNumber = 2,
    symbol = "He",
    name = "Helium",
    category = Category.NOBLE_GAS,
)
private val previewLithium = Element(
    atomicNumber = 3,
    symbol = "Li",
    name = "Lithium",
    category = Category.ALKALI_METAL,
)

private val previewElementCounts: Map<Category, Int> = Category.entries.associateWith { 0 } + mapOf(
    Category.NONMETAL to 7,
    Category.NOBLE_GAS to 6,
    Category.ALKALI_METAL to 6,
    Category.ALKALINE_EARTH to 6,
    Category.TRANSITION_METAL to 38,
    Category.POST_TRANSITION to 7,
    Category.METALLOID to 6,
    Category.HALOGEN to 6,
    Category.LANTHANIDE to 14,
    Category.ACTINIDE to 14,
)

// ===== Previews =====

@Preview(name = "Light - Entrance Grid", showBackground = true)
@Preview(name = "Dark - Entrance Grid", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CategoryEntranceGridPreview() {
    PeriodicProTheme {
        CategoryEntranceContent(
            categories = Category.entries,
            elementCounts = previewElementCounts,
            onCategoryClick = {},
        )
    }
}

@Preview(name = "Light - Detail List", showBackground = true)
@Preview(name = "Dark - Detail List", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDetailContentPreview() {
    PeriodicProTheme {
        CategoryDetailContent(
            category = Category.NONMETAL,
            elements = listOf(previewHydrogen, previewHelium, previewLithium),
            zhMap = mapOf(1 to "氢", 2 to "氦", 3 to "锂"),
            onNavigateBack = {},
            onElementClick = {},
        )
    }
}
