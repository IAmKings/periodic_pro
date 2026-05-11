package com.periodic.pro.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.periodic.pro.data.element.ElementRepository
import com.periodic.pro.data.favorites.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Favorites 屏 ViewModel。
 *
 * 监听 favoritesRepo.favoritesFlow 实时刷新收藏列表。
 */
class FavoritesViewModel(
    private val elementRepo: ElementRepository,
    private val favoritesRepo: FavoritesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesUiState())
    val state: StateFlow<FavoritesUiState> = _state.asStateFlow()

    init {
        observeFavorites()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            val allElements = elementRepo.getAll()
            favoritesRepo.favoritesFlow.collect { favoriteIds ->
                val favElements = allElements.filter { it.atomicNumber in favoriteIds }
                val zhMap = favElements.associate { el ->
                    el.atomicNumber to (elementRepo.getZh(el.atomicNumber)?.nameZh ?: "")
                }
                _state.update {
                    it.copy(
                        favorites = favElements,
                        zhMap = zhMap,
                        isLoading = false,
                        isEmpty = favElements.isEmpty(),
                    )
                }
            }
        }
    }

    fun handle(intent: FavoritesIntent) {
        when (intent) {
            is FavoritesIntent.RemoveFavorite -> {
                viewModelScope.launch {
                    favoritesRepo.toggle(intent.atomicNumber)
                }
            }
        }
    }
}
