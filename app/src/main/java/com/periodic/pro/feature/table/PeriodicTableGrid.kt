package com.periodic.pro.feature.table

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.periodic.pro.data.element.model.Category
import com.periodic.pro.data.element.model.Element
import com.periodic.pro.data.element.model.ElementZh
import com.periodic.pro.theme.CATEGORY_CELL_ALPHA
import com.periodic.pro.theme.CATEGORY_CELL_DARK_ALPHA
import com.periodic.pro.theme.LocalCategoryColors
import com.periodic.pro.theme.forCategory

/**
 * 周期表 18×10 网格布局。
 *
 * 水平可滚动，每个元素单元格独立处理点击/长按。
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
    onSelectSeries: (List<Int>) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val gridMap = remember(elements) { buildGridMap(elements) }

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

        // 行标签列宽
        val rowLabelWidthDp = with(density) { 24.dp }

        // 列号行高
        val headerHeightDp = with(density) { 20.dp }

        val headerHeightPx = with(density) { headerHeightDp.toPx().toInt() }

        // 计算 y 偏移（含间隔）：镧系前 gapPx*2，镧锕之间无额外间隔
        fun yOffset(row: Int): Int {
            var y = headerHeightPx + row * cellPxInt
            if (row >= 7) y += gapPx * 2 // 镧系前翻倍间隔
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
                // 左侧行号列（固定，顶部留白对齐元素区的列号行）
                Column(modifier = Modifier.width(rowLabelWidthDp)) {
                    Spacer(modifier = Modifier.height(headerHeightDp))
                    for (i in 0..6) {
                        Box(modifier = Modifier.size(width = rowLabelWidthDp, height = cellDp), contentAlignment = Alignment.Center) {
                            Text(rowLabels[i], style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(modifier = Modifier.size(width = rowLabelWidthDp, height = gapDp * 2))
                    Box(modifier = Modifier.size(width = rowLabelWidthDp, height = cellDp), contentAlignment = Alignment.Center) {
                        Text(rowLabels[7], style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box(modifier = Modifier.size(width = rowLabelWidthDp, height = cellDp), contentAlignment = Alignment.Center) {
                        Text(rowLabels[8], style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                matchedIds != null && !isInSearch -> 0.2f
                                selectedCategory != null && element.category != selectedCategory -> 0.2f
                                isMultiSelectMode && !isSelected -> 0.5f
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
                        if (gridMap[fBlockMarkerRow6] == null) {
                            FBlockMarker(
                                rangeStart = 57,
                                rangeEnd = 71,
                                onClick = { onSelectSeries((57..71).toList()) },
                                modifier = Modifier
                                    .offset { IntOffset(fBlockMarkerRow6.second * cellPxInt, yOffset(fBlockMarkerRow6.first)) }
                                    .size(cellDp),
                            )
                        }
                        if (gridMap[fBlockMarkerRow7] == null) {
                            FBlockMarker(
                                rangeStart = 89,
                                rangeEnd = 103,
                                onClick = { onSelectSeries((89..103).toList()) },
                                modifier = Modifier
                                    .offset { IntOffset(fBlockMarkerRow7.second * cellPxInt, yOffset(fBlockMarkerRow7.first)) }
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

private fun buildGridMap(elements: List<Element>): Map<Pair<Int, Int>, Element> {
    val map = mutableMapOf<Pair<Int, Int>, Element>()
    for (element in elements) {
        val pos = getGridPosition(element)
        map[pos] = element
    }
    return map
}

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
    val shape = RoundedCornerShape(4.dp)
    val cellBgAlpha = if (isSystemInDarkTheme()) CATEGORY_CELL_DARK_ALPHA else CATEGORY_CELL_ALPHA

    Box(
        modifier = modifier
            .padding(1.dp)
            .clip(shape)
            .background(categoryColor.copy(alpha = cellBgAlpha))
            .alpha(alpha)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape)
                } else Modifier
            )
            .combinedClickable(
                indication = null,
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
                    style = MaterialTheme.typography.labelSmall,
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
    rangeStart: Int,
    rangeEnd: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier = modifier
            .padding(1.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp),
        ) {
            Text(
                text = "$rangeStart",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Text(
                text = "-",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Text(
                text = "$rangeEnd",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
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
