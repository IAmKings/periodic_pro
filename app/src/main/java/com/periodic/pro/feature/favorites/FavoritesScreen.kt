package com.periodic.pro.feature.favorites

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.periodic.pro.R
import com.periodic.pro.data.element.model.Category
import com.periodic.pro.data.element.model.Element
import com.periodic.pro.theme.Dimensions
import com.periodic.pro.theme.PeriodicProTheme
import com.periodic.pro.ui.components.ElementTile
import com.periodic.pro.ui.components.PeriodicTopBar
import com.periodic.pro.ui.pattern.EmptyState
import org.koin.androidx.compose.koinViewModel

/**
 * Favorites 屏 — Spectra（DESIGN.md §9.5）。
 * TopBar 计数副标题（tnum）+ 通栏 ElementTile + 左滑删除（errorContainer）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToTable: () -> Unit,
    modifier: Modifier = Modifier,
    vm: FavoritesViewModel = koinViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            PeriodicTopBar(
                title = stringResource(R.string.screen_favorites),
                subtitle = if (!state.isLoading && !state.isEmpty) {
                    stringResource(R.string.favorites_count, state.favorites.size)
                } else {
                    null
                },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.loading),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else if (state.isEmpty) {
            // 空状态
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                EmptyState(
                    icon = Icons.Filled.Star,
                    title = stringResource(R.string.favorites_empty_title),
                    subtitle = stringResource(R.string.favorites_empty_subtitle),
                    actionLabel = stringResource(R.string.favorites_empty_action),
                    onAction = onNavigateToTable,
                )
            }
        } else {
            // 收藏列表
            FavoritesList(
                favorites = state.favorites,
                zhMap = state.zhMap,
                onRemoveFavorite = { vm.handle(FavoritesIntent.RemoveFavorite(it)) },
                onClickElement = { onNavigateToDetail(it) },
                modifier = Modifier.padding(padding),
            )
        }
    }
}

/**
 * 收藏列表内容。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoritesList(
    favorites: List<Element>,
    zhMap: Map<Int, String>,
    onRemoveFavorite: (Int) -> Unit,
    onClickElement: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.Dp8),
        contentPadding = PaddingValues(
            horizontal = Dimensions.Dp16,
            vertical = Dimensions.Dp8,
        ),
    ) {
        items(favorites, key = { it.atomicNumber }) { element ->
            FavoriteElementItem(
                element = element,
                zhName = zhMap[element.atomicNumber],
                onRemove = { onRemoveFavorite(element.atomicNumber) },
                onClick = { onClickElement(element.atomicNumber) },
            )
        }
    }
}

/**
 * 可左滑删除的收藏元素项。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoriteElementItem(
    element: Element,
    zhName: String?,
    onRemove: () -> Unit,
    onClick: () -> Unit,
) {
    var isRemoved by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                isRemoved = true
                onRemove()
                true
            } else {
                false
            }
        },
    )

    // 删除时缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isRemoved) 0.8f else 1f,
        label = "itemScale",
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = Modifier.scale(scale),
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            // 删除背景：errorContainer + onErrorContainer 图标
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.medium,
                    )
                    .padding(end = Dimensions.Dp24),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.favorites_remove),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(Dimensions.Dp24),
                )
            }
        },
    ) {
        ElementTile(
            element = element,
            onClick = onClick,
            zhName = zhName,
            trailingIcon = Icons.Filled.Star,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ===== Preview Data =====

private val previewHydrogen = Element(
    atomicNumber = 1,
    symbol = "H",
    name = "Hydrogen",
    category = Category.NONMETAL,
)
private val previewHelium = Element(
    atomicNumber = 2,
    symbol = "He",
    name = "Helium",
    category = Category.NOBLE_GAS,
)
private val previewIron = Element(
    atomicNumber = 26,
    symbol = "Fe",
    name = "Iron",
    category = Category.TRANSITION_METAL,
)

// ===== Previews =====

@Preview(name = "Light - List", showBackground = true)
@Preview(name = "Dark - List", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoritesListPreview() {
    PeriodicProTheme {
        Scaffold(
            topBar = {
                PeriodicTopBar(
                    title = stringResource(R.string.screen_favorites),
                    subtitle = stringResource(R.string.favorites_count, 3),
                )
            },
        ) { padding ->
            FavoritesList(
                favorites = listOf(previewHydrogen, previewHelium, previewIron),
                zhMap = mapOf(1 to "氢", 2 to "氦", 26 to "铁"),
                onRemoveFavorite = {},
                onClickElement = {},
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Preview(name = "Light - Empty", showBackground = true)
@Preview(name = "Dark - Empty", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoritesEmptyPreview() {
    PeriodicProTheme {
        Scaffold(
            topBar = {
                PeriodicTopBar(title = stringResource(R.string.screen_favorites))
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                EmptyState(
                    icon = Icons.Filled.Star,
                    title = stringResource(R.string.favorites_empty_title),
                    subtitle = stringResource(R.string.favorites_empty_subtitle),
                    actionLabel = stringResource(R.string.favorites_empty_action),
                    onAction = {},
                )
            }
        }
    }
}
