package com.periodic.pro.ui.pattern

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.periodic.pro.theme.PeriodicProTheme
import kotlin.math.cos
import kotlin.math.sin

/**
 * Canvas 2D 原子动画。
 *
 * 绘制原子核（按原子序号比例缩放）+ 电子层轨道（周期数 = 层数）+ 电子小球匀速转动。
 * 原子核颜色使用该元素的 Category 色。
 *
 * @param atomicNumber 原子序号，决定原子核大小
 * @param period 周期数（电子层数）
 * @param categoryColor 原子核颜色（元素分类色）
 * @param modifier Modifier
 */
@Composable
fun AtomCanvas(
    atomicNumber: Int,
    period: Int,
    categoryColor: Color,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "atom")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "orbit",
    )

    Canvas(modifier = modifier.fillMaxWidth().height(240.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val nucleusRadius = (20f + atomicNumber * 0.3f).coerceIn(20f, 60f)

        // 1. 原子核圆球（Category 色 + radialGradient 立体感）
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(categoryColor, categoryColor.copy(alpha = 0.6f)),
                center = center,
                radius = nucleusRadius,
            ),
            radius = nucleusRadius,
            center = center,
        )

        // 2. 电子层轨道圆环（灰色虚线）
        val shellCount = period.coerceIn(1, 7)
        for (i in 1..shellCount) {
            val orbitRadius = nucleusRadius + 20f + i * 22f
            drawCircle(
                color = Color.Gray.copy(alpha = 0.4f),
                radius = orbitRadius,
                center = center,
                style = Stroke(
                    width = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
                ),
            )
        }

        // 3. 最外层轨道上的电子小球（随 rotation 转动）
        val outerOrbitRadius = nucleusRadius + 20f + shellCount * 22f
        val electronAngle = Math.toRadians(rotation.toDouble())
        val electronPos = Offset(
            center.x + outerOrbitRadius * cos(electronAngle).toFloat(),
            center.y + outerOrbitRadius * sin(electronAngle).toFloat(),
        )
        drawCircle(color = Color(0xFF4FC3F7), radius = 6f, center = electronPos)
    }
}

@Preview(name = "Light - H", showBackground = true)
@Preview(
    name = "Dark - H",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun AtomCanvasHydrogenPreview() {
    PeriodicProTheme {
        AtomCanvas(
            atomicNumber = 1,
            period = 1,
            categoryColor = Color(0xFF748FFC),
        )
    }
}

@Preview(name = "Light - Fe", showBackground = true)
@Preview(
    name = "Dark - Fe",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun AtomCanvasIronPreview() {
    PeriodicProTheme {
        AtomCanvas(
            atomicNumber = 26,
            period = 4,
            categoryColor = Color(0xFFFFD43B),
        )
    }
}

@Preview(name = "Light - U", showBackground = true)
@Preview(
    name = "Dark - U",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun AtomCanvasUraniumPreview() {
    PeriodicProTheme {
        AtomCanvas(
            atomicNumber = 92,
            period = 7,
            categoryColor = Color(0xFFE599F7),
        )
    }
}
