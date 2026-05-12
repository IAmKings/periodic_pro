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
import com.periodic.pro.theme.Dimensions
import com.periodic.pro.theme.PeriodicProTheme
import com.periodic.pro.theme.Shapes

/**
 * 属性标签 Chip，用于 Detail 页属性分类标签和 Category 页标签。
 *
 * 选中态：category 色填充 + 白色文字。
 * 非选中态：透明背景 + 描边 + category 色文字。
 * 圆角 shapeExtraSmall(4dp)，高度 24dp。
 *
 * @param text Chip 显示文本
 * @param selected 是否选中
 * @param onClick 点击回调
 * @param modifier Modifier
 * @param categoryColor 分类色（选中时作为填充色，非选中时作为文字和描边色）
 */
@Composable
fun PropertyChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    categoryColor: Color = MaterialTheme.colorScheme.primary,
) {
    val chipColor = if (selected) categoryColor else Color.Transparent
    val textColor = if (selected) Color.White else categoryColor
    val borderStroke = if (selected) {
        null
    } else {
        BorderStroke(
            width = 1.dp,
            color = categoryColor,
        )
    }

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = Shapes.extraSmall,
        color = chipColor,
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
            categoryColor = Color(0xFFFFD43B),
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
            categoryColor = Color(0xFFFFD43B),
        )
    }
}
