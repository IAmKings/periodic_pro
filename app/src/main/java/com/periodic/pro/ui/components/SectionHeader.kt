package com.periodic.pro.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.periodic.pro.theme.Dimensions
import com.periodic.pro.theme.PeriodicProTheme

/**
 * 小节标题 — 全应用统一的 Section 头（DESIGN.md §8.5）。
 *
 * titleMedium SemiBold + 可选 trailing 文字动作/自定义插槽。
 *
 * @param title 标题
 * @param modifier Modifier
 * @param subtitle 次级说明（可选）
 * @param actionLabel 尾部动作文案（可选，如"查看全部"）
 * @param onAction 尾部动作回调
 * @param trailing 尾部自定义插槽（优先于 actionLabel）
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            when {
                trailing != null -> trailing()
                actionLabel != null && onAction != null -> {
                    PeriodicTextButton(
                        onClick = onAction,
                        text = actionLabel,
                    )
                }
            }
        }
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(Dimensions.Dp2))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SectionHeaderPreview() {
    PeriodicProTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(title = "精选元素")
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(
                title = "快捷入口",
                subtitle = "学习和探索化学",
                actionLabel = "查看全部",
                onAction = {},
            )
        }
    }
}
