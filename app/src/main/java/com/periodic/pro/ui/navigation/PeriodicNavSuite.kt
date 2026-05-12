package com.periodic.pro.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowWidthSizeClass
import com.periodic.pro.R
import com.periodic.pro.ui.components.AppGlassProvider

/**
 * 导航 suite 条目数据
 */
private data class NavItem(
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String,
)

private val navItems = listOf(
    NavItem(R.string.tab_home, Icons.Filled.Home, Icons.Outlined.Home, Routes.HOME),
    NavItem(R.string.tab_table, Icons.Filled.TableChart, Icons.Outlined.TableChart, Routes.TABLE),
    NavItem(R.string.tab_compare, Icons.Filled.TableChart, Icons.Outlined.TableChart, Routes.COMPARE),
    NavItem(R.string.tab_favorites, Icons.Filled.Star, Icons.Outlined.StarBorder, Routes.FAVORITES),
    NavItem(R.string.tab_settings, Icons.Filled.Settings, Icons.Outlined.Settings, Routes.PROFILE),
)

/**
 * 导航 suite 入口。
 * 使用 NavigationSuiteScaffold + currentWindowAdaptiveInfo() 实现自适应导航：
 * - 手机竖屏：Bottom NavigationBar
 * - 横屏/折叠屏：NavigationRail
 * - 平板（>= 840dp）：NavigationDrawer
 *
 * 计划：升级到 window-core 1.4+ 后，用 `isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)`
 *       替换已 deprecated 的 WindowWidthSizeClass 枚举比较。
 */
@Composable
fun PeriodicNavSuite() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    val adaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo()
    val windowWidthClass = adaptiveInfo.windowSizeClass.windowWidthSizeClass
    val customNavSuiteType = when (windowWidthClass) {
        WindowWidthSizeClass.EXPANDED -> NavigationSuiteType.NavigationDrawer
        else -> NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)
    }

    AppGlassProvider {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                navItems.forEach { item ->
                    val selected = currentDestination?.hierarchy
                        ?.any { it.route == item.route } == true
                    item(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = stringResource(item.labelRes),
                            )
                        },
                        label = { Text(stringResource(item.labelRes)) },
                    )
                }
            },
            layoutType = customNavSuiteType,
        ) {
            PeriodicNav(
                navController = navController,
            )
        }
    }
}
