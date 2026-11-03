package com.periodic.pro.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Spectra 形状 — 事实源：/DESIGN.md §4
// extraSmall 6 / small 8 / medium 12 / large 16 / extraLarge 20
// 一致性规则：同层级组件圆角一致；嵌套时内层比外层小 4dp。

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp),
)
