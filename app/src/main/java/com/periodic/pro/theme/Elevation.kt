package com.periodic.pro.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 4 层 Elevation token。
 * L1 Shadow1：微弱投影（卡片）
 * L2 Shadow2：标准悬浮
 * L3 Floating：浮层（BottomSheet、Tooltip）
 * L4 Modal：模态弹窗
 */
object Elevation {
    val Shadow1: Dp = 1.dp
    val Shadow2: Dp = 3.dp
    val Floating: Dp = 6.dp
    val Modal: Dp = 8.dp
}
