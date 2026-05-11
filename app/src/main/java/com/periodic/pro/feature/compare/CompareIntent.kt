package com.periodic.pro.feature.compare

/**
 * 对比屏用户意图。
 */
sealed interface CompareIntent {
    /** 返回上一页 */
    data object NavigateBack : CompareIntent
}
