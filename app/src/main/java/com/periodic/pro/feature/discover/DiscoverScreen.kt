package com.periodic.pro.feature.discover

import android.content.res.Configuration
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.periodic.pro.R
import com.periodic.pro.data.discover.model.DiscoverItem
import com.periodic.pro.theme.Dimensions
import com.periodic.pro.theme.PeriodicProTheme
import org.koin.androidx.compose.koinViewModel

/**
 * Discover 屏入口。
 *
 * 展示每日推荐 Hero 卡片 + Feed 流知识点列表。
 *
 * @param onNavigateToDetail 导航到元素详情
 * @param modifier Modifier
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
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.screen_discover),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
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
                onItemClick = { vm.handle(DiscoverIntent.SelectItem(it)) },
                modifier = Modifier.padding(padding),
            )
        }
    }
}

/**
 * Discover 屏纯 UI 内容。
 *
 * 顶部每日推荐 Hero 卡片 + 下方 Feed 流知识点列表。
 */
@Composable
private fun DiscoverContent(
    items: List<DiscoverItem>,
    dailyRecommend: DiscoverItem?,
    symbolMap: Map<Int, String>,
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
        // 每日推荐
        if (dailyRecommend != null) {
            item(key = "daily_recommend") {
                DailyRecommendSection(
                    item = dailyRecommend,
                    symbol = symbolMap[dailyRecommend.atomicNumber] ?: "?",
                    onClick = { onItemClick(dailyRecommend.atomicNumber) },
                )
            }
        }

        // Feed 列表标题
        item(key = "feed_title") {
            Text(
                text = stringResource(R.string.discover_feed_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = Dimensions.Dp8, bottom = Dimensions.Dp4),
            )
        }

        // 知识点卡片列表
        items(items, key = { it.atomicNumber.toString() + it.title }) { item ->
            DiscoverFeedCard(
                item = item,
                symbol = symbolMap[item.atomicNumber] ?: "?",
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
 * 每日推荐 Hero 卡片。
 *
 * 大尺寸卡片，顶部彩色色条 + 元素符号大标记 + 标题 + 描述。
 */
@Composable
private fun DailyRecommendSection(
    item: DiscoverItem,
    symbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 默认使用过渡金属色，因为可能不知道元素分类
    val elementColor = Color(0xFF4DABF7)

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column {
            // 彩色色条
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(elementColor),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.Dp16),
            ) {
                Column {
                    // 标签
                    Text(
                        text = stringResource(R.string.discover_daily_recommend),
                        style = MaterialTheme.typography.labelMedium,
                        color = elementColor,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(Dimensions.Dp8))

                    // 元素符号大标
                    Text(
                        text = symbol,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = elementColor,
                    )

                    Spacer(modifier = Modifier.height(Dimensions.Dp4))

                    // 标题
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.height(Dimensions.Dp8))

                    // 描述
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
    }
}

/**
 * Feed 流知识点卡片。
 *
 * 较小尺寸的卡片，左侧元素符号圆形标记 + 标题 + 描述摘要。
 */
@Composable
private fun DiscoverFeedCard(
    item: DiscoverItem,
    symbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.Dp12),
            verticalAlignment = Alignment.Top,
        ) {
            // 元素序号圆形标记
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = Color(0xFF4DABF7).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(22.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = item.atomicNumber.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4DABF7),
                )
            }

            Spacer(modifier = Modifier.width(Dimensions.Dp12))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
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
            onItemClick = {},
        )
    }
}
