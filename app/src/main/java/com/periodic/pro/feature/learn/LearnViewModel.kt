package com.periodic.pro.feature.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.periodic.pro.data.element.ElementRepository
import com.periodic.pro.data.learn.LearnRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Learn 屏 ViewModel。
 *
 * 加载元素学习资料列表和详情，处理列表/详情导航。
 */
class LearnViewModel(
    private val learnRepo: LearnRepository,
    private val elementRepo: ElementRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LearnUiState())
    val state: StateFlow<LearnUiState> = _state.asStateFlow()

    private val _effect = Channel<LearnEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        handle(LearnIntent.LoadData)
    }

    fun handle(intent: LearnIntent) {
        when (intent) {
            LearnIntent.LoadData -> loadData()
            is LearnIntent.SelectElement -> selectElement(intent.atomicNumber)
            is LearnIntent.NavigateToDetail -> navigateToDetail(intent.atomicNumber)
            is LearnIntent.SaveScroll -> saveScroll(intent.index, intent.offset)
            LearnIntent.BackToList -> backToList()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val items = learnRepo.loadAll()
                val elements = elementRepo.getAll()
                val symbolMap = elements.associate { it.atomicNumber to it.symbol }
                val categoryMap = elements.associate { it.atomicNumber to it.category }
                _state.update {
                    it.copy(items = items, symbolMap = symbolMap, categoryMap = categoryMap, isLoading = false)
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

    private fun selectElement(atomicNumber: Int) {
        viewModelScope.launch {
            try {
                val item = learnRepo.getByAtomicNumber(atomicNumber)
                _state.update {
                    it.copy(selectedItem = item, navMode = LearnNavMode.DETAIL)
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(errorMessage = e.message ?: "加载详情失败")
                }
            }
        }
    }

    private fun navigateToDetail(atomicNumber: Int) {
        viewModelScope.launch {
            _effect.send(LearnEffect.NavigateToDetail(atomicNumber))
        }
    }

    private fun saveScroll(index: Int, offset: Int) {
        _state.update { it.copy(listScrollIndex = index, listScrollOffset = offset) }
    }

    private fun backToList() {
        _state.update { it.copy(selectedItem = null, navMode = LearnNavMode.LIST) }
    }
}
