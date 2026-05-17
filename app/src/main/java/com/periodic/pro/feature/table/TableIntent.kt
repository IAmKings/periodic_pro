package com.periodic.pro.feature.table

import com.periodic.pro.data.element.model.Category

/**
 * 周期表屏用户意图。
 */
sealed interface TableIntent {
    /** 加载元素数据 */
    data object LoadElements : TableIntent

    /** 搜索文本变化 */
    data class Search(val query: String) : TableIntent

    /** 按分类筛选（null = "全部"） */
    data class FilterByCategory(val category: Category?) : TableIntent

    /** 多选模式下切换选中/取消选中 */
    data class ToggleElement(val atomicNumber: Int) : TableIntent

    /** 进入多选模式 */
    data object EnterMultiSelect : TableIntent

    /** 退出多选模式 */
    data object ExitMultiSelect : TableIntent

    /** 确认对比（导航到 Compare 屏） */
    data object ConfirmCompare : TableIntent

    /** 点击元素（非多选模式下跳详情） */
    data class OnElementClick(val atomicNumber: Int) : TableIntent

    /** 长按元素（进入多选模式） */
    data class OnElementLongClick(val atomicNumber: Int) : TableIntent

    /** 选择整个系列（进入多选 + 全选指定元素） */
    data class SelectSeries(val atomicNumbers: List<Int>) : TableIntent
}
