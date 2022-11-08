package com.danilkinkin.buckwheat.editor

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.R

@Composable
fun TaggingSpent() {
    var checkedTags by remember { mutableStateOf(emptySet<String>()) }

    Row {
        Spacer(Modifier.width(36.dp))
        TagChip(
            selected = checkedTags.contains("food"),
            icon = "\uD83E\uDD55",
            label = "Food",
            onClick = {
                checkedTags = if (checkedTags.contains("food")) {
                    checkedTags.minus("food")
                } else {
                    checkedTags.plus("food")
                }
            }
        )
        Spacer(Modifier.width(12.dp))
        TagChip(
            selected = checkedTags.contains("hobby"),
            icon = "\uD83C\uDFA8",
            label = "Hobby",
            onClick = {
                checkedTags = if (checkedTags.contains("hobby")) {
                    checkedTags.minus("hobby")
                } else {
                    checkedTags.plus("hobby")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagChip(
    selected: Boolean,
    icon: String,
    label: String,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        leadingIcon = {
            Text(text = icon)
        },
        trailingIcon = {
            if (!selected) return@FilterChip

            Icon(
                painter = painterResource(R.drawable.ic_apply),
                contentDescription = null,
            )
        },
        onClick = onClick,
        label = { Text(text = label) },
    )
}

@Preview
@Composable
private fun Preview() {
    BuckwheatTheme() {
        TaggingSpent()
    }
}
