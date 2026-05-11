package com.periodic.pro.feature.favorites

/**
 * Favorites 屏用户意图。
 *
 * 导航相关意图由 Screen 层的回调直接处理，不经过 ViewModel。
 * 参见：FavoritesScreen(onNavigateToDetail, onNavigateToTable)
 */
sealed interface FavoritesIntent {
    /** 移除收藏 */
    data class RemoveFavorite(val atomicNumber: Int) : FavoritesIntent
}
