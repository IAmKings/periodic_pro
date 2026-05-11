package com.periodic.pro.feature.table

/**
 * 周期表屏一次性副作用（导航 / Toast）。
 */
sealed interface TableEffect {
    /** 导航到元素详情页 */
    data class NavigateToDetail(val atomicNumber: Int) : TableEffect

    /** 导航到对比页 */
    data class NavigateToCompare(val ids: List<Int>) : TableEffect
}
