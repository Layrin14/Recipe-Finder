package com.layrin.recipefinder.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.layrin.recipefinder.data.model.RecipeData
import com.layrin.recipefinder.data.repository.RecipeRepository

@Suppress("unchecked_cast")
class RecipeViewViewModelFactory(
    private val repository: RecipeRepository,
    private val recipeData: RecipeData?
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RecipeViewViewModel(repository, recipeData) as T
    }
}