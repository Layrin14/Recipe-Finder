package com.layrin.recipefinder.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.layrin.recipefinder.data.repository.RecipeRepository
import com.layrin.recipefinder.ui.common.SelectionManager
import com.layrin.recipefinder.ui.common.UiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavedRecipeViewModel(
    private val repository: RecipeRepository,
    val selectionManager: SelectionManager,
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow get() = _eventFlow.asSharedFlow()

    val getSavedRecipe
        get() = repository.getAllRecipe()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun onEvent(event: SavedRecipeEvent) {
        when (event) {
            is SavedRecipeEvent.DeleteSavedRecipe -> {
                viewModelScope.launch {
                    try {
                        event.data.forEach { recipeData ->
                            repository.deleteRecipe(recipeData)
                        }
                        _eventFlow.emit(
                            UiEvent.ShowSnackBar(
                                "${event.data.size} recipe(s) deleted"
                            )
                        )
                    } catch (e: Exception) {
                        _eventFlow.emit(
                            UiEvent.ShowSnackBar(
                                e.message ?: event.errorInfo()
                            )
                        )
                    }
                }
            }
        }
    }
}