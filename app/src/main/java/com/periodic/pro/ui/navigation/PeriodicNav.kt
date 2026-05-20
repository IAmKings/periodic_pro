package com.periodic.pro.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.periodic.pro.feature.category.CategoryScreen
import com.periodic.pro.feature.detail.DetailScreen
import com.periodic.pro.feature.discover.DiscoverScreen
import com.periodic.pro.feature.favorites.FavoritesScreen
import com.periodic.pro.feature.home.HomeScreen
import com.periodic.pro.feature.profile.ProfileScreen
import com.periodic.pro.feature.table.TableScreen

private fun NavHostController.navigateRestorable(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavHostController.navigateTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id)
        launchSingleTop = true
    }
}

@Composable
fun PeriodicNav(
    navController: NavHostController,
    rootNavController: NavHostController? = null,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier,
    ) {
        // === Home ===
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToTable = { query ->
                    if (query.isBlank()) navController.navigateTab(Routes.TABLE)
                    else navController.navigateRestorable(Routes.table(query))
                },
                onNavigateToDetail = { atomicNumber ->
                    navController.navigateRestorable(Routes.detail(atomicNumber))
                },
                onNavigateToCompare = {
                    navController.navigateTab("${Routes.TABLE}?enterMultiSelect=true")
                },
                onNavigateToLearn = { rootNavController?.navigate("learn") },
                onNavigateToLab = { rootNavController?.navigate("lab") },
                onNavigateToQuiz = { rootNavController?.navigate("quiz") },
                onNavigateToFavorites = { navController.navigateTab(Routes.FAVORITES) },
            )
        }

        // === Table (周期表) ===
        composable(
            route = "${Routes.TABLE}?query={query}&enterMultiSelect={enterMultiSelect}",
            arguments = listOf(
                navArgument("query") { type = NavType.StringType; defaultValue = "" },
                navArgument("enterMultiSelect") { type = NavType.BoolType; defaultValue = false },
            ),
        ) {
            val query = it.arguments?.getString("query") ?: ""
            val enterMultiSelect = it.arguments?.getBoolean("enterMultiSelect") ?: false
            TableScreen(
                initialQuery = query,
                enterMultiSelect = enterMultiSelect,
                onNavigateToDetail = { atomicNumber ->
                    navController.navigateRestorable(Routes.detail(atomicNumber))
                },
                onNavigateToCompare = { ids ->
                    if (ids.isNotEmpty()) rootNavController?.navigate("compare?ids=${ids.joinToString(",")}")
                },
            )
        }

        // === Detail (元素详情) ===
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("atomicNumber") { type = NavType.IntType }),
        ) { backStackEntry ->
            val atomicNumber = backStackEntry.arguments?.getInt("atomicNumber") ?: 0
            DetailScreen(
                atomicNumber = atomicNumber,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLearn = { num -> rootNavController?.navigate("learn?atomicNumber=$num") },
                onNavigateToDiscover = { navController.navigateTab(Routes.DISCOVER) },
                onNavigateToLab = { _ -> rootNavController?.navigate("lab") },
                onNavigateToLabDetail = { reactionId ->
                    rootNavController?.navigate("lab?reactionId=$reactionId")
                },
            )
        }

        // === Favorites (收藏) ===
        composable(Routes.FAVORITES) {
            FavoritesScreen(
                onNavigateToDetail = { atomicNumber ->
                    navController.navigateRestorable(Routes.detail(atomicNumber))
                },
                onNavigateToTable = { navController.navigateTab(Routes.TABLE) },
            )
        }

        // === Discover (发现) ===
        composable(Routes.DISCOVER) {
            DiscoverScreen(
                onNavigateToDetail = { atomicNumber ->
                    navController.navigateRestorable(Routes.detail(atomicNumber))
                },
            )
        }

        // === Profile (设置) ===
        composable(Routes.PROFILE) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // === Category (分类浏览) ===
        composable(Routes.CATEGORY) {
            CategoryScreen(
                onNavigateToDetail = { atomicNumber ->
                    navController.navigateRestorable(Routes.detail(atomicNumber))
                },
            )
        }

        // === Category Detail (分类详情) ===
        composable(
            route = Routes.CATEGORY_DETAIL,
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            CategoryScreen(
                onNavigateToDetail = { atomicNumber ->
                    navController.navigateRestorable(Routes.detail(atomicNumber))
                },
                initialCategoryId = categoryId,
            )
        }
    }
}
