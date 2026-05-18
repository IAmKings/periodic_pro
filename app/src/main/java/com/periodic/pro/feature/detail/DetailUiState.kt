package com.periodic.pro.feature.detail

import com.periodic.pro.data.element.model.Element

import com.periodic.pro.data.lab.model.ChemicalReaction

data class DetailUiState(
    val element: Element? = null,
    val zhName: String? = null,
    val zhDescription: String? = null,
    val isLoading: Boolean = true,
    val isFavorite: Boolean = false,
    val scrollPosition: Int = 0,
    val reactions: List<ChemicalReaction> = emptyList(),
) {
    val isReady: Boolean get() = element != null && !isLoading
}
