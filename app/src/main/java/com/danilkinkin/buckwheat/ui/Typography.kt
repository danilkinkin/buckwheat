package com.danilkinkin.buckwheat.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.danilkinkin.buckwheat.R
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val light = Font(R.font.manrope_extra_light, FontWeight.W300)
private val regular = Font(R.font.manrope_regular, FontWeight.W400)
private val medium = Font(R.font.manrope_medium, FontWeight.W500)
private val semibold = Font(R.font.manrope_semi_bold, FontWeight.W600)
private val bold = Font(R.font.manrope_bold, FontWeight.W800)
private val extrabold = Font(R.font.manrope_extra_bold, FontWeight.W900)

private val fontFamily = FontFamily(fonts = listOf(light, regular, medium, semibold, bold, extrabold))

val typography = Typography(
    displayLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.W900,
        fontSize = 57.sp
    ),
    displayMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.W800,
        fontSize = 45.sp
    ),
    displaySmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.W700,
        fontSize = 36.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.W800,
        fontSize = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.W700,
        fontSize = 28.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.W700,
        fontSize = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.W800,
        fontSize = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.W700,
        fontSize = 16.sp
    ),
    titleSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.W700,
        fontSize = 14.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.W700,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.W600,
        fontSize = 14.sp
    ),
    bodySmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.W500,
        fontSize = 12.sp
    ),
    labelLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.W800,
        fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.W700,
        fontSize = 12.sp
    ),
    labelSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.W600,
        fontSize = 11.sp
    )
)

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
