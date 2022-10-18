package com.layrin.recipefinder.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.layrin.recipefinder.data.model.RecipeData
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(data: RecipeData)

    @Query("SELECT * FROM recipe_data")
    fun getAllRecipe(): Flow<List<RecipeData>>

    @Delete
    suspend fun deleteRecipe(data: RecipeData)

}