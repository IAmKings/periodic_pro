package com.periodic.pro.data.update

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 更新检查全局状态。
 */
data class UpdateState(
    val isChecking: Boolean = false,
    val result: UpdateResult? = null,
    val shouldShowDialog: Boolean = false,
    val hasNewVersion: Boolean = false,
    val downloadProgress: Float = -1f,
    val downloadFailed: Boolean = false,
)

/**
 * 全局更新服务。
 *
 * Koin 单例，管理 GitHub Release 检查、弹窗策略、状态共享。
 * 启动时自动检查，设置页手动检查走并发去重。
 */
class UpdateService(
    private val repository: UpdateRepository,
    private val preferences: UpdatePreferences,
) {

    private val _state = MutableStateFlow(UpdateState())
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var checkJob: Job? = null

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * 应用启动时自动检查更新。
     * 收到同一天内、已跳过版本则不弹窗。
     */
    fun onAppStart(currentVersion: String) {
        scope.launch {
            val today = dateFormat.format(Date())
            val skippedVersion = preferences.skippedVersion.first().orEmpty()
            val lastPromptDate = preferences.lastPromptDate.first().orEmpty()

            // 同一天已提示过 → 跳过
            if (lastPromptDate == today) {
                Log.d(TAG, "Already prompted today, skip auto-check")
                return@launch
            }

            val result = doCheck(currentVersion)

            if (result is UpdateResult.Available) {
                val remoteVersion = result.release.tagName.removePrefix("v")

                // 已跳过此版本 → 不弹窗
                if (remoteVersion == skippedVersion) {
                    Log.d(TAG, "Version $remoteVersion skipped, no dialog")
                    _state.update { it.copy(hasNewVersion = true) }
                    return@launch
                }

                // 弹出全局弹窗
                preferences.setLastPromptDate(today)
                _state.update { it.copy(hasNewVersion = true, shouldShowDialog = true) }
            }
        }
    }

    /**
     * 手动检查更新（设置页调用）。
     * 若全局检查进行中则取消重发；不弹窗，仅返回结果。
     */
    fun checkUpdate(currentVersion: String) {
        checkJob?.cancel()
        scope.launch {
            _state.update { it.copy(isChecking = true, result = null) }

            val today = dateFormat.format(Date())
            val skippedVersion = preferences.skippedVersion.first().orEmpty()

            val result = doCheck(currentVersion)
            val hasNew = result is UpdateResult.Available

            if (hasNew) {
                val remoteVersion = (result as UpdateResult.Available).release.tagName.removePrefix("v")
                if (remoteVersion == skippedVersion) {
                    // 跳过的版本仍返回 Available 但不标为"新版本"
                    _state.update { it.copy(isChecking = false, result = result, hasNewVersion = false) }
                    return@launch
                }
            }

            _state.update { it.copy(isChecking = false, result = result, hasNewVersion = hasNew) }

            // 如果全局弹窗还未展示且未跳过，标记弹窗
            if (hasNew && !_state.value.shouldShowDialog) {
                val remoteVersion = (result as UpdateResult.Available).release.tagName.removePrefix("v")
                if (remoteVersion != skippedVersion && preferences.lastPromptDate.first().orEmpty() != today) {
                    preferences.setLastPromptDate(today)
                    _state.update { it.copy(shouldShowDialog = true) }
                }
            }
        }
    }

    /** 跳过当前版本，永久不再弹窗 */
    fun skipVersion(version: String) {
        scope.launch {
            preferences.setSkippedVersion(version)
            _state.update { it.copy(shouldShowDialog = false, hasNewVersion = false) }
        }
    }

    /** 稍后提示：关闭弹窗，今天不再弹 */
    fun snooze() {
        _state.update { it.copy(shouldShowDialog = false) }
    }

    /** 关闭弹窗（外部点击取消），不清除 hasNewVersion */
    fun dismissDialog() {
        _state.update { it.copy(shouldShowDialog = false) }
    }

    /** 更新下载进度。>=1 关闭弹窗，-1 标记失败 */
    fun setDownloadProgress(progress: Float) {
        val success = progress >= 1f
        val failed = progress == -1f
        _state.update {
            it.copy(
                downloadProgress = -1f,
                downloadFailed = failed,
                shouldShowDialog = if (success) false else it.shouldShowDialog,
                result = if (success) null else it.result,
            )
        }
    }

    /** 清除下载失败状态 */
    fun clearDownloadFailed() {
        _state.update { it.copy(downloadFailed = false) }
    }

    /** 清除手动检查结果 */
    fun clearResult() {
        _state.update { it.copy(result = null) }
    }

    private suspend fun doCheck(currentVersion: String): UpdateResult {
        return repository.checkUpdate(currentVersion)
    }

    companion object {
        private const val TAG = "UpdateService"
    }
}
