package com.danilkinkin.buckwheat.base

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.entities.Category

/** Keys stored in [Category.icon] and offered by the add-category icon picker. */
val categoryIconKeys = listOf(
    "food",
    "groceries",
    "transport",
    "home",
    "mobile",
    "streaming",
    "music",
    "digital",
    "shopping",
    "entertainment",
    "health",
    "travel",
    "other",
)

fun categoryIconRes(key: String?): Int? = when (key) {
    "food" -> R.drawable.ic_cat_food
    "groceries" -> R.drawable.ic_cat_groceries
    "transport" -> R.drawable.ic_cat_transport
    "home" -> R.drawable.ic_cat_home
    "mobile" -> R.drawable.ic_cat_mobile
    "streaming" -> R.drawable.ic_cat_streaming
    "music" -> R.drawable.ic_cat_music
    "digital" -> R.drawable.ic_cat_digital
    "shopping" -> R.drawable.ic_cat_shopping
    "entertainment" -> R.drawable.ic_cat_entertainment
    "health" -> R.drawable.ic_cat_health
    "travel" -> R.drawable.ic_cat_travel
    "other" -> R.drawable.ic_cat_other
    else -> null
}

@Composable
fun CategoryIcon(
    category: Category?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    CategoryIcon(iconKey = category?.icon, modifier = modifier, tint = tint)
}

@Composable
fun CategoryIcon(
    iconKey: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    Icon(
        painter = painterResource(
            categoryIconRes(iconKey) ?: R.drawable.ic_cat_other
        ),
        contentDescription = null,
        tint = tint,
        modifier = modifier,
    )
}
