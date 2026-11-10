package com.danilkinkin.buckwheat.data

import com.danilkinkin.buckwheat.data.entities.Category

/** Display order for the seeded categories. Custom categories follow, sorted by name. */
private val categoryOrder = listOf(
    "Food & Dining",
    "Groceries",
    "Transport",
    "Home & Bills",
    "Mobile & Internet",
    "Streaming",
    "Music",
    "Digital & AI",
    "Shopping",
    "Entertainment",
    "Health & Fitness",
    "Travel",
)

fun List<Category>.sortedForDisplay(): List<Category> = sortedWith(
    compareBy(
        { category ->
            when {
                category.name == "Other" -> Int.MAX_VALUE
                else -> categoryOrder.indexOf(category.name)
                    .let { if (it >= 0) it else categoryOrder.size }
            }
        },
        { it.name.lowercase() },
    )
)
