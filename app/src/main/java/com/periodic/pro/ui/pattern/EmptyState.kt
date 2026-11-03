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
import com.periodic.pro.theme.Dimensions
import com.periodic.pro.theme.PeriodicProTheme
import com.periodic.pro.ui.components.IconBadge
import com.periodic.pro.ui.components.PeriodicButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info

/**
 * 空状态 — Spectra 风格（DESIGN.md §8.9）。
 *
 * 图标置于 56dp tonal 圆底（primaryContainer 系）+ 标题 + 副文 + Primary 按钮；
 * 整体居中，上下留白 ≥ 64dp。
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Dimensions.Dp32, vertical = Dimensions.Dp64),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        IconBadge(
            icon = icon,
            tint = MaterialTheme.colorScheme.primary,
            size = Dimensions.Dp56,
            iconSize = Dimensions.Dp24,
        )

        Spacer(modifier = Modifier.height(Dimensions.Dp16))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        if (subtitle != null) {
            Spacer(modifier = Modifier.height(Dimensions.Dp8))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
