package com.periodic.pro.feature.profile

import com.periodic.pro.data.theme.ThemeMode
import com.periodic.pro.data.update.GitHubRelease

/**
 * Profile 屏用户意图。
 */
sealed interface ProfileIntent {
    /** 设置主题模式 */
    data class SetThemeMode(val mode: ThemeMode) : ProfileIntent

    /** 检查更新 */
    data object CheckUpdate : ProfileIntent

    /** 清除更新结果（关闭弹窗、Snackbar 等） */
    data object ClearUpdateResult : ProfileIntent

    /** 下载并安装指定 Release 的 APK */
    data class DownloadAndInstall(val release: GitHubRelease) : ProfileIntent
}
