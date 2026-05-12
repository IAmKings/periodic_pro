package com.periodic.pro.feature.category

import com.periodic.pro.data.element.model.Category

/**
 * Category 屏用户意图。
 */
sealed interface CategoryIntent {
    /** 选择一个分类（跳转到分类元素列表） */
    data class SelectCategory(val category: Category) : CategoryIntent

    /** 从分类详情页返回 */
    data object NavigateBack : CategoryIntent
}
