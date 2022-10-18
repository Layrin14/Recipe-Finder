package com.layrin.recipefinder.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.layrin.recipefinder.data.model.RecipeData
import com.layrin.recipefinder.data.repository.RecipeRepository
import com.layrin.recipefinder.ui.common.UiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class RecipeViewViewModel(
    private val repository: RecipeRepository,
    private val recipeData: RecipeData?
): ViewModel() {

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow get() = _eventFlow.asSharedFlow()

    fun onEvent(event: RecipeViewEvent) {
        when (event) {
            RecipeViewEvent.SaveRecipe -> {
                viewModelScope.launch {
                    try {
                        recipeData?.let { data ->
                            repository.insertRecipe(data)
                        }
                        _eventFlow.emit(
                            UiEvent.ShowSnackBar(
                                "Recipe has been saved"
                            )
                        )
                    } catch (e: Exception) {
                        _eventFlow.emit(
                            UiEvent.ShowSnackBar(
                                e.message ?: "Error saving recipe"
                            )
                        )
                    }
                }
            }
        }
    }
}