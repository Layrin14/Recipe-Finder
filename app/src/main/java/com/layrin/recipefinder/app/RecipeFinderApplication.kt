package com.layrin.recipefinder.app

import android.app.Application
import com.layrin.recipefinder.data.db.RecipeDb

class RecipeFinderApplication: Application() {
    val database by lazy {
        RecipeDb.database(this)
    }
}