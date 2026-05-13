package com.periodic.pro

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.periodic.pro.BuildConfig
import com.periodic.pro.data.update.ApkInstaller
import com.periodic.pro.data.update.GitHubRelease
import com.periodic.pro.data.update.UpdateRepository
import com.periodic.pro.data.update.UpdateResult
import com.periodic.pro.theme.PeriodicProTheme
import com.periodic.pro.ui.components.UpdateDialog
import com.periodic.pro.ui.navigation.PeriodicNavSuite
import org.koin.java.KoinJavaComponent.get

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            PeriodicProTheme {
                AutoUpdateHost(
                    context = this@MainActivity,
                ) {
                    PeriodicNavSuite()
                }
            }
        }
    }
}

/**
 * 自动更新宿主。
 *
 * 在应用启动后 2 秒自动检查更新（一天最多弹一次），
 * 若有新版本显示 UpdateDialog。
 */
@Composable
private fun AutoUpdateHost(
    context: Context,
    content: @Composable () -> Unit,
) {
    val updateRepo = remember { get<UpdateRepository>(UpdateRepository::class.java) }
    val apkInstaller = remember { get<ApkInstaller>(ApkInstaller::class.java) }

    var updateRelease by remember { mutableStateOf<GitHubRelease?>(null) }
    var currentVersion by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("auto_update", Context.MODE_PRIVATE)
        val lastCheckDate = prefs.getString("last_check_date", "") ?: ""
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())

        // 一天内不重复弹窗
        if (lastCheckDate == today) return@LaunchedEffect

        // 等待 2 秒后检测
        kotlinx.coroutines.delay(2000L)

        val localVersion = BuildConfig.VERSION_NAME
        val result = updateRepo.checkUpdate(localVersion)

        if (result is com.periodic.pro.data.update.UpdateResult.Available) {
            updateRelease = result.release
            currentVersion = result.currentVersion

            // 记录本次检测日期
            prefs.edit().putString("last_check_date", today).apply()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        content()

        updateRelease?.let { release ->
            UpdateDialog(
                release = release,
                currentVersion = currentVersion,
                onDismiss = { updateRelease = null },
                onUpdate = {
                    updateRelease = null
                    apkInstaller.downloadAndInstall(release)
                },
            )
        }
    }
}
