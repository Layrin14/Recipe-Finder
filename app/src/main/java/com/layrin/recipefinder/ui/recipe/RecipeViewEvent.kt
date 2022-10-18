package com.layrin.recipefinder.ui.recipe

sealed interface RecipeViewEvent {
    object SaveRecipe: RecipeViewEvent
}