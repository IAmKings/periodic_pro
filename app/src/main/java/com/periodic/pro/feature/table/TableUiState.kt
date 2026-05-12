package com.periodic.pro.feature.table

import com.periodic.pro.data.element.model.Category
import com.periodic.pro.data.element.model.Element
import com.periodic.pro.data.element.model.ElementZh

/**
 * 周期表屏 UI 状态。
 *
 * @param elements 全部 118 个元素
 * @param zhMap 中文名映射 { atomicNumber -> ElementZh }
 * @param searchQuery 搜索关键词
 * @param selectedCategory 选中的分类 ID（null = "全部"）
 * @param isMultiSelectMode 是否处于多选模式
 * @param selectedIds 已选中的原子序号集合
 * @param isLoading 是否正在加载
 */
data class TableUiState(
    val elements: List<Element> = emptyList(),
    val zhMap: Map<Int, ElementZh> = emptyMap(),
    val searchQuery: String = "",
    val selectedCategory: Category? = null,
    val isMultiSelectMode: Boolean = false,
    val selectedIds: Set<Int> = emptySet(),
    val isLoading: Boolean = true,
) {
    /**
     * 多选模式下非空即为选中状态。
     */
    val inSelectionMode: Boolean get() = isMultiSelectMode && selectedIds.isNotEmpty()
}
