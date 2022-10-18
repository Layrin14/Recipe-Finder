package com.layrin.recipefinder.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url
import kotlin.random.Random

interface RecipeApi {
    @GET
    suspend fun getRecipes(
        @Url url: String
    ): Response<RecipeResponse>

    companion object {
        fun getUrl(query: String): String =
            "https://api.edamam.com/api/recipes/v2?type=public&q=${query}&app_id=${APP_ID}&app_key=${APP_KEY}&field=uri&field=label&field=image&field=images&field=source&field=url&field=ingredientLines&field=calories&field=totalTime"
        private const val APP_KEY = "2d01cf8bd6eb9d621693c0d00a2d85a1"
        private const val APP_ID = "65285f55"
    }
}