package com.periodic.pro.feature.home

import com.periodic.pro.data.element.model.Element

/**
 * Home 屏 UI 状态。
 *
 * @param featuredElements 推荐元素列表（热门元素）
 * @param zhMap 中文名映射 { atomicNumber -> nameZh }
 * @param searchQuery 搜索关键词
 * @param isLoading 是否正在加载
 */
data class HomeUiState(
    val featuredElements: List<Element> = emptyList(),
    val zhMap: Map<Int, String> = emptyMap(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
) {
    val isReady: Boolean get() = !isLoading
}
