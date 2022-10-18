package com.layrin.recipefinder.data.model

data class RecipeResponseState(
    val recipe: List<RecipeData>? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)
