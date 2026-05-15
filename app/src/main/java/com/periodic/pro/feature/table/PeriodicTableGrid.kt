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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.periodic.pro.data.element.model.Category
import com.periodic.pro.data.element.model.Element
import com.periodic.pro.data.element.model.ElementZh
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
                zhMap[element.atomicNumber]?.pinyin?.contains(query) == true
        }.map { it.atomicNumber }.toSet()
    }

    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val minCellPx = with(density) { 48.dp.toPx() }
        val maxCellPx = with(density) { 72.dp.toPx() }
        val cellPx = with(density) { maxWidth.toPx() / 18f }
        val clampedCellPx = cellPx.coerceIn(minCellPx, maxCellPx)
        val cellDp = with(density) { clampedCellPx.toDp() }

        val fBlockMarkerRow6 = Pair(5, 2)
        val fBlockMarkerRow7 = Pair(6, 2)

        val contentWidthDp = with(density) { (18 * clampedCellPx).toDp() }
        val contentHeightPx = maxOf(10 * clampedCellPx, with(density) { maxHeight.toPx() })
        val contentHeightDp = with(density) { contentHeightPx.toDp() }

        val scrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(scrollState),
        ) {
            Box(
                modifier = Modifier
                    .requiredSize(width = contentWidthDp, height = contentHeightDp),
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
                            .offset { IntOffset(col * clampedCellPx.toInt(), row * clampedCellPx.toInt()) }
                            .size(cellDp),
                    )
                }

                // F-block 标记
                if (gridMap[fBlockMarkerRow6] == null) {
                    FBlockMarker(
                        text = "*",
                        onClick = { onElementClick(57) },
                        modifier = Modifier
                            .offset { IntOffset(fBlockMarkerRow6.second * clampedCellPx.toInt(), fBlockMarkerRow6.first * clampedCellPx.toInt()) }
                            .size(cellDp),
                    )
                }
                if (gridMap[fBlockMarkerRow7] == null) {
                    FBlockMarker(
                        text = "**",
                        onClick = { onElementClick(89) },
                        modifier = Modifier
                            .offset { IntOffset(fBlockMarkerRow7.second * clampedCellPx.toInt(), fBlockMarkerRow7.first * clampedCellPx.toInt()) }
                            .size(cellDp),
                    )
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

    Box(
        modifier = modifier
            .padding(1.dp)
            .clip(shape)
            .background(categoryColor.copy(alpha = 0.3f))
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
    text: String,
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
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
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
