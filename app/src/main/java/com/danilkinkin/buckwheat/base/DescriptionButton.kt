package com.danilkinkin.buckwheat.base

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.ui.BuckwheatTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DescriptionButton(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    description: @Composable (() -> Unit)? = null,
    secondDescription: @Composable (() -> Unit)? = null,
    icon: Painter = painterResource(R.drawable.ic_arrow_right),
    onClick: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 32.dp),
    colors: CardColors = CardDefaults.cardColors(),
){
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = colors,
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
                    ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
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