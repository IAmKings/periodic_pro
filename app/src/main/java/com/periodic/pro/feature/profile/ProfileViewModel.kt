package com.periodic.pro.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.periodic.pro.BuildConfig
import com.periodic.pro.data.theme.ThemeMode
import com.periodic.pro.data.theme.ThemePreferenceRepository
import com.periodic.pro.data.update.ApkInstaller
import com.periodic.pro.data.update.GitHubRelease
import com.periodic.pro.data.update.UpdateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Profile 屏 ViewModel。
 *
 * 管理主题模式切换与自动更新检查。
 */
class ProfileViewModel(
    private val themeRepo: ThemePreferenceRepository,
    private val updateRepo: UpdateRepository,
    private val apkInstaller: ApkInstaller,
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
            ProfileIntent.CheckUpdate -> checkUpdate()
            ProfileIntent.ClearUpdateResult -> clearUpdateResult()
            is ProfileIntent.DownloadAndInstall -> downloadAndInstall(intent.release)
        }
    }

    private fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themeRepo.setThemeMode(mode)
        }
    }

    private fun checkUpdate() {
        viewModelScope.launch {
            _state.update { it.copy(isChecking = true, updateResult = null) }
            val result = updateRepo.checkUpdate(BuildConfig.VERSION_NAME)
            _state.update { it.copy(isChecking = false, updateResult = result) }
        }
    }

    private fun clearUpdateResult() {
        _state.update { it.copy(updateResult = null) }
    }

    private fun downloadAndInstall(release: GitHubRelease) {
        _state.update { it.copy(updateResult = null) }
        apkInstaller.downloadAndInstall(release)
    }
}
