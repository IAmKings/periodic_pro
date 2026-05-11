package com.periodic.pro.feature.favorites

import com.periodic.pro.data.element.model.Element

/**
 * Favorites 屏 UI 状态。
 *
 * @param favorites 收藏的元素列表
 * @param zhMap 中文名映射 { atomicNumber -> nameZh }
 * @param isLoading 是否正在加载
 * @param isEmpty 收藏列表是否为空
 */
data class FavoritesUiState(
    val favorites: List<Element> = emptyList(),
    val zhMap: Map<Int, String> = emptyMap(),
    val isLoading: Boolean = true,
    val isEmpty: Boolean = true,
)
