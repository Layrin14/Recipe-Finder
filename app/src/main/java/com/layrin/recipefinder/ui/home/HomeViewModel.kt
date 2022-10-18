package com.layrin.recipefinder.ui.home

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.layrin.recipefinder.app.RecipeFinderApplication
import com.layrin.recipefinder.data.api.RecipeApi.Companion.getUrl
import com.layrin.recipefinder.data.api.RecipeResponse
import com.layrin.recipefinder.data.model.RecipeData
import com.layrin.recipefinder.data.model.RecipeData.Companion.randomFood
import com.layrin.recipefinder.data.model.RecipeResponseState
import com.layrin.recipefinder.data.repository.RecipeRepository
import com.layrin.recipefinder.ui.common.SelectionManager
import com.layrin.recipefinder.ui.common.UiEvent
import com.layrin.recipefinder.util.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: RecipeRepository,
    val selectionManager: SelectionManager,
    application: Application,
) : AndroidViewModel(application) {

    // Random food recipe
    private val _randomRecipeState = MutableStateFlow(RecipeResponseState())
    val randomRecipeState get() = _randomRecipeState.asStateFlow()

    private var baseUrlRandomFood: String? = getUrl(randomFood())

    private var _randomFoodRecipeResponse: RecipeResponse? = null
    private var _randomRecipeTotal: Int? = 0
    val randomRecipeTotal get() = _randomRecipeTotal

    // Food recipe found from query
    private val _recipeFoundState = MutableStateFlow(RecipeResponseState())
    val recipeFoundState get() = _recipeFoundState.asStateFlow()

    private var baseRecipeUrl: String? = ""
    val searchRecipeUrl get() = baseRecipeUrl

    private var _foodRecipeFoundResponse: RecipeResponse? = null
    private var _foodRecipeFoundTotal: Int? = 0
    val foodRecipeFoundTotal get() = _foodRecipeFoundTotal

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent get() = _uiEvent.asSharedFlow()

    init {
        getRandomRecipes()
    }

    fun getRandomRecipes() = viewModelScope.launch {
        _randomRecipeState.update { data ->
            data.copy(
                isLoading = true,
                error = null
            )
        }
        when (val response = handleGetRandomRecipes(baseUrlRandomFood)) {
            is Resource.Success -> {
                _randomRecipeState.update { data ->
                    data.copy(
                        recipe = response.data?.hits?.map { hit ->
                            RecipeData.recipeResponseToRecipeData(hit)
                        },
                        isLoading = false,
                        error = null
                    )
                }
            }
            is Resource.Error -> {
                _randomRecipeState.update { data ->
                    data.copy(
                        recipe = null,
                        isLoading = false,
                        error = response.message
                    )
                }
            }
        }
    }

    private suspend fun handleGetRandomRecipes(url: String?): Resource<RecipeResponse> {
        return try {
            if (checkHasInternetConnection()) {
                val data = url?.let { repository.getRecipes(it) }
                baseUrlRandomFood = data?.body()?.links?.next?.href
                if (_randomFoodRecipeResponse == null) _randomFoodRecipeResponse = data?.body()
                else {
                    val newData = data?.body()?.hits
                    if (newData != null) _randomFoodRecipeResponse?.hits?.addAll(newData)
                }
                _randomRecipeTotal = data?.body()?.to
                Resource.Success(
                    data = _randomFoodRecipeResponse!!
                )
            } else {
                Resource.Error(
                    message = "No internet connection"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(message = e.message ?: "An unknown error occurred")
        }
    }

    fun searchRecipe(query: String) = viewModelScope.launch {
        _recipeFoundState.update { data ->
            data.copy(
                isLoading = true
            )
        }
        when (val response = handleSearchRecipes(getUrl(query))) {
            is Resource.Success -> {
                _recipeFoundState.update { data ->
                    data.copy(
                        recipe = response.data?.hits?.map { hit ->
                            RecipeData.recipeResponseToRecipeData(hit)
                        },
                        isLoading = false,
                    )
                }
            }
            is Resource.Error -> {
                _recipeFoundState.update { data ->
                    data.copy(
                        isLoading = false,
                        error = response.message
                    )
                }
            }
        }
    }

    fun resetSearchQuery() {
        baseRecipeUrl = ""
        _foodRecipeFoundTotal = 0
        _foodRecipeFoundResponse = null
        _recipeFoundState.update { data ->
            data.copy(
                recipe = null,
                isLoading = true
            )
        }
    }

    private suspend fun handleSearchRecipes(url: String?): Resource<RecipeResponse> {
        return try {
            if (checkHasInternetConnection()) {
                val data = if (baseRecipeUrl == "") url?.let { repository.getRecipes(it) }
                else baseRecipeUrl?.let { repository.getRecipes(it) }
                baseRecipeUrl = data?.body()?.links?.next?.href
                if (_foodRecipeFoundResponse == null) _foodRecipeFoundResponse = data?.body()
                else {
                    val newData = data?.body()?.hits
                    if (newData != null) _foodRecipeFoundResponse?.hits?.addAll(newData)
                }
                _foodRecipeFoundTotal = data?.body()?.to
                Resource.Success(
                    data = _foodRecipeFoundResponse!!
                )
            } else {
                Resource.Error(
                    message = "No internet connection"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(message = e.message ?: "An unknown error occurred")
        }
    }

    private fun checkHasInternetConnection(): Boolean {
        val connectivityManager =
            getApplication<RecipeFinderApplication>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.AddItemToBookmarkEvent -> {
                viewModelScope.launch {
                    try {
                        event.data.forEach { recipeData ->
                            repository.insertRecipe(recipeData)
                        }
                        _uiEvent.emit(
                            UiEvent.ShowSnackBar("${event.data.size} recipe added to bookmark")
                        )
                    } catch (e: Exception) {
                        _uiEvent.emit(
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