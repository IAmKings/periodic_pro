package com.periodic.pro.ui.pattern

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.periodic.pro.data.element.ElectronShells
import com.periodic.pro.theme.PeriodicProTheme
import kotlin.math.cos
import kotlin.math.sin

/**
 * Canvas 2D 原子动画 — 多壳层独立旋转电子。
 *
 * 参考 ElectronShell 实现：每层独立方向/速度旋转，脉冲光晕，电子发光效果。
 *
 * @param atomicNumber 原子序号，决定核大小和电子分布
 * @param categoryColor 原子核/电子颜色（元素分类色）
 * @param modifier Modifier
 */
@Composable
fun AtomCanvas(
    atomicNumber: Int,
    period: Int,
    categoryColor: Color,
    symbol: String = "",
    scale: Float = 1.5f,
    modifier: Modifier = Modifier,
) {
    val shells = remember(atomicNumber) { ElectronShells.getElectronShells(atomicNumber) }
    val baseRadius = 32f * scale
    val radiusIncrement = when {
        shells.size <= 2 -> 50f * scale
        shells.size <= 4 -> 45f * scale
        shells.size <= 5 -> 40f * scale
        else -> 35f * scale
    }

    // 每层独立旋转
    val rotations = shells.mapIndexed { index, _ ->
        val transition = rememberInfiniteTransition(label = "orbit-$index")
        val duration = 3000 + index * 1500
        val direction = if (index % 2 == 0) 1f else -1f
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f * direction,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "rotation-$index",
        ).value
    }

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    val nucleusRadius = ((20f + atomicNumber * 0.3f) * scale).coerceIn(20f * scale, 55f * scale)

    Box(
        modifier = modifier.fillMaxWidth().height((240 * scale).dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2

            // 背景光晕
            val glowRadius = baseRadius + shells.size * radiusIncrement + 20f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(categoryColor.copy(alpha = pulseAlpha), Color.Transparent),
                    center = Offset(centerX, centerY),
                    radius = glowRadius,
                ),
                radius = glowRadius,
                center = Offset(centerX, centerY),
            )

            // 电子壳层虚线轨道
            shells.forEachIndexed { index, _ ->
                val orbitRadius = baseRadius + index * radiusIncrement
                drawCircle(
                    color = Color.Gray.copy(alpha = 0.35f),
                    radius = orbitRadius,
                    center = Offset(centerX, centerY),
                    style = Stroke(
                        width = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
                    ),
                )
            }

            // 原子核光晕
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(categoryColor.copy(alpha = 0.35f), Color.Transparent),
                    center = Offset(centerX, centerY),
                    radius = nucleusRadius + 10f * scale,
                ),
                radius = nucleusRadius + 10f * scale,
                center = Offset(centerX, centerY),
            )

            // 原子核
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(categoryColor, categoryColor.copy(alpha = 0.5f)),
                    center = Offset(centerX, centerY),
                    radius = nucleusRadius,
                ),
                radius = nucleusRadius,
                center = Offset(centerX, centerY),
            )

            // 电子 — 每层独立旋转
            shells.forEachIndexed { shellIndex, electronCount ->
                val orbitRadius = baseRadius + shellIndex * radiusIncrement
                val angleStep = (2 * Math.PI / electronCount).toFloat()
                val rotation = rotations[shellIndex]
                val electronScale = if (shellIndex >= 5) 0.8f else 1f

                rotate(degrees = rotation, pivot = Offset(centerX, centerY)) {
                    for (i in 0 until electronCount) {
                        val angle = angleStep * i - (Math.PI / 2).toFloat()
                        val ex = centerX + orbitRadius * cos(angle)
                        val ey = centerY + orbitRadius * sin(angle)
                        val pos = Offset(ex, ey)

                        // 电子光晕
                        drawCircle(color = categoryColor.copy(alpha = 0.3f), radius = 8f * scale * electronScale, center = pos)
                        // 电子核心
                        drawCircle(color = categoryColor, radius = 4f * scale * electronScale, center = pos)
                        // 高光
                        drawCircle(color = Color.White.copy(alpha = 0.7f), radius = 1.5f * scale * electronScale, center = Offset(ex - 1.5f * scale, ey - 1.5f * scale))
                    }
                }
            }
        }

        // 元素符号覆盖
        Text(
            text = symbol,
            color = Color.White.copy(alpha = 0.15f),
            fontSize = (12 * scale).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Preview(name = "Light - H", showBackground = true)
@Preview(name = "Dark - H", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AtomCanvasHydrogenPreview() {
    PeriodicProTheme {
        AtomCanvas(atomicNumber = 1, period = 1, categoryColor = Color(0xFF748FFC))
    }
}

@Preview(name = "Light - Fe", showBackground = true)
@Preview(name = "Dark - Fe", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AtomCanvasIronPreview() {
    PeriodicProTheme {
        AtomCanvas(atomicNumber = 26, period = 4, categoryColor = Color(0xFFFFD43B))
    }
}

@Preview(name = "Light - U", showBackground = true)
@Preview(name = "Dark - U", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AtomCanvasUraniumPreview() {
    PeriodicProTheme {
        AtomCanvas(atomicNumber = 92, period = 7, categoryColor = Color(0xFFE599F7))
    }
}
