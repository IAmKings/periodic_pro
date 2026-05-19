package com.periodic.pro.feature.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.automirrored.outlined.CompareArrows
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.periodic.pro.theme.PeriodicProTheme
import com.periodic.pro.ui.components.ElementCard
import com.periodic.pro.ui.components.PeriodicSearchBar
import org.koin.androidx.compose.koinViewModel

/**
 * Home 屏入口。
 *
 * @param onNavigateToTable 导航到周期表（可选搜索 query）
 * @param onNavigateToDetail 导航到元素详情
 * @param onNavigateToCompare 导航到对比屏
 * @param onNavigateToFavorites 导航到收藏屏
 * @param modifier Modifier
 */
@Composable
fun HomeScreen(
    onNavigateToTable: (String) -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToCompare: () -> Unit,
    onNavigateToLearn: () -> Unit = {},
    onNavigateToLab: () -> Unit = {},
    onNavigateToQuiz: () -> Unit = {},
    onNavigateToFavorites: () -> Unit,
    modifier: Modifier = Modifier,
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
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // === 1. 欢迎区 ===
        Spacer(modifier = Modifier.height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
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

        Spacer(modifier = Modifier.height(Dimensions.Dp24))

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

        // === 3. 推荐元素 ===
        Text(
            text = stringResource(R.string.home_featured_section),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = Dimensions.Dp16),
        )

        Spacer(modifier = Modifier.height(Dimensions.Dp12))

        // 横向推荐元素列表
        if (state.isReady && state.featuredElements.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimensions.Dp16),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.Dp12),
            ) {
                items(state.featuredElements, key = { it.atomicNumber }) { element ->
                    ElementCard(
                        element = element,
                        onClick = { onElementClick(element.atomicNumber) },
                        zhName = state.zhMap[element.atomicNumber],
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimensions.Dp24))

        // === 4. 快捷入口 Dashboard 2x2 ===
        Text(
            text = stringResource(R.string.home_dashboard_section),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = Dimensions.Dp16),
        )

        Spacer(modifier = Modifier.height(Dimensions.Dp12))

        // Row 1
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.Dp16),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Dp12),
        ) {
            DashboardCard(
                icon = Icons.Filled.School,
                title = "学习",
                onClick = onNavigateToLearn,
                modifier = Modifier.weight(1f),
            )
            DashboardCard(
                icon = Icons.Filled.Psychology,
                title = "测试",
                onClick = onNavigateToQuiz,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.Dp12))

        // Row 2
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.Dp16),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Dp12),
        ) {
            DashboardCard(
                icon = Icons.Filled.Science,
                title = "实验室",
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

        Spacer(modifier = Modifier.height(Dimensions.Dp32))
    }
}

/**
 * Dashboard 快捷入口卡片。
 */
@Composable
private fun DashboardCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(Dimensions.Dp16),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.Shadow2),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        enabled = enabled,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimensions.Dp16),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(Dimensions.Dp32),
                tint = if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                },
            )
            Spacer(modifier = Modifier.height(Dimensions.Dp8))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                },
            )
        }
    }
}

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
        )
    }
}
