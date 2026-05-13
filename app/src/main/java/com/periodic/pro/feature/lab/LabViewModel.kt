package com.periodic.pro.feature.lab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.periodic.pro.data.lab.LabRepository
import com.periodic.pro.data.lab.model.ReactionLevel
import com.periodic.pro.data.lab.model.ReactionType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Lab 屏 ViewModel。
 *
 * 加载化学反应数据列表和详情，支持按年级/类型筛选，处理列表/详情导航。
 */
class LabViewModel(
    private val labRepo: LabRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LabUiState())
    val state: StateFlow<LabUiState> = _state.asStateFlow()

    private val _effect = Channel<LabEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        handle(LabIntent.LoadData)
    }

    fun handle(intent: LabIntent) {
        when (intent) {
            LabIntent.LoadData -> loadData()
            is LabIntent.FilterByLevel -> filterByLevel(intent.level)
            is LabIntent.FilterByType -> filterByType(intent.type)
            is LabIntent.SelectReaction -> selectReaction(intent.reactionId)
            is LabIntent.NavigateToDetail -> navigateToDetail(intent.atomicNumber)
            LabIntent.BackToList -> backToList()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val reactions = labRepo.loadAll()
                _state.update {
                    it.copy(
                        allReactions = reactions,
                        filteredReactions = reactions,
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

    private fun applyFilters() {
        val currentFilter = _state.value.filter
        val allReactions = _state.value.allReactions

        val filtered = allReactions.filter { reaction ->
            val levelMatch = currentFilter.activeLevel == null ||
                reaction.level == currentFilter.activeLevel
            val typeMatch = currentFilter.activeType == null ||
                reaction.type == currentFilter.activeType
            levelMatch && typeMatch
        }
        _state.update { it.copy(filteredReactions = filtered) }
    }

    private fun filterByLevel(level: ReactionLevel?) {
        _state.update {
            it.copy(
                filter = it.filter.copy(activeLevel = level),
            )
        }
        applyFilters()
    }

    private fun filterByType(type: ReactionType?) {
        _state.update {
            it.copy(
                filter = it.filter.copy(activeType = type),
            )
        }
        applyFilters()
    }

    private fun selectReaction(reactionId: String) {
        viewModelScope.launch {
            try {
                val reaction = labRepo.getById(reactionId)
                _state.update {
                    it.copy(selectedReaction = reaction, navMode = LabNavMode.DETAIL)
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
            _effect.send(LabEffect.NavigateToDetail(atomicNumber))
        }
    }

    private fun backToList() {
        _state.update {
            it.copy(selectedReaction = null, navMode = LabNavMode.LIST)
        }
    }
}
