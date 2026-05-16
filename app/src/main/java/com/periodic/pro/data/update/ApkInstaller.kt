package com.periodic.pro.data.update

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File

/**
 * APK 下载安装器。
 *
 * 职责：
 * 1. 使用 DownloadManager 下载 APK 到公共 Downloads 目录
 * 2. 通过 BroadcastReceiver 监听下载完成
 * 3. 使用 FileProvider 提供 content URI 并调起系统安装器
 */
class ApkInstaller(
    private val context: Context,
) {

    /** 权限未就绪时缓存待下载的 Release，权限就绪后自动继续 */
    private var pendingRelease: GitHubRelease? = null

    /**
     * 下载并安装指定 Release 的 APK。
     *
     * 下载前预检安装未知来源权限，无权限时缓存 Release 并弹引导窗。
     * 调用方在 onResume / LaunchedEffect 中应调用 [tryResumePending]
     * 以在用户授权后自动继续下载。
     *
     * @param release GitHub Release 信息
     * @return true=权限已就绪开始下载，false=权限未就绪已弹引导窗
     */
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    fun downloadAndInstall(release: GitHubRelease): Boolean {
        // 下载前预检：Android 8+ 检查安装未知来源权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                Log.w(TAG, "Install permission not granted, caching release and showing settings")
                pendingRelease = release
                val settingsIntent = Intent(
                    android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${context.packageName}"),
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(settingsIntent)
                return false
            }
        }

        return startDownload(release)
    }

    /**
     * 尝试恢复因权限未就绪而挂起的下载。
     * 应在 Activity onResume 或 LaunchedEffect 中调用。
     *
     * @return true=已恢复下载，false=无挂起任务或权限仍未就绪
     */
    fun tryResumePending(): Boolean {
        val release = pendingRelease ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) return false
        }
        pendingRelease = null
        return startDownload(release)
    }

    /** 是否还有挂起的下载任务 */
    fun hasPending(): Boolean = pendingRelease != null

    private fun startDownload(release: GitHubRelease): Boolean {
        val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk") }
            ?: run {
                Log.e(TAG, "No APK asset found in release: ${release.tagName}")
                return false
            }

        val versionTag = release.tagName.removePrefix("v")
        val fileName = "PeriodicPro-v$versionTag.apk"
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        // 检查是否已下载
        val existingFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName,
        )
        if (existingFile.exists()) {
            existingFile.delete()
        }

        val request = DownloadManager.Request(Uri.parse(apkAsset.browserDownloadUrl))
            .setTitle(context.getString(com.periodic.pro.R.string.update_download_title))
            .setDescription(context.getString(com.periodic.pro.R.string.update_download_desc, release.name))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadId = downloadManager.enqueue(request)
        Log.d(TAG, "Download started: $fileName (id=$downloadId)")

        // 注册下载完成广播
        val downloadReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val receivedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                if (receivedId == downloadId) {
                    Log.d(TAG, "Download completed: $fileName")
                    installApk(existingFile)
                    context.unregisterReceiver(this)
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                downloadReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_EXPORTED,
            )
        } else {
            context.registerReceiver(
                downloadReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            )
        }
    }

    /**
     * 安装已下载的 APK。
     */
    private fun installApk(apkFile: File) {
        if (!apkFile.exists()) {
            Log.e(TAG, "APK file not found: ${apkFile.absolutePath}")
            return
        }

        // Android 8+ 检查安装未知来源权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                // 引导用户到设置页面开启安装权限
                val settingsIntent = Intent(
                    android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
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
