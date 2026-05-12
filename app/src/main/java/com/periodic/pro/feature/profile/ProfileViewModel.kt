package com.periodic.pro.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.periodic.pro.data.theme.ThemeMode
import com.periodic.pro.data.theme.ThemePreferenceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Profile 屏 ViewModel。
 *
 * 管理主题模式切换，读取 ThemePreferenceRepository 实现即时生效。
 */
class ProfileViewModel(
    private val themeRepo: ThemePreferenceRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        observeTheme()
    }

    private fun observeTheme() {
        viewModelScope.launch {
            themeRepo.themeMode.collect { mode ->
                _state.update { it.copy(themeMode = mode) }
            }
        }
    }

    fun handle(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.SetThemeMode -> setThemeMode(intent.mode)
        }
    }

    private fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themeRepo.setThemeMode(mode)
        }
    }
}
