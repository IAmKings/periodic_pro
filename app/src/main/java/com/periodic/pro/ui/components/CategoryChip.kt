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
 * 分类筛选 Chip。
 *
 * 选中态填充 Category 色 + 白色文字，未选中态空心描边。
 * 圆角 shapeExtraSmall(4dp)，高度 32dp。
 *
 * @param text Chip 显示文本（如"全部"/"碱金属"/"过渡金属"）
 * @param selected 是否选中
 * @param onClick 点击回调
 * @param modifier Modifier
 * @param selectedColor 选中时的填充色（"全部"时为 null，用 primary）
 */
@Composable
fun CategoryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color? = null,
) {
    val chipColor = if (selected && selectedColor != null) {
        selectedColor
    } else if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }

    val textColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val borderStroke = if (selected) {
        null
    } else {
        BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline,
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
                horizontal = Dimensions.Dp12,
                vertical = Dimensions.Dp4,
            ),
            style = MaterialTheme.typography.labelMedium,
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
        CategoryChip(text = "碱金属", selected = true, onClick = {}, selectedColor = Color(0xFFFF6B6B))
    }
}
