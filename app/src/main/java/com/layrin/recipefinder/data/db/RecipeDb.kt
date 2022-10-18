package com.layrin.recipefinder.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.layrin.recipefinder.data.model.RecipeData

@Database(
    entities = [RecipeData::class],
    version = 1
)
abstract class RecipeDb: RoomDatabase() {
    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile
        private var INSTANCE: RecipeDb? = null

        fun database(context: Context): RecipeDb {
            return INSTANCE ?: synchronized(context) {
                val instance = Room.databaseBuilder(
                    context,
                    RecipeDb::class.java,
                    "recipe_db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}