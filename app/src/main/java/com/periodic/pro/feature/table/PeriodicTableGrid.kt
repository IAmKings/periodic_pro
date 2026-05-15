package com.periodic.pro.feature.table

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
import com.periodic.pro.theme.LocalCategoryColors
import com.periodic.pro.theme.forCategory
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable

/**
 * 周期表 18×9 网格布局。
 *
 * 使用自定义 [Layout] 将 118 个元素定位到标准周期表网格位置。
 * 集成 Telephoto [Modifier.zoomable] 实现缩放/平移手势。
 *
 * 布局映射：
 * - 常规元素：位置 = (period - 1, group - 1)
 * - 镧系 (57-71)：行 8，列 3-17
 * - 锕系 (89-103)：行 9，列 3-17
 * - Period 6/7 的 Group 3 显示 * / ** 占位标记
 *
 * @param elements 全部 118 个元素
 * @param zhMap 中文名映射
 * @param selectedCategory 选中分类（null=全部）
 * @param searchQuery 搜索关键词
 * @param isMultiSelectMode 是否多选模式
 * @param selectedIds 已选原子序号集合
 * @param onElementClick 点击元素回调
 * @param onElementLongClick 长按元素回调
 * @param modifier Modifier
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
    // 构建行/列 → 元素的映射（用于 Telephoto 命中检测）
    val gridMap = remember(elements) { buildGridMap(elements) }

    // 搜索匹配集合
    val query = searchQuery.trim().lowercase()
    val matchedIds = remember(elements, zhMap, query) {
        if (query.isEmpty()) {
            null // null 表示全部匹配
        } else {
            elements.filter { element ->
                element.symbol.lowercase().contains(query) ||
                    element.name.lowercase().contains(query) ||
                    (query.toIntOrNull() != null && element.atomicNumber.toString().contains(query)) ||
                    zhMap[element.atomicNumber]?.nameZh?.contains(query) == true ||
                    zhMap[element.atomicNumber]?.pinyin?.contains(query) == true
            }.map { it.atomicNumber }.toSet()
        }
    }

    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val containerWidthPx = with(density) { maxWidth.toPx() }
        val minCellPx = with(density) { 48.dp.toPx() }
        val maxCellPx = with(density) { 72.dp.toPx() }
        val cellPx = containerWidthPx / 18f
        val clampedCellPx = cellPx.coerceIn(minCellPx, maxCellPx)
        val cellDp = with(density) { clampedCellPx.toDp() }

        // 第 6/7 周期 Group 3 的位置：F-block 标记
        val fBlockMarkerRow6 = Pair(5, 2) // (period-1, group-1)
        val fBlockMarkerRow7 = Pair(6, 2)

        val zoomableState = rememberZoomableState(
            zoomSpec = ZoomSpec(maxZoomFactor = 3f),
        )

        val contentWidthDp = with(density) { (18 * clampedCellPx).toDp() }
        val contentHeightDp = with(density) { (10 * clampedCellPx).toDp() }

        Box(
            modifier = Modifier
                .requiredSize(width = contentWidthDp, height = maxOf(contentHeightDp, with(density) { maxHeight }))
                .zoomable(
                    state = zoomableState,
                    onClick = { offset ->
                        val cell = hitTest(offset, clampedCellPx)
                        if (cell != null) {
                            val element = gridMap[cell]
                            if (element != null) {
                                onElementClick(element.atomicNumber)
                            }
                        }
                    },
                    onLongClick = { offset ->
                        val cell = hitTest(offset, clampedCellPx)
                        if (cell != null) {
                            val element = gridMap[cell]
                            if (element != null) {
                                onElementLongClick(element.atomicNumber)
                            }
                        }
                    },
                ),
            contentAlignment = Alignment.TopStart,
        ) {
            // 使用 offset 定位每个元素
            elements.forEach { element ->
                val (row, col) = getGridPosition(element)
                val isSelected = element.atomicNumber in selectedIds
                val isInSearch = matchedIds == null || element.atomicNumber in matchedIds

                val alphaValue = when {
                    // 搜索模式：未匹配降低透明度
                    matchedIds != null && !isInSearch -> 0.2f
                    // 分类筛选模式
                    selectedCategory != null && element.category != selectedCategory -> 0.2f
                    // 多选模式：未选中降低透明度
                    isMultiSelectMode && !isSelected -> 0.5f
                    else -> 1.0f
                }

                PeriodicTableCell(
                    element = element,
                    zhName = zhMap[element.atomicNumber]?.nameZh,
                    isSelected = isSelected,
                    alpha = alphaValue,
                    modifier = Modifier
                        .offset { IntOffset(col * clampedCellPx.toInt(), row * clampedCellPx.toInt()) }
                        .size(cellDp),
                )
            }

            // F-block 入口标记：Period 6 Group 3 的 *
            if (gridMap[fBlockMarkerRow6] == null) {
                FBlockMarker(
                    text = "*",
                    onClick = { onElementClick(57) }, // La 为镧系入口
                    modifier = Modifier
                        .offset { IntOffset(fBlockMarkerRow6.second * clampedCellPx.toInt(), fBlockMarkerRow6.first * clampedCellPx.toInt()) }
                        .size(cellDp),
                )
            }

            if (gridMap[fBlockMarkerRow7] == null) {
                FBlockMarker(
                    text = "**",
                    onClick = { onElementClick(89) }, // Ac 为锕系入口
                    modifier = Modifier
                        .offset { IntOffset(fBlockMarkerRow7.second * clampedCellPx.toInt(), fBlockMarkerRow7.first * clampedCellPx.toInt()) }
                        .size(cellDp),
                )
            }
        }
    }
}

/**
 * 获取元素在 0-based 网格中的位置。
 *
 * @return Pair(行, 列)，均为 0-based
 */
private fun getGridPosition(element: Element): Pair<Int, Int> {
    return when (element.atomicNumber) {
        // 镧系 (57-71)：第 8 行（0-based=7），列 3-17（0-based=2-16）
        in 57..71 -> Pair(7, element.atomicNumber - 57 + 2)
        // 锕系 (89-103)：第 9 行（0-based=8），列 3-17（0-based=2-16）
        in 89..103 -> Pair(8, element.atomicNumber - 89 + 2)
        // 常规元素
        else -> Pair(
            (element.period ?: 1) - 1,
            (element.group ?: 1) - 1,
        )
    }
}

/**
 * 构建 (行, 列) → Element 的映射。
 */
private fun buildGridMap(elements: List<Element>): Map<Pair<Int, Int>, Element> {
    val map = mutableMapOf<Pair<Int, Int>, Element>()
    for (element in elements) {
        val pos = getGridPosition(element)
        map[pos] = element
    }
    return map
}

/**
 * 命中检测：将 Telephoto 的 Offset（content 坐标）映射到网格 (row, col)。
 */
private fun hitTest(
    offset: Offset,
    cellPx: Float,
): Pair<Int, Int>? {
    val col = (offset.x / cellPx).toInt()
    val row = (offset.y / cellPx).toInt()
    return if (col in 0..17 && row in 0..8) {
        Pair(row, col)
    } else {
        null
    }
}

/**
 * 周期表网格中的元素单元格。
 *
 * 仅渲染视觉内容，不处理点击事件——点击由父级 Telephoto [zoomable] 统一处理。
 */
@Composable
private fun PeriodicTableCell(
    element: Element,
    zhName: String?,
    isSelected: Boolean,
    alpha: Float,
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
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = shape,
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp),
        ) {
            // 原子序号
            Text(
                text = "${element.atomicNumber}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            // 元素符号
            Text(
                text = element.symbol,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            // 中文名
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

/**
 * F-block 入口标记（`*` / `**`）。
 * 点击跳转到镧系/锕系首元素。
 */
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
