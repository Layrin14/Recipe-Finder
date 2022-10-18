package com.layrin.recipefinder.ui.saved

import com.layrin.recipefinder.data.model.RecipeData

sealed class SavedRecipeEvent {
    data class DeleteSavedRecipe(val data: List<RecipeData>) : SavedRecipeEvent() {
        fun errorInfo(): String = "Error deleting saved recipe"
    }
}
