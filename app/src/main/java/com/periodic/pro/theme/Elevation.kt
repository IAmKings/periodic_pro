package com.periodic.pro.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 高度 token — 事实源：/DESIGN.md §6
 * 分层策略：卡片靠表面+发丝线，无阴影；阴影只属于漂浮层。
 * Shadow1 顶栏滚动态 / Shadow2 FAB / Floating 浮层 / Modal 模态
 */
object Elevation {
    val Shadow1: Dp = 1.dp
    val Shadow2: Dp = 3.dp
    val Floating: Dp = 6.dp
    val Modal: Dp = 8.dp
}
