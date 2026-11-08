package com.danilkinkin.buckwheat.ui

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
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
            fontFamily = getFont(800),
            fontSize = 57.sp,
            lineHeight = 64.sp,
            letterSpacing = (-1).sp,
        ),
        displayMedium = TextStyle(
            fontFamily = getFont(800),
            fontSize = 45.sp,
            lineHeight = 52.sp,
            letterSpacing = (-0.8).sp,
        ),
        displaySmall = TextStyle(
            fontFamily = getFont(700),
            fontSize = 34.sp,
            lineHeight = 40.sp,
            letterSpacing = (-0.4).sp,
        ),
        headlineLarge = TextStyle(
            fontFamily = getFont(800),
            fontSize = 34.sp,
            lineHeight = 40.sp,
            letterSpacing = (-0.4).sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = getFont(700),
            fontSize = 28.sp,
            lineHeight = 34.sp,
            letterSpacing = (-0.2).sp,
        ),
        headlineSmall = TextStyle(
            fontFamily = getFont(700),
            fontSize = 24.sp,
            lineHeight = 30.sp,
            letterSpacing = (-0.2).sp,
        ),
        titleLarge = TextStyle(
            fontFamily = getFont(800),
            fontSize = 20.sp,
            lineHeight = 26.sp,
            letterSpacing = (-0.1).sp,
        ),
        titleMedium = TextStyle(
            fontFamily = getFont(600),
            fontSize = 16.sp,
            lineHeight = 22.sp,
        ),
        titleSmall = TextStyle(
            fontFamily = getFont(600),
            fontSize = 14.sp,
            lineHeight = 20.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = getFont(500),
            fontSize = 16.sp,
            lineHeight = 24.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = getFont(500),
            fontSize = 14.sp,
            lineHeight = 20.sp,
        ),
        bodySmall = TextStyle(
            fontFamily = getFont(400),
            fontSize = 13.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.1.sp,
        ),
        labelLarge = TextStyle(
            fontFamily = getFont(700),
            fontSize = 14.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.1.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = getFont(600),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.2.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = getFont(500),
            fontSize = 11.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.3.sp,
        ),
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
    BuckwheatTheme {
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
