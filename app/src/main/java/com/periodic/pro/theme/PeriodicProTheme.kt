package com.periodic.pro.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

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
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val categoryColors = if (darkTheme) DarkCategoryColors else CategoryColors()

    CompositionLocalProvider(LocalCategoryColors provides categoryColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content,
        )
    }
}
