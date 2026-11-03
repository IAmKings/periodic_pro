package com.periodic.pro.feature.discover

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.periodic.pro.R
import com.periodic.pro.data.discover.model.DiscoverItem
import com.periodic.pro.data.element.model.Category
import com.periodic.pro.theme.CategoryAlpha
import com.periodic.pro.theme.Dimensions
import com.periodic.pro.theme.LocalCategoryColors
import com.periodic.pro.theme.PeriodicProTheme
import com.periodic.pro.theme.forCategory
import com.periodic.pro.theme.pressScale
import com.periodic.pro.ui.components.PeriodicTopBar
import com.periodic.pro.ui.components.SectionHeader
import com.periodic.pro.ui.components.TextBadge
import org.koin.androidx.compose.koinViewModel

/**
 * Discover 屏 — Spectra（DESIGN.md §9.4）。
 * 每日推荐 Hero（分类色渐变）+ Feed 流知识点卡片。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    onNavigateToDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
    vm: DiscoverViewModel = koinViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.effect.collect { effect ->
            when (effect) {
                is DiscoverEffect.NavigateToDetail -> onNavigateToDetail(effect.atomicNumber)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            PeriodicTopBar(title = stringResource(R.string.screen_discover))
        },
    ) { padding ->
        if (state.isLoading) {
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
        } else if (state.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = state.errorMessage ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        } else {
            DiscoverContent(
                items = state.items,
                dailyRecommend = state.dailyRecommend,
                symbolMap = state.symbolMap,
                categoryMap = state.categoryMap,
                onItemClick = { vm.handle(DiscoverIntent.SelectItem(it)) },
                modifier = Modifier.padding(padding),
            )
        }
    }
}

/**
 * Discover 屏纯 UI 内容：每日推荐 Hero + Feed 流。
 */
@Composable
private fun DiscoverContent(
    items: List<DiscoverItem>,
    dailyRecommend: DiscoverItem?,
    symbolMap: Map<Int, String>,
    categoryMap: Map<Int, Category>,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = Dimensions.Dp16,
            vertical = Dimensions.Dp8,
        ),
        verticalArrangement = Arrangement.spacedBy(Dimensions.Dp12),
    ) {
        // 每日推荐 Hero
        if (dailyRecommend != null) {
            item(key = "daily_recommend") {
                DailyRecommendSection(
                    item = dailyRecommend,
                    symbol = symbolMap[dailyRecommend.atomicNumber] ?: "?",
                    categoryColor = LocalCategoryColors.current.forCategory(
                        categoryMap[dailyRecommend.atomicNumber] ?: Category.TRANSITION_METAL
                    ),
                    onClick = { onItemClick(dailyRecommend.atomicNumber) },
                )
            }
        }

        // Feed 列表标题
        item(key = "feed_title") {
            SectionHeader(
                title = stringResource(R.string.discover_feed_title),
                modifier = Modifier.padding(top = Dimensions.Dp8),
            )
        }

        // 知识点卡片列表
        items(items, key = { it.atomicNumber.toString() + it.title }) { item ->
            DiscoverFeedCard(
                item = item,
                symbol = symbolMap[item.atomicNumber] ?: "?",
                categoryColor = LocalCategoryColors.current.forCategory(
                    categoryMap[item.atomicNumber] ?: Category.TRANSITION_METAL
                ),
                onClick = { onItemClick(item.atomicNumber) },
            )
        }

        // 底部留白
        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(Dimensions.Dp16))
        }
    }
}

/**
 * 每日推荐 Hero：分类色渐变底 + 大符号 + 标题 + 摘要 + 查看箭头。
 */
@Composable
private fun DailyRecommendSection(
    item: DiscoverItem,
    symbol: String,
    categoryColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                ),
            )
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .pressScale(interaction)
            .clickable(
                interactionSource = interaction,
                indication = ripple(),
                onClick = onClick,
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.Dp20),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.discover_daily_recommend),
                    style = MaterialTheme.typography.labelMedium,
                    color = categoryColor,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    tint = categoryColor,
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.Dp12))

            // 元素符号大标 + 标题
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = symbol,
                    style = MaterialTheme.typography.headlineMedium,
                    color = categoryColor,
                )
                Spacer(modifier = Modifier.width(Dimensions.Dp12))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.Dp8))

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Feed 卡：TextBadge（元素序号）+ 标题 + 2 行摘要。
 */
@Composable
private fun DiscoverFeedCard(
    item: DiscoverItem,
    symbol: String,
    categoryColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.medium
    val interaction = remember { MutableInteractionSource() }

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .pressScale(interaction),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = Dimensions.Dp0,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        interactionSource = interaction,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.Dp12),
            verticalAlignment = Alignment.Top,
        ) {
            TextBadge(
                text = item.atomicNumber.toString(),
                tint = categoryColor,
                size = Dimensions.Dp40,
            )

            Spacer(modifier = Modifier.width(Dimensions.Dp12))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(Dimensions.Dp4))

                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ===== Preview Data =====

private val previewDiscoverItems = listOf(
    DiscoverItem(
        atomicNumber = 1,
        title = "宇宙中最丰富的元素",
        description = "氢约占宇宙可见质量的 75%，是恒星核聚变的主要燃料。",
        funFact = "地球上游离态氢很少，但水中氢原子数量是氧的两倍。",
    ),
    DiscoverItem(
        atomicNumber = 6,
        title = "生命的基础",
        description = "碳是地球上所有已知生命的基础元素。它能形成稳定的长链结构。",
        funFact = "金刚石和石墨都由纯碳构成，不同的排列方式造就了最坚硬和最柔软的物质。",
    ),
    DiscoverItem(
        atomicNumber = 79,
        title = "跨越文明的财富",
        description = "金是唯一呈现天然黄色的金属，极耐腐蚀。人类已开采约 20 万吨。",
    ),
)

private val previewDaily = DiscoverItem(
    atomicNumber = 3,
    title = "最轻的金属",
    description = "锂是密度最小的金属元素，密度仅为 0.534 g/cm³。",
    funFact = "锂离子电池让现代手机、笔记本和电动车成为可能。",
)

// ===== Previews =====

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DiscoverContentPreview() {
    PeriodicProTheme {
        DiscoverContent(
            items = previewDiscoverItems,
            dailyRecommend = previewDaily,
            symbolMap = emptyMap(),
            categoryMap = emptyMap(),
            onItemClick = {},
        )
    }
}
