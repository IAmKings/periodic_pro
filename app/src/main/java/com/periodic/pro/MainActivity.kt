package com.periodic.pro

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.periodic.pro.BuildConfig
import com.periodic.pro.data.update.ApkInstaller
import com.periodic.pro.data.update.GitHubRelease
import com.periodic.pro.data.update.UpdateService
import com.periodic.pro.theme.PeriodicProTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val updateService = remember { get<UpdateService>(UpdateService::class.java) }
    val apkInstaller = remember { get<ApkInstaller>(ApkInstaller::class.java) }
    val updateState by updateService.state.collectAsStateWithLifecycle()

    // 启动时自动检查
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000L)
        updateService.onAppStart(BuildConfig.VERSION_NAME)
    }

    // 从设置页返回后恢复挂起的下载任务
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                apkInstaller.tryResumePending()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        content()

        // 全局弹窗
        if (updateState.shouldShowDialog && updateState.result is com.periodic.pro.data.update.UpdateResult.Available) {
            val result = updateState.result as com.periodic.pro.data.update.UpdateResult.Available
            UpdateDialog(
                release = result.release,
                currentVersion = result.currentVersion,
                onDismiss = { updateService.dismissDialog() },
                onSnooze = { updateService.snooze() },
                onSkipVersion = {
                    updateService.skipVersion(result.release.tagName.removePrefix("v"))
                },
                onUpdate = {
                    updateService.dismissDialog()
                    apkInstaller.downloadAndInstall(result.release)
                },
            )
        }
    }
}
