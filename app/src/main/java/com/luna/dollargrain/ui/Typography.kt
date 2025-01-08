package com.luna.dollargrain.ui

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun typography(context: Context): Typography {
    fun getFont(weight: Int) = FontFamily(
        Font(
            "font/manrope_variable.ttf", context.assets,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(weight),
            ),
        )
    )

    return Typography(
        displayLarge = TextStyle(
            fontFamily = getFont(750),
            fontSize = 57.sp
        ),
        displayMedium = TextStyle(
            fontFamily = getFont(900),
            fontSize = 45.sp
        ),
        displaySmall = TextStyle(
            fontFamily = getFont(600),
            fontSize = 22.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = getFont(800),
            fontSize = 36.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = getFont(700),
            fontSize = 28.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = getFont(700),
            fontSize = 24.sp
        ),
        titleLarge = TextStyle(
            fontFamily = getFont(700),
            fontSize = 22.sp
        ),
        titleMedium = TextStyle(
            fontFamily = getFont(700),
            fontSize = 16.sp
        ),
        titleSmall = TextStyle(
            fontFamily = getFont(700),
            fontSize = 14.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = getFont(700),
            fontSize = 16.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = getFont(700),
            fontSize = 14.sp
        ),
        bodySmall = TextStyle(
            fontFamily = getFont(600),
            fontSize = 14.sp
        ),
        labelLarge = TextStyle(
            fontFamily = getFont(700),
            fontSize = 14.sp
        ),
        labelMedium = TextStyle(
            fontFamily = getFont(700),
            fontSize = 12.sp
        ),
        labelSmall = TextStyle(
            fontFamily = getFont(600),
            fontSize = 11.sp
        )
    )
}

@Composable
fun FontCard(family: String, size: String, style: TextStyle) {
    Card(
        shape = CardDefaults.outlinedShape,
        colors = CardDefaults.outlinedCardColors(),
        modifier = Modifier.padding(8.dp),
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            Text(text = family, style = style)
            Text(text = size)
        }
    }
}

@Preview
@Composable
fun PreviewTypography() {
    DollargrainTheme {
        Surface {
            Row {
                Column {
                    FontCard("Display", "L", MaterialTheme.typography.displayLarge)
                    FontCard("Display", "M", MaterialTheme.typography.displayMedium)
                    FontCard("Display", "S", MaterialTheme.typography.displaySmall)
                    FontCard("Headline", "L", MaterialTheme.typography.headlineLarge)
                    FontCard("Headline", "M", MaterialTheme.typography.headlineMedium)
                    FontCard("Headline", "S", MaterialTheme.typography.headlineSmall)
                    FontCard("Title", "L", MaterialTheme.typography.titleLarge)
                    FontCard("Title", "M", MaterialTheme.typography.titleMedium)
                    FontCard("Title", "S", MaterialTheme.typography.titleSmall)
                }
                Column {
                    FontCard("Body", "L", MaterialTheme.typography.bodyLarge)
                    FontCard("Body", "M", MaterialTheme.typography.bodyMedium)
                    FontCard("Body", "S", MaterialTheme.typography.bodySmall)
                    FontCard("Label", "L", MaterialTheme.typography.labelLarge)
                    FontCard("Label", "M", MaterialTheme.typography.labelMedium)
                    FontCard("Label", "S", MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
