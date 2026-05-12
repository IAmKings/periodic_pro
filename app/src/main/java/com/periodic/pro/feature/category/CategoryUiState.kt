package com.periodic.pro.feature.category

import com.periodic.pro.data.element.model.Category
import com.periodic.pro.data.element.model.Element

/**
 * Category 屏 UI 状态。
 *
 * @param categories 所有分类列表（固定 10 项）
 * @param selectedCategory 当前选中的分类（null 表示在入口 Grid 页）
 * @param categoryElements 当前选中分类下的元素列表
 * @param zhMap 元素中文名映射
 * @param elementCounts 每个分类下的元素数量
 * @param isLoading 是否加载中
 */
data class CategoryUiState(
    val categories: List<Category> = Category.entries,
    val selectedCategory: Category? = null,
    val categoryElements: List<Element> = emptyList(),
    val zhMap: Map<Int, String> = emptyMap(),
    val elementCounts: Map<Category, Int> = emptyMap(),
    val isLoading: Boolean = true,
)
