package com.periodic.pro.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.periodic.pro.BuildConfig
import com.periodic.pro.data.theme.ThemeMode
import com.periodic.pro.data.theme.ThemePreferenceRepository
import com.periodic.pro.data.update.ApkInstaller
import com.periodic.pro.data.update.GitHubRelease
import com.periodic.pro.data.update.UpdateService
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
    private val updateService: UpdateService,
    private val apkInstaller: ApkInstaller,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        observeTheme()
        observeUpdateService()
    }

    private fun observeTheme() {
        viewModelScope.launch {
            themeRepo.themeMode.collect { mode ->
                _state.update { it.copy(themeMode = mode) }
            }
        }
    }

    private fun observeUpdateService() {
        viewModelScope.launch {
            updateService.state.collect { serviceState ->
                _state.update {
                    it.copy(
                        isChecking = serviceState.isChecking,
                        updateResult = serviceState.result,
                        hasNewVersion = serviceState.hasNewVersion,
                        downloadProgress = if (serviceState.downloadProgress >= 0f) serviceState.downloadProgress else it.downloadProgress,
                    )
                }
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
        updateService.checkUpdate(BuildConfig.VERSION_NAME)
    }

    private fun clearUpdateResult() {
        // 只清本地 result，不碰全局 UpdateService（否则会导致 MainActivity 弹窗消失）
        _state.update { it.copy(updateResult = null) }
    }

    fun skipVersion(version: String) {
        updateService.skipVersion(version)
    }

    private fun downloadAndInstall(release: GitHubRelease) {
        _state.update { it.copy(isDownloading = true, downloadProgress = 0f) }
        apkInstaller.downloadAndInstall(release) { progress ->
            // >=1f 完成，-1f 失败 → 重置状态
            val done = progress >= 1f || progress == -1f
            _state.update {
                it.copy(
                    downloadProgress = progress,
                    isDownloading = !done,
                )
            }
            updateService.setDownloadProgress(progress)
        }
    }
}
