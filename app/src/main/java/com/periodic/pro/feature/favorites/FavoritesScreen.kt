package com.periodic.pro.feature.favorites

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.periodic.pro.R
import com.periodic.pro.data.element.model.Element
import com.periodic.pro.theme.Dimensions
import com.periodic.pro.theme.PeriodicProTheme
import com.periodic.pro.ui.components.ElementCard
import com.periodic.pro.ui.pattern.EmptyState
import org.koin.androidx.compose.koinViewModel

/**
 * Favorites 屏入口。
 *
 * @param onNavigateToDetail 导航到元素详情
 * @param onNavigateToTable 导航到周期表
 * @param modifier Modifier
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
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.screen_favorites),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
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
                    icon = Icons.Filled.Favorite,
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
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
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

    // Animate scale when being dismissed
    val scale by animateFloatAsState(
        targetValue = if (isRemoved) 0.8f else 1f,
        label = "itemScale",
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = Modifier.scale(scale),
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            // 删除背景：红色 + 删除图标
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.error,
                        shape = MaterialTheme.shapes.medium,
                    )
                    .padding(end = Dimensions.Dp24),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.favorites_remove),
                    tint = Color.White,
                    modifier = Modifier.size(Dimensions.Dp24),
                )
            }
        },
    ) {
        ElementCard(
            element = element,
            onClick = onClick,
            zhName = zhName,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ===== Preview Data =====

private val previewHydrogen = Element(
    atomicNumber = 1,
    symbol = "H",
    name = "Hydrogen",
    category = "nonmetal",
)
private val previewHelium = Element(
    atomicNumber = 2,
    symbol = "He",
    name = "Helium",
    category = "noble-gas",
)
private val previewIron = Element(
    atomicNumber = 26,
    symbol = "Fe",
    name = "Iron",
    category = "transition-metal",
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
                TopAppBar(
                    title = { Text(stringResource(R.string.screen_favorites)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
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
                TopAppBar(
                    title = { Text(stringResource(R.string.screen_favorites)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                EmptyState(
                    icon = Icons.Filled.Favorite,
                    title = stringResource(R.string.favorites_empty_title),
                    subtitle = stringResource(R.string.favorites_empty_subtitle),
                    actionLabel = stringResource(R.string.favorites_empty_action),
                    onAction = {},
                )
            }
        }
    }
}
