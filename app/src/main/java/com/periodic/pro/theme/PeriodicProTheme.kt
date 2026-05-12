package com.periodic.pro.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.periodic.pro.data.theme.ThemeMode
import com.periodic.pro.data.theme.ThemePreferenceRepository
import org.koin.compose.koinInject

/**
 * 元素分类色 CompositionLocal。
 * 通过 staticCompositionLocalOf 独立注入，不混入 M3 ColorScheme。
 */
val LocalCategoryColors = staticCompositionLocalOf { CategoryColors() }

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    background = LightBackground,
    onBackground = LightOnBackground,
    error = LightError,
    onError = LightOnError,
)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    error = DarkError,
    onError = DarkOnError,
)

@Composable
fun PeriodicProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // 尝试从 Koin 获取 ThemePreferenceRepository；Preview 等场景无 Koin 时 fallback 到参数
    val themeRepo = runCatching { koinInject<ThemePreferenceRepository>() }.getOrNull()
    val themeMode by (
        themeRepo?.themeMode?.collectAsState(ThemeMode.SYSTEM)
            ?: remember { mutableStateOf(ThemeMode.SYSTEM) }
        )

    val effectiveDarkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> darkTheme
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (effectiveDarkTheme) DarkColorScheme else LightColorScheme
    val categoryColors = if (effectiveDarkTheme) DarkCategoryColors else CategoryColors()

    CompositionLocalProvider(LocalCategoryColors provides categoryColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content,
        )
    }
}
