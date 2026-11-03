package com.periodic.pro.feature.profile

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.periodic.pro.BuildConfig
import com.periodic.pro.R
import com.periodic.pro.data.theme.ThemeMode
import com.periodic.pro.data.update.UpdateResult
import com.periodic.pro.theme.Dimensions
import com.periodic.pro.theme.PeriodicProTheme
import com.periodic.pro.ui.components.IconBadge
import com.periodic.pro.ui.components.PeriodicButton
import com.periodic.pro.ui.components.PeriodicTopBar
import com.periodic.pro.ui.components.SectionHeader
import com.periodic.pro.ui.components.UpdateDialog
import org.koin.androidx.compose.koinViewModel

/**
 * Profile 屏 — Spectra 分组卡布局（DESIGN.md §9.6）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ProfileViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val onIntent = viewModel::handle
    val upToDateMessage = stringResource(R.string.update_up_to_date)
    val context = LocalContext.current

    // 处理一次性效果：Snackbar 显示
    LaunchedEffect(state.updateResult) {
        val result = state.updateResult
        if (result == null) return@LaunchedEffect
        val message = when (result) {
            is UpdateResult.UpToDate -> upToDateMessage
            is UpdateResult.Error ->
                context.getString(R.string.update_check_error, result.message)
            is UpdateResult.Available -> null
        }
        if (message != null) {
            snackbarHostState.showSnackbar(message = message)
            onIntent(ProfileIntent.ClearUpdateResult)
        }
        // Available 时不自动清除——由 UpdateDialog 的 dismiss/snooze/skip 回调处理
    }

    // UpdateDialog
    val updateResult = state.updateResult
    if (updateResult is UpdateResult.Available) {
        val currentVersion = updateResult.currentVersion
        val release = updateResult.release
        UpdateDialog(
            release = release,
            currentVersion = currentVersion,
            downloadProgress = state.downloadProgress,
            downloadFailed = state.downloadFailed,
            onCancelDownload = {
                viewModel.cancelDownload()
                onIntent(ProfileIntent.ClearUpdateResult)
            },
            onDismiss = { onIntent(ProfileIntent.ClearUpdateResult) },
            onSnooze = { onIntent(ProfileIntent.ClearUpdateResult) },
            onSkipVersion = {
                viewModel.skipVersion(release.tagName.removePrefix("v"))
                onIntent(ProfileIntent.ClearUpdateResult)
            },
            onUpdate = {
                onIntent(ProfileIntent.DownloadAndInstall(release))
            },
        )
    }

    ProfileContent(
        state = state,
        onIntent = onIntent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

/**
 * Profile 屏纯 UI 层：分组卡 + 组内发丝分隔。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    state: ProfileUiState,
    onIntent: (ProfileIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            PeriodicTopBar(title = stringResource(R.string.tab_settings))
        },
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimensions.Dp16),
        ) {
            // === 1. 主题切换组 ===
            SectionHeader(
                title = stringResource(R.string.profile_theme),
                modifier = Modifier.padding(top = Dimensions.Dp8, bottom = Dimensions.Dp12),
            )
            SettingsCard {
                ThemeSegmented(
                    currentMode = state.themeMode,
                    onModeChange = { mode -> onIntent(ProfileIntent.SetThemeMode(mode)) },
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.Dp24))

            // === 2. 更新检查组 ===
            SectionHeader(
                title = stringResource(R.string.profile_check_update),
                modifier = Modifier.padding(bottom = Dimensions.Dp12),
            )
            SettingsCard {
                UpdateSection(
                    isChecking = state.isChecking,
                    isDownloading = state.isDownloading,
                    hasNewVersion = state.hasNewVersion,
                    onCheckUpdate = { onIntent(ProfileIntent.CheckUpdate) },
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.Dp24))

            // === 3. 关于组 ===
            SectionHeader(
                title = stringResource(R.string.profile_about),
                modifier = Modifier.padding(bottom = Dimensions.Dp12),
            )
            SettingsCard {
                AboutSection()
            }

            Spacer(modifier = Modifier.height(Dimensions.Dp24))

            // === 4. 数据署名组 ===
            SectionHeader(
                title = stringResource(R.string.profile_attribution),
                modifier = Modifier.padding(bottom = Dimensions.Dp12),
            )
            SettingsCard {
                AttributionSection()
            }

            Spacer(modifier = Modifier.height(Dimensions.Dp32))
        }
    }
}

/** 分组设置卡：surface + 发丝描边 + 16dp 内边距 */
@Composable
private fun SettingsCard(
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = Dimensions.Dp0,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(modifier = Modifier.padding(Dimensions.Dp16)) {
            content()
        }
    }
}

/**
 * 主题三段选择（Segmented）：跟随系统 / 浅色 / 深色。
 */
@Composable
private fun ThemeSegmented(
    currentMode: ThemeMode,
    onModeChange: (ThemeMode) -> Unit,
) {
    val options = listOf(
        Triple(ThemeMode.SYSTEM, Icons.Filled.Visibility, stringResource(R.string.profile_theme_system)),
        Triple(ThemeMode.LIGHT, Icons.Filled.LightMode, stringResource(R.string.profile_theme_light)),
        Triple(ThemeMode.DARK, Icons.Filled.DarkMode, stringResource(R.string.profile_theme_dark)),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
    ) {
        options.forEach { (mode, icon, label) ->
            val selected = currentMode == mode
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            Color.Transparent
                        }
                    )
                    .clickable { onModeChange(mode) }
                    .padding(vertical = Dimensions.Dp8),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                Spacer(modifier = Modifier.width(Dimensions.Dp4))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * 更新检查区。
 */
@Composable
private fun UpdateSection(
    isChecking: Boolean,
    isDownloading: Boolean,
    hasNewVersion: Boolean,
    onCheckUpdate: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBadge(
                icon = Icons.Filled.Refresh,
                tint = MaterialTheme.colorScheme.primary,
                size = Dimensions.Dp32,
                iconSize = 16.dp,
            )
            Spacer(modifier = Modifier.width(Dimensions.Dp12))
            if (isDownloading) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.profile_downloading_update),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(Dimensions.Dp8))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else if (isChecking) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.profile_checking_update),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(Dimensions.Dp8))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                Text(
                    text = if (hasNewVersion) {
                        stringResource(R.string.profile_upgrade)
                    } else {
                        stringResource(R.string.profile_version, BuildConfig.VERSION_NAME)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(Dimensions.Dp12))
                PeriodicButton(
                    onClick = onCheckUpdate,
                    text = stringResource(R.string.profile_check_update),
                )
            }
        }
    }
}

/**
 * 关于区：App 图标 + 名称 + 版本（tnum）+ 技术栈标签。
 */
@Composable
private fun AboutSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        // App 图标 + 名称 + 版本
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(Dimensions.Dp56)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(Dimensions.Dp12),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier.size(Dimensions.Dp48),
                )
            }

            Spacer(modifier = Modifier.width(Dimensions.Dp16))

            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.profile_version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimensions.Dp16))

        // 技术栈标签
        Text(
            text = stringResource(R.string.profile_tech_stack),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(Dimensions.Dp8))

        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Dp8),
        ) {
            TechTag("Kotlin")
            TechTag("Compose")
            TechTag("M3")
            TechTag("Koin")
            TechTag("DataStore")
        }
    }
}

/**
 * 技术栈标签 Chip：tonal 底。
 */
@Composable
private fun TechTag(
    text: String,
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                horizontal = Dimensions.Dp8,
                vertical = Dimensions.Dp4,
            ),
        )
    }
}

/**
 * 数据署名区：CC BY-SA 3.0 + Wikipedia 链接说明。
 */
@Composable
private fun AttributionSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.profile_attribution_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(Dimensions.Dp8))

        Text(
            text = stringResource(R.string.profile_attribution_license),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ========== Preview ==========

@Preview(name = "Light", showBackground = true)
@Preview(
    name = "Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ProfileContentPreview() {
    PeriodicProTheme {
        ProfileContent(
            state = ProfileUiState(
                themeMode = ThemeMode.SYSTEM,
            ),
            onIntent = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
