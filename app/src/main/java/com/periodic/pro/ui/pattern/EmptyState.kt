package com.periodic.pro.ui.pattern

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.periodic.pro.theme.Dimensions
import com.periodic.pro.theme.PeriodicProTheme
import com.periodic.pro.ui.components.PeriodicButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info

/**
 * 空状态组件。
 *
 * 居中布局：图标 + title + subtitle（可选）+ Button（可选）。
 *
 * @param icon 图标
 * @param title 主标题
 * @param subtitle 副标题（可选）
 * @param actionLabel 按钮文案（可选）
 * @param onAction 按钮点击回调
 * @param modifier Modifier
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimensions.Dp32),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )

        Spacer(modifier = Modifier.height(Dimensions.Dp16))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (subtitle != null) {
            Spacer(modifier = Modifier.height(Dimensions.Dp8))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }

        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(Dimensions.Dp24))
            PeriodicButton(
                onClick = onAction,
                text = actionLabel,
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmptyStatePreview() {
    PeriodicProTheme {
        EmptyState(
            icon = Icons.Filled.Info,
            title = "空状态标题",
            subtitle = "这是副标题描述文字",
            actionLabel = "操作按钮",
            onAction = {},
        )
    }
}
