package com.periodic.pro.feature.discover

/**
 * Discover 屏一次性副作用。
 */
sealed interface DiscoverEffect {
    /** 导航到元素详情 */
    data class NavigateToDetail(val atomicNumber: Int) : DiscoverEffect
}
