package com.periodic.pro.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.periodic.pro.R
import com.periodic.pro.feature.table.TableScreen
import com.periodic.pro.ui.components.GlassSurface

@Composable
fun PeriodicNav(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier,
    ) {
        composable(Routes.HOME) {
            PlaceholderScreen(title = stringResource(R.string.screen_home), showGlass = true)
        }
        composable(Routes.TABLE) {
            TableScreen(
                onNavigateToDetail = { atomicNumber ->
                    navController.navigate(Routes.detail(atomicNumber))
                },
                onNavigateToCompare = { ids ->
                    if (ids.isNotEmpty()) {
                        navController.navigate(Routes.compare(ids))
                    }
                },
            )
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("atomicNumber") { type = NavType.IntType }),
        ) { backStackEntry ->
            val atomicNumber = backStackEntry.arguments?.getInt("atomicNumber") ?: 0
            PlaceholderScreen(title = stringResource(R.string.screen_detail, atomicNumber))
        }
        composable(
            route = Routes.COMPARE,
            arguments = listOf(navArgument("ids") {
                type = NavType.StringType
                defaultValue = ""
            }),
        ) { backStackEntry ->
            val ids = (backStackEntry.arguments?.getString("ids") ?: "")
                .split(",")
                .mapNotNull { it.toIntOrNull() }
            PlaceholderScreen(title = "Compare: ${ids.joinToString(", ")}")
        }
        composable(Routes.FAVORITES) {
            PlaceholderScreen(title = stringResource(R.string.screen_favorites))
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    showGlass: Boolean = false,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (showGlass) {
            GlassSurface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(0.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = title)
                }
            }
        } else {
            Text(text = title)
        }
    }
}
