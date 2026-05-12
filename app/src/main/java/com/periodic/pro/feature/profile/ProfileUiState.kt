package com.periodic.pro.feature.profile

import com.periodic.pro.data.theme.ThemeMode

/**
 * Profile 屏 UI 状态。
 *
 * @param themeMode 当前主题模式
 */
data class ProfileUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)
