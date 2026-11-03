package com.periodic.pro.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import com.periodic.pro.theme.CategoryAlpha
import com.periodic.pro.theme.PeriodicProTheme

/**
 * 图标强调位 — 分类色/主色 12% 圆底 + 全值色图标（DESIGN.md §8.7）。
 *
 * @param icon 图标
 * @param tint 强调色（分类色或 primary）
 * @param modifier Modifier
 * @param size 整体尺寸（默认 40dp，图标约一半）
 */
@Composable
fun IconBadge(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = size / 2,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(tint.copy(alpha = CategoryAlpha.BADGE), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(iconSize),
        )
    }
}

/**
 * 文本强调位 — 与 [IconBadge] 同构，用于放元素序号/符号。
 */
@Composable
fun TextBadge(
    text: String,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleSmall,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(tint.copy(alpha = CategoryAlpha.BADGE), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = style,
            color = tint,
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun IconBadgePreview() {
    PeriodicProTheme {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IconBadge(
                icon = Icons.Filled.School,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
