package com.periodic.pro.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.periodic.pro.theme.CategoryAlpha
import com.periodic.pro.theme.Dimensions
import com.periodic.pro.theme.PeriodicProTheme

/**
 * 属性标签 Chip — tonal 风格（DESIGN.md §8.4 同族）。
 * 分类色 14% 底 + 分类色文字，用于元素分类标识（详情页 / 分类页）。
 *
 * @param text Chip 显示文本
 * @param selected 是否选中（选中时 tonal 填充，否则仅描边）
 * @param onClick 点击回调
 * @param modifier Modifier
 * @param categoryColor 分类色
 */
@Composable
fun PropertyChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    categoryColor: Color = MaterialTheme.colorScheme.primary,
) {
    val container = if (selected) {
        categoryColor.copy(alpha = CategoryAlpha.CHIP)
    } else {
        Color.Transparent
    }
    val textColor = if (selected) categoryColor else MaterialTheme.colorScheme.onSurfaceVariant
    val borderStroke = if (selected) {
        BorderStroke(width = 1.dp, color = categoryColor.copy(alpha = 0.4f))
    } else {
        BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = container,
        contentColor = textColor,
        border = borderStroke,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = Dimensions.Dp8,
                vertical = Dimensions.Dp4,
            ),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(name = "Light Unselected", showBackground = true)
@Preview(name = "Dark Unselected", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PropertyChipUnselectedPreview() {
    PeriodicProTheme {
        PropertyChip(
            text = "过渡金属",
            selected = false,
            onClick = {},
            categoryColor = Color(0xFFF5A623),
        )
    }
}

@Preview(name = "Light Selected", showBackground = true)
@Preview(name = "Dark Selected", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PropertyChipSelectedPreview() {
    PeriodicProTheme {
        PropertyChip(
            text = "过渡金属",
            selected = true,
            onClick = {},
            categoryColor = Color(0xFFF5A623),
        )
    }
}
