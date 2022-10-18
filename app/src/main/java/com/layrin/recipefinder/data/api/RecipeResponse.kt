package com.layrin.recipefinder.data.api


import com.squareup.moshi.Json

data class RecipeResponse(
    @Json(name = "count")
    val count: Int,
    @Json(name = "from")
    val from: Int,
    @Json(name = "hits")
    val hits: MutableList<Hit>,
    @Json(name = "_links")
    val links: Links,
    @Json(name = "to")
    val to: Int
) {
    data class Hit(
        @Json(name = "_links")
        val links: Links,
        @Json(name = "recipe")
        val recipe: Recipe
    ) {
        data class Links(
            @Json(name = "self")
            val self: Self
        ) {
            data class Self(
                @Json(name = "href")
                val href: String,
                @Json(name = "title")
                val title: String
            )
        }

        data class Recipe(
            @Json(name = "calories")
            val calories: Double,
            @Json(name = "image")
            val image: String,
            @Json(name = "images")
            val images: Images,
            @Json(name = "ingredientLines")
            val ingredientLines: List<String>,
            @Json(name = "label")
            val label: String,
            @Json(name = "source")
            val source: String,
            @Json(name = "totalTime")
            val totalTime: Double,
            @Json(name = "uri")
            val uri: String,
            @Json(name = "url")
            val url: String
        ) {
            data class Images(
                @Json(name = "LARGE")
                val lARGE: LARGE?,
                @Json(name = "REGULAR")
                val rEGULAR: REGULAR,
                @Json(name = "SMALL")
                val sMALL: SMALL,
                @Json(name = "THUMBNAIL")
                val tHUMBNAIL: THUMBNAIL
            ) {
                data class LARGE(
                    @Json(name = "height")
                    val height: Int,
                    @Json(name = "url")
                    val url: String,
                    @Json(name = "width")
                    val width: Int
                )

                data class REGULAR(
                    @Json(name = "height")
                    val height: Int,
                    @Json(name = "url")
                    val url: String,
                    @Json(name = "width")
                    val width: Int
                )

                data class SMALL(
                    @Json(name = "height")
                    val height: Int,
                    @Json(name = "url")
                    val url: String,
                    @Json(name = "width")
                    val width: Int
                )

                data class THUMBNAIL(
                    @Json(name = "height")
                    val height: Int,
                    @Json(name = "url")
                    val url: String,
                    @Json(name = "width")
                    val width: Int
                )
            }
        }
    }

    data class Links(
        @Json(name = "next")
        val next: Next
    ) {
        data class Next(
            @Json(name = "href")
            val href: String,
            @Json(name = "title")
            val title: String
        )
    }
}