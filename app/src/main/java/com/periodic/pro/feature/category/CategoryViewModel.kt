package com.periodic.pro.feature.category

import androidx.lifecycle.ViewModel
import com.periodic.pro.data.element.ElementRepository
import com.periodic.pro.data.element.model.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Category 屏 ViewModel。
 *
 * 管理分类入口 Grid 和分类详情列表状态。
 * 通过 [ElementRepository] 获取元素数据并按分类筛选。
 */
class CategoryViewModel(
    private val elementRepo: ElementRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryUiState())
    val state: StateFlow<CategoryUiState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val allElements = elementRepo.getAll()
        val zhMap = allElements.associate { element ->
            element.atomicNumber to (elementRepo.getZh(element.atomicNumber)?.nameZh ?: element.name)
        }
        val elementCounts = Category.entries.associateWith { category ->
            allElements.count { it.category == category }
        }

        _state.update {
            it.copy(
                elementCounts = elementCounts,
                zhMap = zhMap,
                isLoading = false,
            )
        }
    }

    fun handle(intent: CategoryIntent) {
        when (intent) {
            is CategoryIntent.SelectCategory -> selectCategory(intent.category)
            is CategoryIntent.NavigateBack -> clearSelection()
        }
    }

    private fun selectCategory(category: Category) {
        val elements = elementRepo.getAll().filter { it.category == category }
        _state.update {
            it.copy(
                selectedCategory = category,
                categoryElements = elements,
            )
        }
    }

    private fun clearSelection() {
        _state.update {
            it.copy(
                selectedCategory = null,
                categoryElements = emptyList(),
            )
        }
    }
}
