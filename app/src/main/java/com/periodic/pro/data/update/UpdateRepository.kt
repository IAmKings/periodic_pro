package com.periodic.pro.data.update

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException
import java.net.SocketTimeoutException

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
 * 使用 HttpURLConnection + User-Agent header（GitHub API 强制要求），
 * 请求前预检网络连通性，异常按类型分类返回中文提示。
 */
class UpdateRepository(
    private val context: Context,
) {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * 检测是否有新版本。
     *
     * @param currentVersion 当前版本号，如 "0.2.0"
     * @return UpdateResult
     */
    suspend fun checkUpdate(currentVersion: String): UpdateResult {
        // 网络连通性预检
        if (!isNetworkAvailable()) {
            Log.w(TAG, "No network connectivity")
            return UpdateResult.Error("网络不可用，请检查网络连接")
        }

        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                connection = (URL(GITHUB_API_URL).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 15_000
                    readTimeout = 15_000
                    setRequestProperty("User-Agent", "PeriodicPro/$currentVersion")
                    setRequestProperty("Accept", "application/vnd.github+json")
                }

                val code = connection.responseCode
                if (code != HttpURLConnection.HTTP_OK) {
                    val errorBody = connection.errorStream?.bufferedReader()?.readText() ?: ""
                    val ghMessage = parseGitHubErrorMessage(errorBody)
                    val msg = when (code) {
                        403 -> "GitHub API 拒绝访问（限流或无权限）" +
                            if (ghMessage != null) "：$ghMessage" else ""
                        404 -> "未找到 Release 信息，请检查仓库是否已发布版本"
                        else -> "服务器返回 HTTP $code" +
                            if (ghMessage != null) "：$ghMessage" else ""
                    }
                    Log.w(TAG, "GitHub API $code: $errorBody")
                    return@withContext UpdateResult.Error(msg)
                }

                val body = connection.inputStream.bufferedReader().readText()
                val release = json.decodeFromString<GitHubRelease>(body)

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
            } catch (e: UnknownHostException) {
                Log.e(TAG, "DNS resolution failed: ${GITHUB_API_URL}", e)
                UpdateResult.Error("无法连接到 GitHub 服务器，请检查网络或 DNS 设置")
            } catch (e: SocketTimeoutException) {
                Log.e(TAG, "Connection timeout", e)
                UpdateResult.Error("连接 GitHub 超时，请检查网络环境")
            } catch (e: IOException) {
                Log.e(TAG, "Network I/O error", e)
                UpdateResult.Error(e.message?.let {
                    if (it.contains("403")) "GitHub API 拒绝访问" else "网络请求失败：$it"
                } ?: "网络请求失败")
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error checking update", e)
                UpdateResult.Error("检查更新失败：${e.message ?: "未知错误"}")
            } finally {
                connection?.disconnect()
            }
        }
    }

    /**
     * 检查设备当前网络是否可用。
     */
    private fun isNetworkAvailable(): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return true // 无法获取服务时放过，交由实际请求决定
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: SecurityException) {
            // 缺少 ACCESS_NETWORK_STATE 权限时放过，交由实际请求决定
            true
        }
    }

    /**
     * 解析 GitHub API 返回的 JSON 错误消息。
     *
     * GitHub 错误格式：{"message": "...", "documentation_url": "..."}
     */
    private fun parseGitHubErrorMessage(errorBody: String): String? {
        return try {
            val obj = json.decodeFromString<JsonObject>(errorBody)
            obj["message"]?.jsonPrimitive?.content
        } catch (_: Exception) {
            null
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
