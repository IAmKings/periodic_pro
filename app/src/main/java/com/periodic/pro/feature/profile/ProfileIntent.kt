package com.periodic.pro.feature.profile

import com.periodic.pro.data.theme.ThemeMode

/**
 * Profile 屏用户意图。
 */
sealed interface ProfileIntent {
    /** 设置主题模式 */
    data class SetThemeMode(val mode: ThemeMode) : ProfileIntent
}
