package com.periodic.pro.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

/** 20.dp 圆角 — 用于 GlassSurface 等大卡片场景（M3 Shapes 1.3.x 无 extraExtraLarge，独立定义）。 */
val ShapeXXLarge = RoundedCornerShape(20.dp)
