package com.layrin.recipefinder.data.repository

import com.layrin.recipefinder.data.api.RecipeApi
import com.layrin.recipefinder.data.api.RecipeResponse
import com.layrin.recipefinder.data.db.RecipeDao
import com.layrin.recipefinder.data.model.RecipeData
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class RecipeRepository(
    private val recipeDao: RecipeDao
) {

    private val recipeApi: RecipeApi

    init {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.edamam.com/api/recipes/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(client)
            .build()
        recipeApi = retrofit.create(RecipeApi::class.java)
    }

    suspend fun getRecipes(url: String): Response<RecipeResponse> = recipeApi.getRecipes(url)

    suspend fun insertRecipe(data: RecipeData) = withContext(Dispatchers.IO) {
        recipeDao.insertRecipe(data)
    }

    suspend fun deleteRecipe(data: RecipeData) = withContext(Dispatchers.IO) {
        recipeDao.deleteRecipe(data)
    }

    fun getAllRecipe(): Flow<List<RecipeData>> = recipeDao.getAllRecipe()
}