package com.luna.dollargrain.base

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.dollargrain.R
import com.luna.dollargrain.ui.BuckwheatTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DescriptionButton(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    description: @Composable (() -> Unit)? = null,
    secondDescription: @Composable (() -> Unit)? = null,
    icon: Painter = painterResource(R.drawable.ic_arrow_right),
    onClick: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(),
){
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = colors,
        elevation = elevation,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                Modifier
                    .padding(contentPadding)
                    .weight(weight = 1F, fill = true)
            ) {
                ProvideTextStyle(MaterialTheme.typography.titleMedium) {
                    title()
                }
                if (description !== null) {
                    Spacer(Modifier.height(4.dp))
                    ProvideTextStyle(MaterialTheme.typography.bodySmall) {
                        description()
                    }
                }
                if (secondDescription !== null) {
                    Spacer(Modifier.height(4.dp))
                    ProvideTextStyle(MaterialTheme.typography.bodySmall) {
                        secondDescription()
                    }
                }
            }
            Icon(
                modifier = Modifier
                    .width(48.dp)
                    .padding(end = 8.dp),
                painter = icon,
                contentDescription = null,
            )
        }
    }
}

@Preview
@Composable
fun PreviewButtonWithIcon(){
    BuckwheatTheme {
        DescriptionButton(
            title = { Text("Title") },
            description = { Text("Button looooooooooooooooooooooooooooooooooong description") },
            onClick = {},
        )
    }
}