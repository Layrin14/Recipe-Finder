package com.layrin.recipefinder.ui.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.layrin.recipefinder.data.repository.RecipeRepository
import com.layrin.recipefinder.ui.common.SelectionManager

@Suppress("unchecked_cast")
class HomeViewModelFactory(
    private val repository: RecipeRepository,
    private val manager: SelectionManager,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(repository, manager, application) as T
    }
}