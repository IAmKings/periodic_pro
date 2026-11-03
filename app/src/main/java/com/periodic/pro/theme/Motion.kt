package com.periodic.pro.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

/**
 * 动效 token 与公共动效扩展 — 事实源：/DESIGN.md §7
 * 原则：动效服务反馈，不做纯装饰动画。
 */
object Motion {
    /** 按压弹簧：快速回弹 */
    val PressSpring = spring<Float>(
        dampingRatio = 0.7f,
        stiffness = 600f,
        visibilityThreshold = 0.01f,
    )

    /** 通用弹簧（选中态色变等用默认 spring） */
    val StandardSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    )

    /** 收藏心形弹跳 */
    val BounceSpring = spring<Float>(
        dampingRatio = 0.45f,
        stiffness = 700f,
    )
}

/** 按压缩放：按下时缩至 0.97，松手弹回。用于卡片/磁贴等大点击面。 */
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.97f,
): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = Motion.PressSpring,
        label = "pressScale",
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/** 记住一个可复用的 InteractionSource（配合 pressScale / clickable indication） */
@Composable
fun rememberInteractionSource(): MutableInteractionSource = remember { MutableInteractionSource() }
