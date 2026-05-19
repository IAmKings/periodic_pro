package com.periodic.pro.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * APK 下载安装器。
 *
 * 应用内 HttpURLConnection 下载，实时回调进度，下载完成后 FileProvider 安装。
 */
class ApkInstaller(
    private val context: Context,
) {

    /** 权限未就绪时缓存待下载的 Release */
    private var pendingRelease: GitHubRelease? = null

    @Volatile
    private var cancelled = false

    /** 当前下载 ID，用于回调去重 */
    @Volatile
    private var currentDownloadId = 0

    /**
     * 下载并安装 APK。
     *
     * @param release GitHub Release 信息
     * @param onProgress 进度回调 (0f-1f)
     * @return true=已开始下载，false=权限未就绪已跳设置
     */
    fun downloadAndInstall(
        release: GitHubRelease,
        onProgress: (Float) -> Unit,
    ): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                Log.w(TAG, "Install permission not granted, caching and showing settings")
                pendingRelease = release
                val settingsIntent = Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${context.packageName}"),
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(settingsIntent)
                return false
            }
        }

        val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk") } ?: run {
            Log.e(TAG, "No APK asset found in release: ${release.tagName}")
            return false
        }

        val versionTag = release.tagName.removePrefix("v")
        val fileName = "PeriodicPro-v$versionTag.apk"
        val apkFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            fileName,
        )

        if (apkFile.exists()) apkFile.delete()

        cancelled = false
        val downloadId = ++currentDownloadId
        Thread {
            var conn: HttpURLConnection? = null
            var input: java.io.InputStream? = null
            var output: FileOutputStream? = null
            try {
                if (downloadId != currentDownloadId) return@Thread
                conn = URL(apkAsset.browserDownloadUrl).openConnection() as HttpURLConnection
                conn.apply {
                    requestMethod = "GET"
                    connectTimeout = 30_000
                    readTimeout = 60_000
                    setRequestProperty("User-Agent", "PeriodicPro/$versionTag")
                }
                val responseCode = conn.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Download failed: HTTP $responseCode")
                    if (downloadId == currentDownloadId) onProgress(-1f)
                    return@Thread
                }
                input = conn.inputStream
                output = FileOutputStream(apkFile)

                val total = conn.contentLength.toLong()
                val buffer = ByteArray(8192)
                var downloaded = 0L
                var bytes: Int

                while (input!!.read(buffer).also { bytes = it } != -1) {
                    if (cancelled) {
                        Log.w(TAG, "Download cancelled")
                        if (downloadId == currentDownloadId) onProgress(-1f)
                        return@Thread
                    }
                    output!!.write(buffer, 0, bytes)
                    downloaded += bytes
                    if (total > 0 && downloadId == currentDownloadId) {
                        onProgress(downloaded.toFloat() / total.toFloat())
                    }
                }
                output!!.flush()
                output!!.fd.sync()
                Log.d(TAG, "Download completed: $fileName (${apkFile.length()} bytes)")
                installApk(apkFile)
            } catch (e: Exception) {
                Log.e(TAG, "Download failed", e)
                if (downloadId == currentDownloadId) onProgress(-1f)
            } finally {
                try { output?.close() } catch (_: Exception) {}
                try { input?.close() } catch (_: Exception) {}
                try { conn?.disconnect() } catch (_: Exception) {}
                if (cancelled) apkFile.delete()
            }
        }.start()

        return true
    }

    fun tryResumePending(): Boolean {
        val release = pendingRelease ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) return false
        }
        pendingRelease = null
        return downloadAndInstall(release) { /* 恢复下载不使用进度回调 */ }
    }

    fun cancelDownload() {
        cancelled = true
        currentDownloadId++ // 阻止旧线程的回调污染
    }

    fun hasPending(): Boolean = pendingRelease != null

    private fun installApk(apkFile: File) {
        if (!apkFile.exists()) {
            Log.e(TAG, "APK file not found: ${apkFile.absolutePath}")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val settingsIntent = Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${context.packageName}"),
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(settingsIntent)
                return
            }
        }

        val apkUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile,
        )

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(installIntent)
        Log.d(TAG, "Install intent launched for: ${apkFile.name}")
    }

    companion object {
        private const val TAG = "ApkInstaller"
    }
}
