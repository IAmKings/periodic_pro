package com.periodic.pro.feature.profile

import android.content.res.Configuration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.periodic.pro.BuildConfig
import com.periodic.pro.R
import com.periodic.pro.data.theme.ThemeMode
import com.periodic.pro.data.update.UpdateResult
import com.periodic.pro.theme.Dimensions
import com.periodic.pro.theme.PeriodicProTheme
import com.periodic.pro.ui.components.PeriodicButton
import com.periodic.pro.ui.components.UpdateDialog
import org.koin.androidx.compose.koinViewModel

/**
 * Profile 屏入口。
 *
 * @param onNavigateBack 返回回调
 * @param modifier Modifier
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
 * Profile 屏纯 UI 层。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    state: ProfileUiState,
    onIntent: (ProfileIntent) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.tab_settings),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.detail_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            // === 1. 主题切换区 ===
            ThemeSection(
                currentMode = state.themeMode,
                onModeChange = { mode -> onIntent(ProfileIntent.SetThemeMode(mode)) },
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = Dimensions.Dp16),
                thickness = 0.5.dp,
            )

            // === 2. 更新检查区 ===
            UpdateSection(
                isChecking = state.isChecking,
                isDownloading = state.isDownloading,
                hasNewVersion = state.hasNewVersion,
                onCheckUpdate = { onIntent(ProfileIntent.CheckUpdate) },
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = Dimensions.Dp16),
                thickness = 0.5.dp,
            )

            // === 3. 关于区 ===
            AboutSection()

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = Dimensions.Dp16),
                thickness = 0.5.dp,
            )

            // === 4. 数据署名区 ===
            AttributionSection()

            Spacer(modifier = Modifier.height(Dimensions.Dp32))
        }
    }
}

/**
 * 主题切换区。
 *
 * 提供三段式选择：跟随系统 / 浅色 / 深色，即时生效。
 */
@Composable
private fun ThemeSection(
    currentMode: ThemeMode,
    onModeChange: (ThemeMode) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimensions.Dp16),
    ) {
        Text(
            text = stringResource(R.string.profile_theme),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(Dimensions.Dp12))

        ThemeOption(
            icon = Icons.Filled.Visibility,
            title = stringResource(R.string.profile_theme_system),
            subtitle = stringResource(R.string.profile_theme_system_desc),
            selected = currentMode == ThemeMode.SYSTEM,
            onClick = { onModeChange(ThemeMode.SYSTEM) },
        )

        Spacer(modifier = Modifier.height(Dimensions.Dp8))

        ThemeOption(
            icon = Icons.Filled.LightMode,
            title = stringResource(R.string.profile_theme_light),
            subtitle = stringResource(R.string.profile_theme_light_desc),
            selected = currentMode == ThemeMode.LIGHT,
            onClick = { onModeChange(ThemeMode.LIGHT) },
        )

        Spacer(modifier = Modifier.height(Dimensions.Dp8))

        ThemeOption(
            icon = Icons.Filled.DarkMode,
            title = stringResource(R.string.profile_theme_dark),
            subtitle = stringResource(R.string.profile_theme_dark_desc),
            selected = currentMode == ThemeMode.DARK,
            onClick = { onModeChange(ThemeMode.DARK) },
        )
    }
}

/**
 * 单行主题选择项：图标 + 标题 + 副标题 + RadioButton。
 */
@Composable
private fun ThemeOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Dimensions.Dp24),
        )

        Spacer(modifier = Modifier.width(Dimensions.Dp16))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        RadioButton(
            selected = selected,
            onClick = onClick,
        )
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimensions.Dp16),
    ) {
        Text(
            text = stringResource(R.string.profile_check_update),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(Dimensions.Dp12))

        if (isDownloading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(Dimensions.Dp8))
            Text(
                text = "更新中...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else if (isChecking) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(Dimensions.Dp8))
            Text(
                text = stringResource(R.string.profile_checking_update),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            PeriodicButton(
                onClick = onCheckUpdate,
                text = if (hasNewVersion) {
                    stringResource(R.string.profile_upgrade)
                } else {
                    stringResource(R.string.profile_check_update)
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * 关于区：App 图标 + 版本号 + 技术栈标签。
 */
@Composable
private fun AboutSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimensions.Dp16),
    ) {
        Text(
            text = stringResource(R.string.profile_about),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(Dimensions.Dp16))

        // App 图标 + 名称 + 版本
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 应用图标
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(Dimensions.Dp56),
            )

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
                    style = MaterialTheme.typography.bodyMedium,
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
 * 技术栈标签 Chip。
 */
@Composable
private fun TechTag(
    text: String,
) {
    Card(
        shape = RoundedCornerShape(Dimensions.Dp8),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimensions.Dp16),
    ) {
        Text(
            text = stringResource(R.string.profile_attribution),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(Dimensions.Dp12))

        Text(
            text = stringResource(R.string.profile_attribution_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(Dimensions.Dp8))

        Text(
            text = stringResource(R.string.profile_attribution_license),
            style = MaterialTheme.typography.bodyMedium,
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
