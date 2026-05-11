package com.periodic.pro.feature.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.periodic.pro.data.element.ElementRepository
import com.periodic.pro.data.favorites.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailViewModel(
    private val elementRepo: ElementRepository,
    private val favoritesRepo: FavoritesRepository,
    private val atomicNumber: Int,
) : ViewModel() {

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    init {
        loadElement()
        observeFavorite()
    }

    private fun loadElement() {
        val element = elementRepo.getByNumber(atomicNumber)
        val zhName = elementRepo.getZh(atomicNumber)?.nameZh
        _state.update { it.copy(element = element, zhName = zhName, isLoading = false) }
    }

    private fun observeFavorite() {
        viewModelScope.launch {
            favoritesRepo.isFavorite(atomicNumber).collect { isFav ->
                _state.update { it.copy(isFavorite = isFav) }
            }
        }
    }

    fun handle(intent: DetailIntent) {
        when (intent) {
            DetailIntent.ToggleFavorite -> {
                viewModelScope.launch { favoritesRepo.toggle(atomicNumber) }
            }
            DetailIntent.NavigateBack -> {
                // handled by navigation callback in the Screen layer
            }
        }
    }
}
