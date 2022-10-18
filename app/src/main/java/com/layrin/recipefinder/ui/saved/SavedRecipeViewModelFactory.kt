package com.layrin.recipefinder.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.layrin.recipefinder.data.repository.RecipeRepository
import com.layrin.recipefinder.ui.common.SelectionManager

@Suppress("unchecked_cast")
class SavedRecipeViewModelFactory(
    private val repository: RecipeRepository,
    private val selectionManager: SelectionManager
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SavedRecipeViewModel(repository, selectionManager) as T
    }
}