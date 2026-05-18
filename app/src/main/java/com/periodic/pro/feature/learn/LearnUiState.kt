package com.periodic.pro.feature.learn

import com.periodic.pro.data.element.model.Category
import com.periodic.pro.data.learn.model.LearnItem

enum class LearnNavMode { LIST, DETAIL }

data class LearnUiState(
    val navMode: LearnNavMode = LearnNavMode.LIST,
    val items: List<LearnItem> = emptyList(),
    val selectedItem: LearnItem? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val symbolMap: Map<Int, String> = emptyMap(),
    val categoryMap: Map<Int, Category> = emptyMap(),
    val listScrollIndex: Int = 0,
    val listScrollOffset: Int = 0,
) {
    val isReady: Boolean get() = !isLoading && errorMessage == null
    val coreItems: List<LearnItem> get() = items.filter { it.level == "core" }
    val importantItems: List<LearnItem> get() = items.filter { it.level == "important" }
    val otherItems: List<LearnItem> get() = items.filter { it.level == "other" }
}
