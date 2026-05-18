package com.periodic.pro.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.periodic.pro.data.permission.PermissionRequest

/**
 * 权限弹窗 — PermissionDef 数据驱动。
 *
 * 首次弹窗：解释权限用途 + 确认/取消
 * 被拒弹窗：引导到系统设置 + 确认/取消
 *
 * @param request 权限请求数据
 * @param onConfirm 确认（授予权限/去设置）
 * @param onDismiss 取消/关闭
 */
@Composable
fun PermissionDialog(
    request: PermissionRequest,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val def = request.def
    val title = if (request.isPermanentlyDenied) def.deniedTitle else def.title
    val description = if (request.isPermanentlyDenied) def.deniedDescription else def.description
    val confirmText = if (request.isPermanentlyDenied) "去设置" else "允许"

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        icon = {
            Icon(
                imageVector = def.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            PeriodicButton(
                onClick = onConfirm,
                text = confirmText,
            )
        },
        dismissButton = {
            PeriodicOutlinedButton(
                onClick = onDismiss,
                text = "取消",
            )
        },
    )
}
