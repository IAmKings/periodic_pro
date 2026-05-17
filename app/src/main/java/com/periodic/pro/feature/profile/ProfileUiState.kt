package com.periodic.pro.feature.profile

import com.periodic.pro.data.theme.ThemeMode
import com.periodic.pro.data.update.UpdateResult

/**
 * Profile 屏 UI 状态。
 *
 * @param themeMode 当前主题模式
 * @param isChecking 是否正在检查更新
 * @param updateResult 更新检查结果
 */
data class ProfileUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isChecking: Boolean = false,
    val isDownloading: Boolean = false,
    val updateResult: UpdateResult? = null,
    val hasNewVersion: Boolean = false,
)
