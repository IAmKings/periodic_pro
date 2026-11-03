@file:OptIn(ExperimentalMaterial3Api::class)

package com.periodic.pro.feature.learn

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.periodic.pro.R
import com.periodic.pro.data.learn.model.LearnItem
import com.periodic.pro.data.element.model.Category
import com.periodic.pro.theme.Dimensions
import com.periodic.pro.theme.LocalCategoryColors
import com.periodic.pro.theme.LocalSemanticColors
import com.periodic.pro.theme.PeriodicProTheme
import com.periodic.pro.theme.Semantic
import com.periodic.pro.theme.forCategory
import com.periodic.pro.theme.pressScale
import com.periodic.pro.ui.components.PeriodicButton
import com.periodic.pro.ui.components.PeriodicTopBar
import com.periodic.pro.ui.components.SectionHeader
import com.periodic.pro.ui.components.TextBadge
import org.koin.androidx.compose.koinViewModel

/**
 * Learn 屏 — Spectra（DESIGN.md §9.7）。
 * 等级分组列表（语义色 badge）+ 学习详情（分组卡）。
 */
@Composable
fun LearnScreen(
    onNavigateToDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
    initialAtomicNumber: Int = 0,
    onNavigateBack: () -> Unit = {},
    vm: LearnViewModel = koinViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(initialAtomicNumber) {
        if (initialAtomicNumber > 0) {
            vm.handle(LearnIntent.SelectElement(initialAtomicNumber))
        }
    }

    LaunchedEffect(Unit) {
        vm.effect.collect { effect ->
            when (effect) {
                is LearnEffect.NavigateToDetail -> onNavigateToDetail(effect.atomicNumber)
            }
        }
    }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = state.listScrollIndex,
        initialFirstVisibleItemScrollOffset = state.listScrollOffset,
    )

    when (state.navMode) {
        LearnNavMode.LIST -> LearnListContent(
            state = state,
            listState = listState,
            onIntent = vm::handle,
            onBack = onNavigateBack,
            modifier = modifier,
        )
        LearnNavMode.DETAIL -> LearnDetailContent(
            item = state.selectedItem,
            symbol = state.selectedItem?.atomicNumber?.let { state.symbolMap[it] } ?: "?",
            onBack = {
                if (initialAtomicNumber > 0) onNavigateBack()
                else vm.handle(LearnIntent.BackToList)
            },
            onNavigateToDetail = { atomicNumber ->
                vm.handle(LearnIntent.NavigateToDetail(atomicNumber))
            },
            modifier = modifier,
        )
    }
}

// ===== List view =====

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LearnListContent(
    state: LearnUiState,
    listState: LazyListState,
    onIntent: (LearnIntent) -> Unit,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            PeriodicTopBar(
                title = stringResource(R.string.screen_learn),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.detail_back),
                        )
                    }
                },
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
            val hasCore = state.coreItems.isNotEmpty()
            val hasImportant = state.importantItems.isNotEmpty()
            val hasOther = state.otherItems.isNotEmpty()

            val categoryMap = state.categoryMap
            val scope = rememberCoroutineScope()
            // LazyColumn索引：0=jump buttons, 1=core header, ...
            val coreSectionItems = if (hasCore) state.coreItems.size + 1 else 0
            val importantSectionIndex = 1 + coreSectionItems
            val otherSectionIndex = importantSectionIndex + (if (hasImportant) state.importantItems.size + 1 else 0)

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    horizontal = Dimensions.Dp16,
                    vertical = Dimensions.Dp8,
                ),
                verticalArrangement = Arrangement.spacedBy(Dimensions.Dp8),
            ) {
                // 快速跳转 chips（stickyHeader 吸顶）
                stickyHeader(key = "jump_buttons") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(vertical = Dimensions.Dp4),
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.Dp8),
                    ) {
                        JumpChip(
                            label = stringResource(R.string.learn_level_core),
                            tint = Semantic.colors.levelCore,
                            container = Semantic.colors.levelCoreContainer,
                            enabled = true,
                            onClick = { scope.launch { listState.scrollToItem(1) } },
                            modifier = Modifier.weight(1f),
                        )
                        JumpChip(
                            label = stringResource(R.string.learn_level_important),
                            tint = Semantic.colors.levelImportant,
                            container = Semantic.colors.levelImportantContainer,
                            enabled = hasImportant,
                            onClick = { scope.launch { listState.scrollToItem(importantSectionIndex) } },
                            modifier = Modifier.weight(1f),
                        )
                        JumpChip(
                            label = stringResource(R.string.learn_level_normal),
                            tint = Semantic.colors.levelNormal,
                            container = Semantic.colors.levelNormalContainer,
                            enabled = hasOther,
                            onClick = { scope.launch { listState.scrollToItem(otherSectionIndex) } },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                // Core section
                if (hasCore) {
                    item(key = "section_core") {
                        LevelSectionHeader(
                            title = stringResource(R.string.learn_level_core),
                            badgeColor = Semantic.colors.levelCore,
                        )
                    }
                    items(state.coreItems, key = { "core_${it.atomicNumber}" }) { item ->
                        LearnListItem(
                            item = item,
                            symbol = state.symbolMap[item.atomicNumber] ?: "?",
                            categoryColor = LocalCategoryColors.current.forCategory(
                                categoryMap[item.atomicNumber] ?: Category.TRANSITION_METAL
                            ),
                            onClick = {
                                onIntent(LearnIntent.SaveScroll(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset))
                                onIntent(LearnIntent.SelectElement(item.atomicNumber))
                            },
                        )
                    }
                }

                // Important section
                if (hasImportant) {
                    item(key = "section_important") {
                        LevelSectionHeader(
                            title = stringResource(R.string.learn_level_important),
                            badgeColor = Semantic.colors.levelImportant,
                        )
                    }
                    items(
                        state.importantItems,
                        key = { "important_${it.atomicNumber}" },
                    ) { item ->
                        LearnListItem(
                            item = item,
                            symbol = state.symbolMap[item.atomicNumber] ?: "?",
                            categoryColor = LocalCategoryColors.current.forCategory(
                                categoryMap[item.atomicNumber] ?: Category.TRANSITION_METAL
                            ),
                            onClick = {
                                onIntent(LearnIntent.SaveScroll(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset))
                                onIntent(LearnIntent.SelectElement(item.atomicNumber))
                            },
                        )
                    }
                }

                // Other section
                if (hasOther) {
                    item(key = "section_other") {
                        LevelSectionHeader(
                            title = stringResource(R.string.learn_level_normal),
                            badgeColor = Semantic.colors.levelNormal,
                        )
                    }
                    items(state.otherItems, key = { "other_${it.atomicNumber}" }) { item ->
                        LearnListItem(
                            item = item,
                            symbol = state.symbolMap[item.atomicNumber] ?: "?",
                            categoryColor = LocalCategoryColors.current.forCategory(
                                categoryMap[item.atomicNumber] ?: Category.TRANSITION_METAL
                            ),
                            onClick = {
                                onIntent(LearnIntent.SaveScroll(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset))
                                onIntent(LearnIntent.SelectElement(item.atomicNumber))
                            },
                        )
                    }
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(Dimensions.Dp16))
                }
            }
        }
    }
}

/**
 * 等级跳转 Chip（语义色 tonal）。
 */
@Composable
private fun JumpChip(
    label: String,
    tint: Color,
    container: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.small
    Row(
        modifier = modifier
            .clip(shape)
            .background(if (enabled) container else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = Dimensions.Dp8),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(if (enabled) tint else MaterialTheme.colorScheme.onSurfaceVariant, CircleShape),
        )
        Spacer(modifier = Modifier.width(Dimensions.Dp6))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) tint else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

/**
 * 等级分组的 Section 表头。
 */
@Composable
private fun LevelSectionHeader(
    title: String,
    badgeColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.Dp8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(badgeColor, RoundedCornerShape(4.dp)),
        )
        Spacer(modifier = Modifier.width(Dimensions.Dp8))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * 元素学习列表卡：TextBadge 符号 + 标题 + 等级 badge + 摘要。
 */
@Composable
private fun LearnListItem(
    item: LearnItem,
    symbol: String,
    categoryColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val semantic = LocalSemanticColors.current
    val (badgeColor, badgeBg, badgeText) = when (item.level) {
        "core" -> Triple(
            semantic.levelCore,
            semantic.levelCoreContainer,
            stringResource(R.string.learn_badge_core),
        )
        "important" -> Triple(
            semantic.levelImportant,
            semantic.levelImportantContainer,
            stringResource(R.string.learn_badge_important),
        )
        else -> Triple(
            semantic.levelNormal,
            semantic.levelNormalContainer,
            stringResource(R.string.learn_badge_normal),
        )
    }

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
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 元素符号圆标
            TextBadge(
                text = symbol,
                tint = categoryColor,
                size = Dimensions.Dp40,
            )

            Spacer(modifier = Modifier.width(Dimensions.Dp12))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(
                            R.string.learn_element_label,
                            item.atomicNumber,
                            symbol,
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(Dimensions.Dp8))
                    // Level badge
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = badgeBg,
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.Dp4))

                Text(
                    text = item.discoveryStory,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ===== Detail view =====

@Composable
private fun LearnDetailContent(
    item: LearnItem?,
    symbol: String,
    onBack: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (item == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.learn_detail_not_found),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        return
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            PeriodicTopBar(
                title = stringResource(R.string.learn_detail_title, item.atomicNumber, symbol),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.detail_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimensions.Dp16),
        ) {
            DiscoveryStorySection(story = item.discoveryStory)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ApplicationsSection(applications = item.applications)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            FunFactsSection(funFacts = item.funFacts)
            Spacer(modifier = Modifier.height(Dimensions.Dp8))
            PeriodicButton(
                onClick = { onNavigateToDetail(item.atomicNumber) },
                text = stringResource(R.string.learn_view_detail),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(Dimensions.Dp32))
        }
    }
}

@Composable
private fun DiscoveryStorySection(
    story: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.Dp16),
    ) {
        SectionHeader(title = stringResource(R.string.learn_section_discovery))
        Spacer(modifier = Modifier.height(Dimensions.Dp12))
        Text(
            text = story,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ApplicationsSection(
    applications: List<String>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.Dp16),
    ) {
        SectionHeader(title = stringResource(R.string.learn_section_applications))
        Spacer(modifier = Modifier.height(Dimensions.Dp12))
        applications.forEachIndexed { index, app ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimensions.Dp4),
            ) {
                Text(
                    text = "${index + 1}.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(Dimensions.Dp20),
                )
                Text(
                    text = app,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun FunFactsSection(
    funFacts: List<String>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.Dp16),
    ) {
        SectionHeader(title = stringResource(R.string.learn_section_funfacts))
        Spacer(modifier = Modifier.height(Dimensions.Dp12))
        funFacts.forEach { fact ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimensions.Dp4),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = "\uD83D\uDCA1",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(Dimensions.Dp20),
                )
                Spacer(modifier = Modifier.width(Dimensions.Dp4))
                Text(
                    text = fact,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

// ===== Preview =====

private val previewItems = listOf(
    LearnItem(
        atomicNumber = 1,
        level = "core",
        discoveryStory = "氢是宇宙中最丰富的元素，约占可见物质的 75%。1766 年，英国化学家亨利·卡文迪什通过金属与酸反应首次分离出氢气。",
        applications = listOf(
            "氢燃料电池：将氢与氧的化学能直接转化为电能，副产物只有水",
            "工业原料：哈伯法制氨消耗全球约一半的氢气产量",
        ),
        funFacts = listOf(
            "太阳每秒钟将约 6 亿吨氢聚变为氦",
            "地球上游离态氢很少，但水中氢原子数量是氧的两倍",
        ),
    ),
    LearnItem(
        atomicNumber = 6,
        level = "core",
        discoveryStory = "碳是已知最古老的元素之一，自古以来就以木炭、石墨和金刚石的形式为人类所知。",
        applications = listOf(
            "能源：煤、石油和天然气的主要成分是碳氢化合物",
            "材料科学：碳纤维强度是钢的 5 倍但重量仅为其 1/4",
        ),
        funFacts = listOf(
            "金刚石和石墨都由纯碳构成——同样的原子，不同的排列造就了最坚硬和最柔软的物质",
        ),
    ),
    LearnItem(
        atomicNumber = 26,
        level = "core",
        discoveryStory = "铁是人类使用历史最悠久的金属之一，铁器时代标志着人类文明的重大飞跃。",
        applications = listOf(
            "钢铁工业：全球每年生产约 19 亿吨钢铁",
            "生物功能：血红蛋白依靠铁离子运输氧气",
        ),
        funFacts = listOf(
            "地球核心由约 85% 的铁和 15% 的镍组成",
        ),
    ),
)

@Preview(name = "Light - List", showBackground = true)
@Preview(
    name = "Dark - List",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun LearnListPreview() {
    PeriodicProTheme {
        LearnListContent(
            state = LearnUiState(
                items = previewItems,
                isLoading = false,
            ),
            listState = rememberLazyListState(),
            onIntent = {},
        )
    }
}

@Preview(name = "Light - Detail", showBackground = true)
@Preview(
    name = "Dark - Detail",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun LearnDetailPreview() {
    val previewItem = LearnItem(
        atomicNumber = 1,
        level = "core",
        discoveryStory = "氢是宇宙中最丰富的元素，约占可见物质的 75%。" +
            "1766 年，英国化学家亨利·卡文迪什通过金属与酸反应首次分离出氢气，将其命名为「可燃空气」。1783 年，拉瓦锡确认它是一种独立元素。",
        applications = listOf(
            "氢燃料电池：将氢与氧的化学能直接转化为电能，副产物只有水",
            "工业原料：哈伯法制氨消耗全球约一半的氢气产量",
            "火箭燃料：液态氢是运载火箭的主要推进剂",
        ),
        funFacts = listOf(
            "太阳每秒钟将约 6 亿吨氢聚变为氦，释放的能量相当于数千亿颗氢弹同时爆炸",
            "地球上游离态氢很少，但水中氢原子数量是氧的两倍",
        ),
    )
    PeriodicProTheme {
        LearnDetailContent(
            item = previewItem,
            symbol = "H",
            onBack = {},
            onNavigateToDetail = {},
        )
    }
}
