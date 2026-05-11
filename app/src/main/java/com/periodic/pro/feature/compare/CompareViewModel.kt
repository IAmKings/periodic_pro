package com.periodic.pro.feature.compare

import androidx.lifecycle.ViewModel
import com.periodic.pro.data.element.ElementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CompareViewModel(
    private val elementRepo: ElementRepository,
    private val ids: List<Int>,
) : ViewModel() {

    private val _state = MutableStateFlow(CompareUiState())
    val state: StateFlow<CompareUiState> = _state.asStateFlow()

    init {
        loadElements()
    }

    private fun loadElements() {
        if (ids.isEmpty()) {
            _state.update { it.copy(isEmpty = true, isLoading = false) }
            return
        }

        val elements = ids.mapNotNull { elementRepo.getByNumber(it) }
        val zhMap = mutableMapOf<Int, String>()
        elements.forEach { el ->
            elementRepo.getZh(el.atomicNumber)?.let { zh ->
                zhMap[el.atomicNumber] = zh.nameZh
            }
        }
        _state.update {
            it.copy(
                elements = elements,
                zhMap = zhMap,
                isLoading = false,
                isEmpty = false,
            )
        }
    }

    fun handle(intent: CompareIntent) {
        // Navigation handled by callback in Screen layer
    }
}
