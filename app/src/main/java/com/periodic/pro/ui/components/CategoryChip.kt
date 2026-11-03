package com.periodic.pro.ui.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.periodic.pro.theme.CategoryAlpha
import com.periodic.pro.theme.Dimensions
import com.periodic.pro.theme.PeriodicProTheme

/**
 * 分类筛选 Chip — Spectra 风格（DESIGN.md §8.4）。
 *
 * 未选：surface 底 + 发丝描边 + 分类色圆点 + onSurface 文字
 * 选中：分类色 × 0.14 底 + 分类色文字 + 圆点放大
 *
 * @param text Chip 显示文本（如"全部"/"碱金属"）
 * @param selected 是否选中
 * @param onClick 点击回调
 * @param modifier Modifier
 * @param selectedColor 分类色（"全部"等无分类色时传 null，用 primary）
 */
@Composable
fun CategoryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color? = null,
) {
    val accent = selectedColor ?: MaterialTheme.colorScheme.primary
    val container by animateColorAsState(
        targetValue = if (selected) {
            accent.copy(alpha = CategoryAlpha.CHIP)
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 150),
        label = "chipContainer",
    )
    val contentColor = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (selected) {
        accent.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    val shape = MaterialTheme.shapes.small
    val interaction = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .height(36.dp)
            .clip(shape)
            .background(container)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .clickable(
                interactionSource = interaction,
                indication = ripple(),
                onClick = onClick,
            )
            .padding(horizontal = Dimensions.Dp12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 分类色圆点
        Spacer(
            modifier = Modifier
                .size(if (selected) 8.dp else 6.dp)
                .background(accent, CircleShape),
        )
        Spacer(modifier = Modifier.size(Dimensions.Dp8))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(name = "Light Unselected", showBackground = true)
@Preview(name = "Dark Unselected", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CategoryChipUnselectedPreview() {
    PeriodicProTheme {
        CategoryChip(text = "全部", selected = false, onClick = {})
    }
}

@Preview(name = "Light Selected", showBackground = true)
@Preview(name = "Dark Selected", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CategoryChipSelectedPreview() {
    PeriodicProTheme {
        CategoryChip(text = "碱金属", selected = true, onClick = {}, selectedColor = Color(0xFFE5484D))
    }
}
