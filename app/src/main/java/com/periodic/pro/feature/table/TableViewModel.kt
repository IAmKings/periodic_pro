package com.periodic.pro.feature.table

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.periodic.pro.data.element.ElementRepository
import com.periodic.pro.data.favorites.FavoritesRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TableViewModel(
    private val elementRepo: ElementRepository,
    private val favoritesRepo: FavoritesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(TableUiState())
    val state: StateFlow<TableUiState> = _state.asStateFlow()

    private val _effect = Channel<TableEffect>(Channel.BUFFERED)
    val effect: Flow<TableEffect> = _effect.receiveAsFlow()

    init {
        handle(TableIntent.LoadElements)
    }

    fun handle(intent: TableIntent) {
        when (intent) {
            TableIntent.LoadElements -> loadElements()
            is TableIntent.Search -> {
                _state.update { it.copy(searchQuery = intent.query) }
            }
            is TableIntent.FilterByCategory -> {
                _state.update { it.copy(selectedCategory = intent.category) }
            }
            is TableIntent.ToggleElement -> toggleElement(intent.atomicNumber)
            TableIntent.EnterMultiSelect -> {
                _state.update { it.copy(isMultiSelectMode = true) }
            }
            TableIntent.ExitMultiSelect -> {
                _state.update { it.copy(isMultiSelectMode = false, selectedIds = emptySet()) }
            }
            TableIntent.ConfirmCompare -> confirmCompare()
            is TableIntent.OnElementClick -> onElementClick(intent.atomicNumber)
            is TableIntent.OnElementLongClick -> onElementLongClick(intent.atomicNumber)
            is TableIntent.SelectSeries -> selectSeries(intent.atomicNumbers)
        }
    }

    private fun loadElements() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val elements = elementRepo.getAll()
            // 通过与 zhMap 合并获取中文名
            val zhMap = elements.mapNotNull { element ->
                elementRepo.getZh(element.atomicNumber)?.let { zh -> element.atomicNumber to zh }
            }.toMap()
            _state.update {
                it.copy(
                    elements = elements,
                    zhMap = zhMap,
                    isLoading = false,
                )
            }
        }
    }

    private fun onElementClick(atomicNumber: Int) {
        val cur = _state.value
        if (cur.isMultiSelectMode) {
            // 多选模式下 toggle
            toggleElement(atomicNumber)
        } else {
            // 普通模式下跳详情
            viewModelScope.launch {
                _effect.send(TableEffect.NavigateToDetail(atomicNumber))
            }
        }
    }

    private fun onElementLongClick(atomicNumber: Int) {
        // 进入多选模式 + 选中该元素（收藏由用户手动操作，不在此触发）
        _state.update {
            it.copy(
                isMultiSelectMode = true,
                selectedIds = it.selectedIds + atomicNumber,
            )
        }
    }

    private fun selectSeries(atomicNumbers: List<Int>) {
        _state.update {
            it.copy(
                isMultiSelectMode = true,
                selectedIds = it.selectedIds + atomicNumbers.toSet(),
            )
        }
    }

    private fun toggleElement(atomicNumber: Int) {
        _state.update { cur ->
            val newSelected = if (atomicNumber in cur.selectedIds) {
                cur.selectedIds - atomicNumber
            } else {
                cur.selectedIds + atomicNumber
            }
            cur.copy(selectedIds = newSelected)
        }
    }

    private fun confirmCompare() {
        val ids = _state.value.selectedIds.toList()
        if (ids.isNotEmpty()) {
            viewModelScope.launch {
                _effect.send(TableEffect.NavigateToCompare(ids))
            }
        }
    }
}
