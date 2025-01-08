package com.luna.dollargrain.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luna.dollargrain.ui.DollargrainTheme
import com.luna.dollargrain.util.combineColors

@Composable
fun NumberedRow(
    modifier: Modifier = Modifier,
    number: Int,
    title: String,
    subtitle: String? = null,
) {
    val color = contentColorFor(
        combineColors(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.surfaceVariant,
            angle = 0.9F,
        )
    )

    Row {
        Box(
            Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = number.toString())
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = modifier
                .padding(bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier.heightIn(min = 28.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = color,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (subtitle !== null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = color,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun PreviewNumberedRow(){
    DollargrainTheme {
        NumberedRow(
            number = 1,
            title = "Title",
            subtitle = "looooooooooooooooooooooooooooooooooong description",
        )
    }
}