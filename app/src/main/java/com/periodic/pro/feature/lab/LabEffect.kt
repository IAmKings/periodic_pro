package com.periodic.pro.feature.lab

/**
 * Lab 屏一次性副作用。
 */
sealed interface LabEffect {
    /** 导航到元素详情 */
    data class NavigateToDetail(val atomicNumber: Int) : LabEffect
}
