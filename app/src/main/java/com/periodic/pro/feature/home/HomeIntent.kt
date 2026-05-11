package com.periodic.pro.feature.home

/**
 * Home 屏用户意图。
 */
sealed interface HomeIntent {
    /** 加载推荐元素 */
    data object LoadFeatured : HomeIntent

    /** 搜索文本变化 */
    data class Search(val query: String) : HomeIntent
}
