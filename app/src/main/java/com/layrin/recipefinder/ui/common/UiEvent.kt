package com.layrin.recipefinder.ui.common

sealed class UiEvent {
    data class ShowSnackBar(val data: String) : UiEvent()
}
