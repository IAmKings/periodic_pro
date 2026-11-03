package com.periodic.pro.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily

/**
 * 专用文本样式 — 事实源：/DESIGN.md §3.3
 * 数值等宽（tnum）与公式等宽字体，避免散写 fontSize。
 * 颜色由调用方按上下文指定（默认给 onSurface 便捷值）。
 */
object PeriodicTextStyles {

    /** 属性值 / 统计数值：等宽数字 */
    val NumericValue: TextStyle @Composable get() = MaterialTheme.typography.bodyLarge.copy(
        fontFeatureSettings = FONT_FEATURE_TNUM,
    )

    /** 数值单位：小号弱化 */
    val NumericUnit: TextStyle @Composable get() = MaterialTheme.typography.bodySmall

    /** 化学方程式：等宽字体 */
    val Equation: TextStyle @Composable get() = MaterialTheme.typography.bodyMedium.copy(
        fontFamily = FontFamily.Monospace,
    )

    /** 电子排布：等宽字体小号 */
    val ElectronConfig: TextStyle @Composable get() = MaterialTheme.typography.bodySmall.copy(
        fontFamily = FontFamily.Monospace,
    )
}

/** 语义色 CompositionLocal（正确/错误/等级/难度），见 Color.kt §4 */
val LocalSemanticColors = staticCompositionLocalOf { SemanticColors() }
