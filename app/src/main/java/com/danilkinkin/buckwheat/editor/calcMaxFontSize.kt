package com.danilkinkin.buckwheat.editor

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.Paragraph
import androidx.compose.ui.text.ParagraphIntrinsics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlin.math.ceil

@Composable
fun calcMaxFont(
    height: Float,
    text: String = "SAMPLE 1234567890",
    style: TextStyle = MaterialTheme.typography.displayLarge,
): TextUnit {
    val measureFontSize = 100.sp

    val intrinsics = ParagraphIntrinsics(
        text = text,
        style = style.copy(fontSize = measureFontSize),
        density = LocalDensity.current,
        fontFamilyResolver = createFontFamilyResolver(LocalContext.current)
    )

    val paragraph = Paragraph(
        paragraphIntrinsics = intrinsics,
        constraints = Constraints(maxWidth = ceil(1000f).toInt()),
        maxLines = 1,
        ellipsis = false
    )

    return with(LocalDensity.current) {
        ((measureFontSize.toPx() / paragraph.height) * height).toSp()
    }
}

@Composable
fun calcFontHeight(
    text: String = "SAMPLE 1234567890",
    style: TextStyle = MaterialTheme.typography.displayLarge,
): Dp {
    val intrinsics = ParagraphIntrinsics(
        text = text,
        style = style,
        density = LocalDensity.current,
        fontFamilyResolver = createFontFamilyResolver(LocalContext.current)
    )

    val paragraph = Paragraph(
        paragraphIntrinsics = intrinsics,
        constraints = Constraints(maxWidth = ceil(1000f).toInt()),
        maxLines = 1,
        ellipsis = false
    )

    return with(LocalDensity.current) {
        paragraph.height.toDp()
    }
}