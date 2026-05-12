package com.periodic.pro.feature.discover

/**
 * Discover 屏用户意图。
 */
sealed interface DiscoverIntent {
    /** 加载知识点数据 */
    data object LoadData : DiscoverIntent

    /** 点击知识点卡片，跳转到元素详情 */
    data class SelectItem(val atomicNumber: Int) : DiscoverIntent
}
