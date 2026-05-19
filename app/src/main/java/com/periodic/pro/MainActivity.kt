package com.periodic.pro

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.periodic.pro.data.update.ApkInstaller
import com.periodic.pro.data.update.UpdateService
import com.periodic.pro.feature.compare.CompareScreen
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
                RootNav(context = this@MainActivity)
            }
        }
    }
}

/**
 * 根导航宿主 — 两层架构：
 * - "main" 路由：带底部 Tab 的主框架
 * - "compare/{ids}" 路由：全屏二级页面（无 Tab，带滑动动画）
 */
@Composable
private fun RootNav(context: Context) {
    val rootNavController = rememberNavController()

    NavHost(
        navController = rootNavController,
        startDestination = "main",
    ) {
        composable("main") {
            AutoUpdateHost(context = context) {
                PeriodicNavSuite(rootNavController = rootNavController)
            }
        }

        composable(
            route = "compare?ids={ids}",
            arguments = listOf(navArgument("ids") { type = NavType.StringType; defaultValue = "" }),
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) },
        ) { backStackEntry ->
            val ids = (backStackEntry.arguments?.getString("ids") ?: "")
                .split(",").mapNotNull { it.toIntOrNull() }
            CompareScreen(
                ids = ids,
                onNavigateBack = { rootNavController.popBackStack() },
                onNavigateToTable = {
                    rootNavController.popBackStack("main", inclusive = false)
                },
            )
        }
    }
}

/**
 * 自动更新宿主。
 */
@Composable
private fun AutoUpdateHost(
    context: Context,
    content: @Composable () -> Unit,
) {
    val updateService = remember { get<UpdateService>(UpdateService::class.java) }
    val apkInstaller = remember { get<ApkInstaller>(ApkInstaller::class.java) }
    val updateState by updateService.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000L)
        updateService.onAppStart(BuildConfig.VERSION_NAME)
    }

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

        if (updateState.shouldShowDialog && updateState.result is com.periodic.pro.data.update.UpdateResult.Available) {
            val result = updateState.result as com.periodic.pro.data.update.UpdateResult.Available
            UpdateDialog(
                release = result.release,
                currentVersion = result.currentVersion,
                downloadProgress = updateState.downloadProgress,
                downloadFailed = updateState.downloadFailed,
                onCancelDownload = { apkInstaller.cancelDownload() },
                onDismiss = { updateService.dismissDialog() },
                onSnooze = { updateService.snooze() },
                onSkipVersion = {
                    updateService.skipVersion(result.release.tagName.removePrefix("v"))
                },
                onUpdate = {
                    updateService.clearDownloadFailed()
                    apkInstaller.downloadAndInstall(result.release) { progress ->
                        updateService.setDownloadProgress(progress)
                    }
                },
            )
        }
    }
}
