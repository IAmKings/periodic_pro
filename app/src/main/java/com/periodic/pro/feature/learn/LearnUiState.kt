package com.periodic.pro.feature.learn

import com.periodic.pro.data.learn.model.LearnItem

/**
 * Learn 屏 UI 导航模式。
 */
enum class LearnNavMode {
    /** 元素学习列表 */
    LIST,

    /** 学习详情 */
    DETAIL,
}

/**
 * Learn 屏 UI 状态。
 *
 * @param navMode 当前导航模式
 * @param items 全部学习资料列表
 * @param selectedItem 当前查看的学习详情
 * @param isLoading 是否正在加载
 * @param errorMessage 错误信息
 */
data class LearnUiState(
    val navMode: LearnNavMode = LearnNavMode.LIST,
    val items: List<LearnItem> = emptyList(),
    val selectedItem: LearnItem? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    /** 原子序数 → 元素符号映射 */
    val symbolMap: Map<Int, String> = emptyMap(),
) {
    val isReady: Boolean get() = !isLoading && errorMessage == null

    /** 按等级分组：核心 / 重点 / 普通 */
    val coreItems: List<LearnItem> get() = items.filter { it.level == "core" }
    val importantItems: List<LearnItem> get() = items.filter { it.level == "important" }
    val otherItems: List<LearnItem> get() = items.filter { it.level == "other" }
}
