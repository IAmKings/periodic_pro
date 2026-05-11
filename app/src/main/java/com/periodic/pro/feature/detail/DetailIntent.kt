package com.periodic.pro.feature.detail

sealed interface DetailIntent {
    data object ToggleFavorite : DetailIntent
    data object NavigateBack : DetailIntent
}
