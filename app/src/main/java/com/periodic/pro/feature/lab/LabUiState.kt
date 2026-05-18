package com.periodic.pro.feature.lab

import com.periodic.pro.data.lab.model.ChemicalReaction
import com.periodic.pro.data.lab.model.ReactionLevel
import com.periodic.pro.data.lab.model.ReactionType

/**
 * Lab 屏 UI 导航模式。
 */
enum class LabNavMode {
    /** 反应列表 */
    LIST,

    /** 反应详情 */
    DETAIL,
}

/**
 * Lab 屏筛选状态。
 *
 * @param activeLevel 当前筛选的年级等级（null=全部）
 * @param activeType 当前筛选的反应类型（null=全部）
 */
data class LabFilterState(
    val activeLevel: ReactionLevel? = null,
    val activeType: ReactionType? = null,
)

/**
 * Lab 屏 UI 状态。
 *
 * @param navMode 当前导航模式
 * @param allReactions 全部反应列表
 * @param filteredReactions 筛选后的反应列表
 * @param selectedReaction 当前查看的反应详情
 * @param filter 当前筛选状态
 * @param isLoading 是否正在加载
 * @param errorMessage 错误信息
 */
data class LabUiState(
    val navMode: LabNavMode = LabNavMode.LIST,
    val allReactions: List<ChemicalReaction> = emptyList(),
    val filteredReactions: List<ChemicalReaction> = emptyList(),
    val selectedReaction: ChemicalReaction? = null,
    val filter: LabFilterState = LabFilterState(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    /** 原子序数 → 元素符号映射 */
    val symbolMap: Map<Int, String> = emptyMap(),
    val listScrollIndex: Int = 0,
    val listScrollOffset: Int = 0,
) {
    val isReady: Boolean get() = !isLoading && errorMessage == null

    /** 所有不重复的反应类型（用于 FilterChips 展示）。 */
    val availableTypes: List<ReactionType> =
        ReactionType.entries.toList()

    /** 当前是否有活跃筛选条件。 */
    val hasActiveFilter: Boolean get() =
        filter.activeLevel != null || filter.activeType != null
}
