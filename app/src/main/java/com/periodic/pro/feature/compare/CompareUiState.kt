package com.periodic.pro.feature.compare

import com.periodic.pro.data.element.model.Element

/**
 * 对比屏 UI 状态。
 *
 * @param elements 待对比的元素列表（2-5 个）
 * @param zhMap 中文名映射 { atomicNumber -> nameZh }
 * @param isLoading 是否正在加载
 * @param isEmpty ids 为空时 true，显示空状态
 */
data class CompareUiState(
    val elements: List<Element> = emptyList(),
    val zhMap: Map<Int, String> = emptyMap(),
    val isLoading: Boolean = false,
    val isEmpty: Boolean = true,
) {
    val isReady: Boolean get() = elements.isNotEmpty() && !isLoading
}
