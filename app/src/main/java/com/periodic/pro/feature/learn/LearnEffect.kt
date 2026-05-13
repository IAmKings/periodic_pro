package com.periodic.pro.feature.learn

/**
 * Learn 屏一次性副作用。
 */
sealed interface LearnEffect {
    /** 导航到元素详情 */
    data class NavigateToDetail(val atomicNumber: Int) : LearnEffect
}
