package com.periodic.pro.feature.learn

/**
 * Learn 屏用户意图。
 */
sealed interface LearnIntent {
    /** 加载学习数据 */
    data object LoadData : LearnIntent

    /** 选择一个元素查看详情 */
    data class SelectElement(val atomicNumber: Int) : LearnIntent

    /** 点击「查看元素详情」按钮，跳转到元素详情 */
    data class NavigateToDetail(val atomicNumber: Int) : LearnIntent

    /** 返回元素学习列表 */
    data object BackToList : LearnIntent
}
