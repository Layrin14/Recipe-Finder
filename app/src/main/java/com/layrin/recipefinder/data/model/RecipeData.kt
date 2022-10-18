package com.layrin.recipefinder.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.layrin.recipefinder.data.api.RecipeResponse.Hit
import kotlinx.datetime.Clock
import kotlinx.parcelize.Parcelize
import kotlin.random.Random

@Entity(tableName = "recipe_data")
@Parcelize
data class RecipeData(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "uri")
    val uri: String,
    @ColumnInfo(name = "label")
    val label: String,
    @ColumnInfo(name = "images")
    val images: String,
    @ColumnInfo(name = "url")
    val url: String,
    @ColumnInfo(name = "total_time")
    val totalTime: Double,
    @ColumnInfo(name = "calorie")
    val calories: Double,
) : Parcelable {

    companion object {
        fun recipeResponseToRecipeData(data: Hit): RecipeData =
            RecipeData(
                uri = data.recipe.uri,
                label = data.recipe.label,
                images = data.recipe.image,
                url = data.recipe.url,
                totalTime = data.recipe.totalTime,
                calories = data.recipe.calories
            )

        private val foodList = listOf(
            "apple",
            "beef",
            "chicken",
            "dumplings",
            "eggplant"
        )

        fun randomFood(): String = foodList.random(Random(Clock.System.now().toEpochMilliseconds()))
    }
}