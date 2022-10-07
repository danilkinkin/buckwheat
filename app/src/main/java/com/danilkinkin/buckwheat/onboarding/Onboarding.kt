package com.danilkinkin.buckwheat.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.*

@Composable
fun Onboarding(
    onSetBudget: () -> Unit = {},
    onClose: () -> Unit = {},
) {
    val navigationBarHeight = WindowInsets.systemBars
        .asPaddingValues()
        .calculateBottomPadding()
        .coerceAtLeast(16.dp)

    Surface {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = navigationBarHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.hello),
                style = MaterialTheme.typography.displayMedium,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.onboarding_title),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(48.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
            ) {
                NumberedRow(
                    number = 1,
                    title = stringResource(R.string.help_set_budget_title),
                    subtitle = stringResource(R.string.help_set_budget_description),
                )
                NumberedRow(
                    number = 2,
                    title = stringResource(R.string.help_record_spends_title),
                    subtitle = stringResource(R.string.help_record_spends_description),
                )
                NumberedRow(
                    number = 3,
                    title = stringResource(R.string.help_good_luck_title),
                    subtitle = stringResource(R.string.help_good_luck_description),
                )
            }
            Spacer(Modifier.height(48.dp))
            ButtonWithIcon(
                title = stringResource(R.string.set_period_title),
                onClick = {
                    onSetBudget()
                    onClose()
                },
            )
        }
    }
}

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonWithIcon(
    title: String,
    description: String? = null,
    secondDescription: String? = null,
    onClick: () -> Unit,
){
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                Modifier
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .weight(weight = 1F, fill = true)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
                if (description !== null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (secondDescription !== null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = secondDescription,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Icon(
                modifier = Modifier
                    .width(48.dp)
                    .padding(end = 8.dp),
                painter = painterResource(R.drawable.ic_arrow_right),
                contentDescription = null,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewNumberedRow(){
    BuckwheatTheme {
        NumberedRow(
            number = 1,
            title = "Title",
            subtitle = "Button looooooooooooooooooooooooooooooooooong description",
        )
    }
}

@Preview
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        Onboarding()
    }
}