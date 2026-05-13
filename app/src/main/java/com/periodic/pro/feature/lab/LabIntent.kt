package com.periodic.pro.feature.lab

import com.periodic.pro.data.lab.model.ReactionLevel
import com.periodic.pro.data.lab.model.ReactionType

/**
 * Lab 屏用户意图。
 */
sealed interface LabIntent {
    /** 加载反应数据 */
    data object LoadData : LabIntent

    /** 按等级筛选 */
    data class FilterByLevel(val level: ReactionLevel?) : LabIntent

    /** 按类型筛选 */
    data class FilterByType(val type: ReactionType?) : LabIntent

    /** 选择一个反应查看详情 */
    data class SelectReaction(val reactionId: String) : LabIntent

    /** 点击涉及元素，跳转到元素详情 */
    data class NavigateToDetail(val atomicNumber: Int) : LabIntent

    /** 返回反应列表 */
    data object BackToList : LabIntent
}
