package com.periodic.pro.feature.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CompareArrows
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.periodic.pro.R
import com.periodic.pro.data.element.model.Category
import com.periodic.pro.data.element.model.Element
import com.periodic.pro.theme.CategoryAlpha
import com.periodic.pro.theme.Dimensions
import com.periodic.pro.theme.LocalCategoryColors
import com.periodic.pro.theme.PeriodicProTheme
import com.periodic.pro.theme.forCategory
import com.periodic.pro.theme.pressScale
import com.periodic.pro.ui.components.ElementTile
import com.periodic.pro.ui.components.IconBadge
import com.periodic.pro.ui.components.PeriodicSearchBar
import com.periodic.pro.ui.components.SectionHeader
import com.periodic.pro.ui.components.elementTileRowSize
import org.koin.androidx.compose.koinViewModel

/**
 * Home 屏入口 — Spectra「仪表盘」布局（DESIGN.md §9.1）。
 */
@Composable
fun HomeScreen(
    onNavigateToTable: (String) -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToCompare: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToLearn: () -> Unit = {},
    onNavigateToLab: () -> Unit = {},
    onNavigateToQuiz: () -> Unit = {},
    onNavigateToCategory: (String) -> Unit = {},
    vm: HomeViewModel = koinViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()

    HomeContent(
        state = state,
        onSearchSubmit = { query -> onNavigateToTable(query) },
        onElementClick = { atomicNumber -> onNavigateToDetail(atomicNumber) },
        onNavigateToTable = { onNavigateToTable("") },
        onNavigateToCompare = onNavigateToCompare,
        onNavigateToLearn = onNavigateToLearn,
        onNavigateToLab = onNavigateToLab,
        onNavigateToQuiz = onNavigateToQuiz,
        onNavigateToFavorites = onNavigateToFavorites,
        onNavigateToCategory = onNavigateToCategory,
        modifier = modifier,
    )
}

/**
 * Home 屏纯 UI 内容。
 */
@Composable
private fun HomeContent(
    state: HomeUiState,
    onSearchSubmit: (String) -> Unit,
    onElementClick: (Int) -> Unit,
    onNavigateToTable: () -> Unit,
    onNavigateToCompare: () -> Unit,
    onNavigateToLearn: () -> Unit,
    onNavigateToLab: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToCategory: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // === 1. 欢迎区 ===
        Spacer(
            modifier = Modifier.height(
                WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + Dimensions.Dp16,
            ),
        )
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = Dimensions.Dp16),
        )
        Text(
            text = stringResource(R.string.home_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                start = Dimensions.Dp16,
                end = Dimensions.Dp16,
                top = Dimensions.Dp4,
            ),
        )

        Spacer(modifier = Modifier.height(Dimensions.Dp20))

        // === 2. 搜索框 ===
        PeriodicSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.Dp16),
            placeholder = stringResource(R.string.search_placeholder),
            onSubmit = { onSearchSubmit(searchQuery) },
        )

        Spacer(modifier = Modifier.height(Dimensions.Dp24))

        if (state.isReady && state.featuredElements.isNotEmpty()) {
            // === 3. 今日聚焦 Hero（featured 首个元素） ===
            val hero = state.featuredElements.first()
            HomeHeroCard(
                element = hero,
                zhName = state.zhMap[hero.atomicNumber],
                onClick = { onElementClick(hero.atomicNumber) },
                modifier = Modifier.padding(horizontal = Dimensions.Dp16),
            )

            Spacer(modifier = Modifier.height(Dimensions.Dp24))

            // === 4. 精选元素横滑 ===
            if (state.featuredElements.size > 1) {
                SectionHeader(
                    title = stringResource(R.string.home_featured_section),
                    modifier = Modifier.padding(horizontal = Dimensions.Dp16),
                )
                Spacer(modifier = Modifier.height(Dimensions.Dp12))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = Dimensions.Dp16,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.Dp12),
                ) {
                    items(state.featuredElements.drop(1), key = { it.atomicNumber }) { element ->
                        ElementTile(
                            element = element,
                            onClick = { onElementClick(element.atomicNumber) },
                            zhName = state.zhMap[element.atomicNumber],
                            modifier = Modifier.elementTileRowSize(),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.Dp24))
            }
        }

        // === 5. 快捷入口 2×2 ===
        SectionHeader(
            title = stringResource(R.string.home_dashboard_section),
            modifier = Modifier.padding(horizontal = Dimensions.Dp16),
        )
        Spacer(modifier = Modifier.height(Dimensions.Dp12))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.Dp16),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Dp12),
        ) {
            DashboardCard(
                icon = Icons.Filled.School,
                title = stringResource(R.string.home_dashboard_learn),
                onClick = onNavigateToLearn,
                modifier = Modifier.weight(1f),
            )
            DashboardCard(
                icon = Icons.Filled.Psychology,
                title = stringResource(R.string.home_dashboard_quiz),
                onClick = onNavigateToQuiz,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.Dp12))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.Dp16),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Dp12),
        ) {
            DashboardCard(
                icon = Icons.Filled.Science,
                title = stringResource(R.string.home_dashboard_lab),
                onClick = onNavigateToLab,
                modifier = Modifier.weight(1f),
            )
            DashboardCard(
                icon = Icons.AutoMirrored.Outlined.CompareArrows,
                title = stringResource(R.string.home_dashboard_compare),
                onClick = onNavigateToCompare,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.Dp24))

        // === 6. 元素分类图例（填补页尾节奏，双列可点） ===
        SectionHeader(
            title = stringResource(R.string.home_category_section),
            actionLabel = stringResource(R.string.tab_table),
            onAction = onNavigateToTable,
            modifier = Modifier.padding(horizontal = Dimensions.Dp16),
        )
        Spacer(modifier = Modifier.height(Dimensions.Dp12))
        CategoryLegendGrid(
            onCategoryClick = onNavigateToCategory,
            modifier = Modifier.padding(horizontal = Dimensions.Dp16),
        )

        Spacer(modifier = Modifier.height(Dimensions.Dp32))
    }
}

/**
 * 今日聚焦 Hero 卡 — 分类色 18%→透明渐变 + 大符号 + 描述 + 进入箭头。
 */
@Composable
private fun HomeHeroCard(
    element: Element,
    zhName: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categoryColor = LocalCategoryColors.current.forCategory(element.category)
    val shape = MaterialTheme.shapes.extraLarge
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        categoryColor.copy(alpha = CategoryAlpha.HERO),
                        Color.Transparent,
                    ),
                    start = androidx.compose.ui.geometry.Offset(x = 0f, y = 0f),
                    end = androidx.compose.ui.geometry.Offset(x = 800f, y = 500f),
                ),
            )
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .pressScale(interaction)
            .clickable(interactionSource = interaction, indication = ripple(), onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.Dp20),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_hero_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = categoryColor,
                )
                Spacer(modifier = Modifier.height(Dimensions.Dp8))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = element.symbol,
                        style = MaterialTheme.typography.headlineMedium,
                        color = categoryColor,
                    )
                    Spacer(modifier = Modifier.width(Dimensions.Dp12))
                    Column(modifier = Modifier.padding(bottom = 3.dp)) {
                        Text(
                            text = listOfNotNull(zhName, element.name).joinToString(" · "),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = element.category.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = null,
                tint = categoryColor,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * Dashboard 快捷入口卡片 — IconBadge + 标题 + 发丝描边。
 */
@Composable
private fun DashboardCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.medium
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .height(88.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .pressScale(interaction)
            .clickable(interactionSource = interaction, indication = ripple(), onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = Dimensions.Dp16),
        ) {
            IconBadge(
                icon = icon,
                tint = MaterialTheme.colorScheme.primary,
                size = Dimensions.Dp40,
            )
            Spacer(modifier = Modifier.width(Dimensions.Dp12))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        }
    }
}

/**
 * 元素分类图例（双列，可点击跳转分类详情）。
 */
@Composable
private fun CategoryLegendGrid(
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val categoryColors = LocalCategoryColors.current
    Column(modifier = modifier.fillMaxWidth()) {
        legendCategories.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.Dp12),
            ) {
                row.forEach { (category, labelRes) ->
                    val color = categoryColors.forCategory(category)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
                            .clickable { onCategoryClick(category.id) }
                            .padding(horizontal = Dimensions.Dp12, vertical = Dimensions.Dp12),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(color, CircleShape),
                            )
                            Spacer(modifier = Modifier.width(Dimensions.Dp8))
                            Text(
                                text = stringResource(labelRes),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                            )
                        }
                    }
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(Dimensions.Dp12))
        }
    }
}

/** 图例分类（与周期表筛选一致） */
private val legendCategories = listOf(
    Category.ALKALI_METAL to R.string.category_alkali_metal,
    Category.ALKALINE_EARTH to R.string.category_alkaline_earth_metal,
    Category.TRANSITION_METAL to R.string.category_transition_metal,
    Category.POST_TRANSITION to R.string.category_post_transition_metal,
    Category.METALLOID to R.string.category_metalloid,
    Category.NONMETAL to R.string.category_nonmetal,
    Category.HALOGEN to R.string.category_halogen,
    Category.NOBLE_GAS to R.string.category_noble_gas,
    Category.LANTHANIDE to R.string.category_lanthanide,
    Category.ACTINIDE to R.string.category_actinide,
)

// ===== Preview Data =====

private val previewElements = listOf(
    Element(atomicNumber = 1, symbol = "H", name = "Hydrogen", category = Category.NONMETAL),
    Element(atomicNumber = 2, symbol = "He", name = "Helium", category = Category.NOBLE_GAS),
    Element(atomicNumber = 6, symbol = "C", name = "Carbon", category = Category.NONMETAL),
    Element(atomicNumber = 8, symbol = "O", name = "Oxygen", category = Category.NONMETAL),
    Element(atomicNumber = 26, symbol = "Fe", name = "Iron", category = Category.TRANSITION_METAL),
    Element(atomicNumber = 29, symbol = "Cu", name = "Copper", category = Category.TRANSITION_METAL),
    Element(atomicNumber = 79, symbol = "Au", name = "Gold", category = Category.TRANSITION_METAL),
    Element(atomicNumber = 80, symbol = "Hg", name = "Mercury", category = Category.TRANSITION_METAL),
)

// ===== Previews =====

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeContentPreview() {
    PeriodicProTheme {
        HomeContent(
            state = HomeUiState(
                featuredElements = previewElements,
                zhMap = mapOf(
                    1 to "氢", 2 to "氦", 6 to "碳", 8 to "氧",
                    26 to "铁", 29 to "铜", 79 to "金", 80 to "汞",
                ),
                isLoading = false,
            ),
            onSearchSubmit = {},
            onElementClick = {},
            onNavigateToTable = {},
            onNavigateToCompare = {},
            onNavigateToLearn = {},
            onNavigateToLab = {},
            onNavigateToQuiz = {},
            onNavigateToFavorites = {},
            onNavigateToCategory = {},
        )
    }
}
