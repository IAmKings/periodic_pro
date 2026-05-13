package com.periodic.pro.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import java.util.Locale
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.periodic.pro.R
import com.periodic.pro.data.update.GitHubRelease
import com.periodic.pro.data.update.Asset
import com.periodic.pro.theme.Dimensions
import com.periodic.pro.theme.PeriodicProTheme

/**
 * 更新弹窗。
 *
 * 显示新版本信息、当前版本、文件大小和 Release 说明。
 * 提供「稍后再说」和「立即更新」两个操作按钮。
 *
 * @param release GitHub Release 信息
 * @param currentVersion 当前版本号
 * @param onDismiss 关闭弹窗
 * @param onUpdate 立即更新
 * @param modifier Modifier
 */
@Composable
fun UpdateDialog(
    release: GitHubRelease,
    currentVersion: String,
    onDismiss: () -> Unit,
    onUpdate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val apkSize = release.assets
        .firstOrNull { it.name.endsWith(".apk") }
        ?.let { formatFileSize(it.size) }
        ?: ""

    // Release 说明取前 5 行
    val releaseNotes = release.body
        .lineSequence()
        .take(5)
        .joinToString("\n")

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Text(
                text = stringResource(R.string.update_dialog_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                // 版本信息
                Text(
                    text = stringResource(R.string.update_dialog_new_version, release.tagName),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(modifier = Modifier.height(Dimensions.Dp8))

                Text(
                    text = stringResource(R.string.update_dialog_current_version, currentVersion),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (apkSize.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.update_dialog_file_size, apkSize),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(Dimensions.Dp16))

                // Release 说明
                Text(
                    text = stringResource(R.string.update_dialog_release_notes),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(Dimensions.Dp8))

                Text(
                    text = releaseNotes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            PeriodicButton(
                onClick = onUpdate,
                text = stringResource(R.string.update_dialog_confirm),
            )
        },
        dismissButton = {
            PeriodicOutlinedButton(
                onClick = onDismiss,
                text = stringResource(R.string.update_dialog_dismiss),
            )
        },
    )
}

/**
 * 格式化文件大小。
 */
private fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) {
        String.format(Locale.ROOT, "%.1f MB", mb)
    } else {
        String.format(Locale.ROOT, "%.0f KB", kb)
    }
}

// ========== Preview ==========

private val previewRelease = GitHubRelease(
    tagName = "v0.2.0",
    name = "Periodic Pro v0.2.0",
    htmlUrl = "https://github.com/IAmKings/periodic_pro/releases/tag/v0.2.0",
    body = "- 修复了已知问题\n- 新增了自动更新功能\n- 优化了性能表现",
    prerelease = false,
    assets = listOf(
        Asset(
            name = "PeriodicPro-v0.2.0.apk",
            browserDownloadUrl = "https://github.com/IAmKings/periodic_pro/releases/download/v0.2.0/PeriodicPro-v0.2.0.apk",
            size = 3_200_000L,
        ),
    ),
)

@Preview(name = "Light", showBackground = true)
@Preview(
    name = "Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun UpdateDialogPreview() {
    PeriodicProTheme {
        UpdateDialog(
            release = previewRelease,
            currentVersion = "0.1.1",
            onDismiss = {},
            onUpdate = {},
        )
    }
}
