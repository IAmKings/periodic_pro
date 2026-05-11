@file:OptIn(ExperimentalHazeApi::class)

package com.periodic.pro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

/**
 * 全局 HazeState CompositionLocal。
 * 在 App 根级（AppGlassProvider）提供，下方 GlassSurface 共享。
 */
val LocalHazeState = compositionLocalOf<HazeState?> { null }

/**
 * 应用根级毛玻璃提供者。
 * 在 NavigationSuiteScaffold 外层包裹，为下层所有 GlassSurface 提供 HazeState。
 */
@Composable
fun AppGlassProvider(
    content: @Composable () -> Unit,
) {
    val hazeState = remember { HazeState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .hazeSource(state = hazeState),
    ) {
        CompositionLocalProvider(LocalHazeState provides hazeState) {
            content()
        }
    }
}

/**
 * 毛玻璃 Surface 组件。
 *
 * 基于 Haze 1.6.x 实现（PRD 要求 1.7.2，但 1.7.x 需要 Kotlin 2.2+，
 * 当前使用 Kotlin 2.0.21，因此锁定 1.6.0）：
 * - API 31+ → RenderEffect 真模糊
 * - API 24-30 → 半透明 Scrim 降级（Haze 自动处理）
 *
 * 使用约束：
 * - 同屏 GlassSurface 数量 ≤ 5
 * - 禁止在 LazyColumn / Grid 子项中使用
 * - 禁止嵌套 GlassSurface
 *
 * @param modifier Modifier
 * @param blurRadius 模糊半径
 * @param tint 半透明着色
 * @param shape 圆角形状
 * @param content 子内容
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    blurRadius: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
    shape: Shape = RoundedCornerShape(20.dp),
    content: @Composable () -> Unit,
) {
    val hazeState = LocalHazeState.current

    val actualModifier = if (hazeState != null) {
        modifier
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    blurRadius = blurRadius,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    tints = listOf(HazeTint(tint)),
                ),
            )
    } else {
        // 兜底：无 HazeState 时用半透明背景
        modifier
            .clip(shape)
            .background(tint, shape)
    }

    Box(modifier = actualModifier) {
        content()
    }
}
