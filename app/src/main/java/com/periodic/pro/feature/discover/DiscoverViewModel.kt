package com.periodic.pro.feature.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.periodic.pro.data.discover.DiscoverRepository
import com.periodic.pro.data.element.ElementRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Discover 屏 ViewModel。
 *
 * 加载所有知识点列表，计算每日推荐，处理卡片点击事件。
 */
class DiscoverViewModel(
    private val discoverRepo: DiscoverRepository,
    private val elementRepo: ElementRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DiscoverUiState())
    val state: StateFlow<DiscoverUiState> = _state.asStateFlow()

    private val _effect = Channel<DiscoverEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        handle(DiscoverIntent.LoadData)
    }

    fun handle(intent: DiscoverIntent) {
        when (intent) {
            DiscoverIntent.LoadData -> loadData()
            is DiscoverIntent.SelectItem -> {
                viewModelScope.launch {
                    _effect.send(DiscoverEffect.NavigateToDetail(intent.atomicNumber))
                }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val items = discoverRepo.loadAll()
                val daily = discoverRepo.getDailyRecommend()
                val elements = elementRepo.getAll()
                val symbolMap = elements.associate { it.atomicNumber to it.symbol }
                val categoryMap = elements.associate { it.atomicNumber to it.category }
                _state.update {
                    it.copy(
                        items = items,
                        dailyRecommend = daily,
                        symbolMap = symbolMap,
                        categoryMap = categoryMap,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "加载失败",
                    )
                }
            }
        }
    }
}
