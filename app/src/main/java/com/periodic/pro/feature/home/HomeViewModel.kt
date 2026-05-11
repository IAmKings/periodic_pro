package com.periodic.pro.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.periodic.pro.data.element.ElementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Home 屏 ViewModel。
 *
 * 加载推荐元素列表（热门元素），管理搜索 query 状态。
 */
class HomeViewModel(
    private val elementRepo: ElementRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        handle(HomeIntent.LoadFeatured)
    }

    fun handle(intent: HomeIntent) {
        when (intent) {
            HomeIntent.LoadFeatured -> loadFeatured()
            is HomeIntent.Search -> _state.update { it.copy(searchQuery = intent.query) }
        }
    }

    private fun loadFeatured() {
        viewModelScope.launch {
            // 热门元素原子序号（化学常用元素）
            val hotNumbers = setOf(1, 2, 6, 8, 12, 13, 14, 16, 17, 19, 20, 26, 29, 47, 79, 80)
            val allElements = elementRepo.getAll()
            val featured = allElements
                .filter { it.atomicNumber in hotNumbers }
                .sortedBy { it.atomicNumber }
            val zhMap = mutableMapOf<Int, String>()
            featured.forEach { el ->
                elementRepo.getZh(el.atomicNumber)?.let { zh ->
                    zhMap[el.atomicNumber] = zh.nameZh
                }
            }
            _state.update {
                it.copy(
                    featuredElements = featured,
                    zhMap = zhMap,
                    isLoading = false,
                )
            }
        }
    }
}
