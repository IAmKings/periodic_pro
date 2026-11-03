package com.periodic.pro.feature.table

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.periodic.pro.data.element.model.Category
import com.periodic.pro.data.element.model.Element
import com.periodic.pro.data.element.model.ElementZh
import com.periodic.pro.theme.CategoryAlpha
import com.periodic.pro.theme.LocalCategoryColors
import com.periodic.pro.theme.forCategory

/**
 * 周期表 18×10 网格 — Spectra 单元格规范（DESIGN.md §8.1）。
 *
 * 水平可滚动；单元格 6dp 圆角 + 分类色 tint 底；
 * 筛选淘汰 0.12 alpha；选中 2dp 分类色描边 + tint 加深。
 */
@Composable
fun PeriodicTableGrid(
    elements: List<Element>,
    zhMap: Map<Int, ElementZh>,
    selectedCategory: Category?,
    searchQuery: String,
    isMultiSelectMode: Boolean,
    selectedIds: Set<Int>,
    onElementClick: (Int) -> Unit,
    onElementLongClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onSelectSeries: (List<Int>) -> Unit = {},
) {
    val query = searchQuery.trim().lowercase()
    val matchedIds = remember(elements, zhMap, query) {
        if (query.isEmpty()) null
        else elements.filter { element ->
            element.symbol.lowercase().contains(query) ||
                element.name.lowercase().contains(query) ||
                (query.toIntOrNull() != null && element.atomicNumber.toString().contains(query)) ||
                zhMap[element.atomicNumber]?.nameZh?.contains(query) == true ||
                zhMap[element.atomicNumber]?.pinyin?.let { stripTones(it).contains(query) } == true
        }.map { it.atomicNumber }.toSet()
    }

    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val minCellPx = with(density) { 48.dp.toPx() }
        val maxCellPx = with(density) { 72.dp.toPx() }
        val cellPx = with(density) { maxWidth.toPx() / 18f }
        val clampedCellPx = cellPx.coerceIn(minCellPx, maxCellPx)
        val cellDp = with(density) { clampedCellPx.toDp() }
        val cellPxInt = clampedCellPx.toInt()

        // 镧系/锕系间隔
        val gapPx = with(density) { 8.dp.toPx() }.toInt()

        // 行标签列宽 / 列号行高
        val rowLabelWidthDp = with(density) { 24.dp }
        val headerHeightDp = with(density) { 20.dp }
        val headerHeightPx = with(density) { headerHeightDp.toPx().toInt() }

        // 计算 y 偏移（含间隔）：镧系前 gapPx*2
        fun yOffset(row: Int): Int {
            var y = headerHeightPx + row * cellPxInt
            if (row >= 7) y += gapPx * 2
            return y
        }

        val contentWidthDp = with(density) { (18 * clampedCellPx).toDp() }
        val lastRow = 8
        val totalHeightPx = yOffset(lastRow) + cellPxInt
        val totalHeightDp = with(density) { totalHeightPx.toDp() }

        val hScrollState = rememberScrollState()
        val vScrollState = rememberScrollState()
        val gapDp = with(density) { 8.dp }
        val rowLabels = listOf("1", "2", "3", "4", "5", "6", "7", "La", "Ac")

        val fBlockMarkerRow6 = Pair(5, 2)
        val fBlockMarkerRow7 = Pair(6, 2)

        Column(modifier = Modifier.fillMaxSize().verticalScroll(vScrollState)) {
            // === 顶部列号行 ===
            Row(modifier = Modifier.height(headerHeightDp)) {
                Spacer(modifier = Modifier.width(rowLabelWidthDp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(hScrollState),
                ) {
                    Row(
                        modifier = Modifier.requiredSize(width = contentWidthDp, height = headerHeightDp),
                    ) {
                        for (col in 1..18) {
                            Box(
                                modifier = Modifier.size(width = cellDp, height = headerHeightDp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "$col",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }

            // === 主体：行号 + 元素区 ===
            Row {
                // 左侧行号列（固定）
                Column(modifier = Modifier.width(rowLabelWidthDp)) {
                    Spacer(modifier = Modifier.height(headerHeightDp))
                    for (i in 0..6) {
                        Box(
                            modifier = Modifier.size(width = rowLabelWidthDp, height = cellDp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                rowLabels[i],
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(width = rowLabelWidthDp, height = gapDp * 2))
                    Box(
                        modifier = Modifier.size(width = rowLabelWidthDp, height = cellDp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            rowLabels[7],
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Box(
                        modifier = Modifier.size(width = rowLabelWidthDp, height = cellDp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            rowLabels[8],
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // 右侧元素区（可水平滚动）
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(hScrollState),
                ) {
                    Box(
                        modifier = Modifier
                            .requiredSize(width = contentWidthDp, height = totalHeightDp),
                    ) {
                        elements.forEach { element ->
                            val (row, col) = getGridPosition(element)
                            val isSelected = element.atomicNumber in selectedIds
                            val isInSearch = matchedIds == null || element.atomicNumber in matchedIds

                            val alphaValue = when {
                                matchedIds != null && !isInSearch -> 0.12f
                                selectedCategory != null && element.category != selectedCategory -> 0.12f
                                isMultiSelectMode && !isSelected -> 0.4f
                                else -> 1.0f
                            }

                            PeriodicTableCell(
                                element = element,
                                zhName = zhMap[element.atomicNumber]?.nameZh,
                                isSelected = isSelected,
                                alpha = alphaValue,
                                onClick = { onElementClick(element.atomicNumber) },
                                onLongClick = { onElementLongClick(element.atomicNumber) },
                                modifier = Modifier
                                    .offset { IntOffset(col * cellPxInt, yOffset(row)) }
                                    .size(cellDp),
                            )
                        }

                        // F-block 标记
                        if (elements.none { it.atomicNumber in 57..71 }) {
                            FBlockMarker(
                                label = "57–71",
                                onClick = { onSelectSeries((57..71).toList()) },
                                modifier = Modifier
                                    .offset {
                                        IntOffset(fBlockMarkerRow6.second * cellPxInt, yOffset(fBlockMarkerRow6.first))
                                    }
                                    .size(cellDp),
                            )
                        }
                        if (elements.none { it.atomicNumber in 89..103 }) {
                            FBlockMarker(
                                label = "89–103",
                                onClick = { onSelectSeries((89..103).toList()) },
                                modifier = Modifier
                                    .offset {
                                        IntOffset(fBlockMarkerRow7.second * cellPxInt, yOffset(fBlockMarkerRow7.first))
                                    }
                                    .size(cellDp),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getGridPosition(element: Element): Pair<Int, Int> {
    return when (element.atomicNumber) {
        in 57..71 -> Pair(7, element.atomicNumber - 57 + 2)
        in 89..103 -> Pair(8, element.atomicNumber - 89 + 2)
        else -> Pair(
            (element.period ?: 1) - 1,
            (element.group ?: 1) - 1,
        )
    }
}

/** 去除拼音声调 + ü→v：tiě → tie, lǜ → lv */
private fun stripTones(pinyin: String): String =
    java.text.Normalizer.normalize(pinyin, java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        .replace('ü', 'v')
        .replace('\u01D8', 'v')  // ǖ
        .replace('\u01D9', 'v')  // ǘ
        .replace('\u01DA', 'v')  // ǚ
        .replace('\u01DB', 'v')  // ǜ
        .lowercase()

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PeriodicTableCell(
    element: Element,
    zhName: String?,
    isSelected: Boolean,
    alpha: Float,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categoryColor = LocalCategoryColors.current.forCategory(element.category)
    val shape = MaterialTheme.shapes.extraSmall
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val cellBgAlpha = if (isSelected) {
        CategoryAlpha.CELL_SELECTED
    } else if (isDark) {
        CategoryAlpha.CELL_DARK
    } else {
        CategoryAlpha.CELL
    }

    Box(
        modifier = modifier
            .padding(1.dp)
            .clip(shape)
            .background(categoryColor.copy(alpha = cellBgAlpha))
            .alpha(alpha)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, categoryColor, shape)
                } else Modifier
            )
            .combinedClickable(
                indication = ripple(),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp),
        ) {
            Text(
                text = "${element.atomicNumber}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Text(
                text = element.symbol,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            if (zhName != null) {
                Text(
                    text = zhName,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun FBlockMarker(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.extraSmall
    Box(
        modifier = modifier
            .padding(1.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(name = "PeriodicTableGrid Light", showBackground = true)
@Composable
private fun PeriodicTableGridPreview() {
    com.periodic.pro.theme.PeriodicProTheme {
        PeriodicTableGrid(
            elements = emptyList(),
            zhMap = emptyMap(),
            selectedCategory = null,
            searchQuery = "",
            isMultiSelectMode = false,
            selectedIds = emptySet(),
            onElementClick = {},
            onElementLongClick = {},
        )
    }
}
