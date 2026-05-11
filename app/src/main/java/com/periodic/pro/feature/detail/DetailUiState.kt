package com.periodic.pro.feature.detail

import com.periodic.pro.data.element.model.Element

data class DetailUiState(
    val element: Element? = null,
    val zhName: String? = null,
    val isLoading: Boolean = true,
    val isFavorite: Boolean = false,
) {
    val isReady: Boolean get() = element != null && !isLoading
}
