package com.layrin.recipefinder.ui.home

import com.layrin.recipefinder.data.model.RecipeData

sealed class HomeEvent {
    data class AddItemToBookmarkEvent(val data: List<RecipeData>) : HomeEvent() {
        fun errorInfo(): String = "Error adding recipe to bookmark"
    }
}
