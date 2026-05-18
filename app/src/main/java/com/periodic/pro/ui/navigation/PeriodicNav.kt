package com.periodic.pro.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.navArgument
import com.periodic.pro.feature.category.CategoryScreen
import com.periodic.pro.feature.compare.CompareScreen
import com.periodic.pro.feature.detail.DetailScreen
import com.periodic.pro.feature.discover.DiscoverScreen
import com.periodic.pro.feature.favorites.FavoritesScreen
import com.periodic.pro.feature.home.HomeScreen
import com.periodic.pro.feature.lab.LabScreen
import com.periodic.pro.feature.learn.LearnScreen
import com.periodic.pro.feature.profile.ProfileScreen
import com.periodic.pro.feature.table.TableScreen

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
        // === Home ===
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToTable = { query ->
                    navController.navigate(Routes.table(query)) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToDetail = { atomicNumber ->
                    navController.navigate(Routes.detail(atomicNumber))
                },
                onNavigateToCompare = {
                    navController.navigate(Routes.COMPARE) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToFavorites = {
                    navController.navigate(Routes.FAVORITES) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }

        // === Table (周期表) ===
        composable(
            route = "${Routes.TABLE}?query={query}",
            arguments = listOf(
                navArgument("query") { type = NavType.StringType; defaultValue = "" },
            ),
        ) {
            val query = it.arguments?.getString("query") ?: ""
            TableScreen(
                initialQuery = query,
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

        // === Detail (元素详情) ===
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("atomicNumber") { type = NavType.IntType }),
        ) { backStackEntry ->
            val atomicNumber = backStackEntry.arguments?.getInt("atomicNumber") ?: 0
            DetailScreen(
                atomicNumber = atomicNumber,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLearn = { num ->
                    navController.navigate(Routes.learnDetail(num))
                },
                onNavigateToDiscover = {
                    navController.navigate(Routes.DISCOVER) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }

        // === Compare (元素对比) ===
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
            CompareScreen(
                ids = ids,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTable = {
                    navController.navigate(Routes.TABLE) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }

        // === Favorites (收藏) ===
        composable(Routes.FAVORITES) {
            FavoritesScreen(
                onNavigateToDetail = { atomicNumber ->
                    navController.navigate(Routes.detail(atomicNumber))
                },
                onNavigateToTable = {
                    navController.navigate(Routes.TABLE) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }

        // === Discover (发现) ===
        composable(Routes.DISCOVER) {
            DiscoverScreen(
                onNavigateToDetail = { atomicNumber ->
                    navController.navigate(Routes.detail(atomicNumber))
                },
            )
        }

        // === Profile (设置) ===
        composable(Routes.PROFILE) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // === Learn (学习) ===
        composable(Routes.LEARN) {
            LearnScreen(
                onNavigateToDetail = { atomicNumber ->
                    navController.navigate(Routes.detail(atomicNumber))
                },
            )
        }

        // === Lab (化学实验室) ===
        composable(Routes.LAB) {
            LabScreen(
                onNavigateToDetail = { atomicNumber ->
                    navController.navigate(Routes.detail(atomicNumber))
                },
            )
        }

        // === Category (分类浏览) ===
        composable(Routes.CATEGORY) {
            CategoryScreen(
                onNavigateToDetail = { atomicNumber ->
                    navController.navigate(Routes.detail(atomicNumber))
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
                    navController.navigate(Routes.detail(atomicNumber))
                },
                initialCategoryId = categoryId,
            )
        }
    }
}
