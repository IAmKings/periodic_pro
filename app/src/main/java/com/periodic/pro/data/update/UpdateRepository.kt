package com.periodic.pro.data.update

import android.util.Log
import kotlinx.serialization.json.Json

/**
 * 更新检查结果。
 */
sealed interface UpdateResult {
    /** 有新版本可用 */
    data class Available(
        val release: GitHubRelease,
        val currentVersion: String,
    ) : UpdateResult

    /** 已是最新版本 */
    data object UpToDate : UpdateResult

    /** 检查出错 */
    data class Error(val message: String) : UpdateResult
}

/**
 * GitHub Release 更新检查仓库。
 *
 * 通过 GitHub API 获取最新 Release 信息，与本地版本号比较。
 * 使用 java.net.URL 进行 HTTP 请求，不引入额外网络库。
 */
class UpdateRepository {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * 检测是否有新版本。
     *
     * @param currentVersion 当前版本号，如 "0.1.1"
     * @return UpdateResult
     */
    suspend fun checkUpdate(currentVersion: String): UpdateResult {
        return try {
            val urlString = GITHUB_API_URL
            val response = java.net.URL(urlString).readText()
            val release = json.decodeFromString<GitHubRelease>(response)

            val remoteTag = release.tagName.removePrefix("v")
            val localTag = currentVersion.removePrefix("v")

            if (isNewerVersion(remoteTag, localTag)) {
                UpdateResult.Available(
                    release = release,
                    currentVersion = currentVersion,
                )
            } else {
                UpdateResult.UpToDate
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check update", e)
            UpdateResult.Error(e.message ?: "检查更新失败")
        }
    }

    /**
     * 语义化版本比较。
     *
     * 支持 major.minor.patch 三段版本号比较。
     * 若 remote > local 返回 true，否则 false。
     */
    internal fun isNewerVersion(remote: String, local: String): Boolean {
        val remoteParts = remote.split(".").mapNotNull { it.toIntOrNull() }
        val localParts = local.split(".").mapNotNull { it.toIntOrNull() }

        val maxLength = maxOf(remoteParts.size, localParts.size)
        var newer = false
        for (i in 0 until maxLength) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r > l) { newer = true; break }
            if (r < l) { break }
        }
        return newer
    }

    companion object {
        private const val TAG = "UpdateRepository"
        private const val GITHUB_API_URL =
            "https://api.github.com/repos/IAmKings/periodic_pro/releases/latest"
    }
}
