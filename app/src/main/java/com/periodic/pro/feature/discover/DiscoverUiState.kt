package com.periodic.pro.feature.discover

import com.periodic.pro.data.discover.model.DiscoverItem

/**
 * Discover 屏 UI 状态。
 *
 * @param items 全部知识点列表
 * @param dailyRecommend 每日推荐知识点
 * @param isLoading 是否正在加载
 * @param errorMessage 错误信息（加载失败时）
 */
data class DiscoverUiState(
    val items: List<DiscoverItem> = emptyList(),
    val dailyRecommend: DiscoverItem? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    /** 原子序数 → 元素符号映射 */
    val symbolMap: Map<Int, String> = emptyMap(),
) {
    val isReady: Boolean get() = !isLoading && errorMessage == null
}
