package com.danilkinkin.buckwheat.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.danilkinkin.buckwheat.base.Divider

@Composable
fun CheckPaddings() {
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .background(Color.Red)
                    .fillMaxWidth()
                    .weight(1F))
            Box(
                Modifier
                    .background(Color.Blue)
                    .fillMaxWidth()
                    .weight(1F))
        }
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            Divider()
            Box(
                Modifier
                    .background(Color.Red)
                    .fillMaxWidth()
                    .weight(1F))
            Box(
                Modifier
                    .background(Color.Green)
                    .fillMaxWidth()
                    .weight(1F))
            Box(
                Modifier
                    .background(Color.Blue)
                    .fillMaxWidth()
                    .weight(1F))
            Divider()
        }
    }
}

@Preview
@Composable
fun PreviewCheckPaddings() {
    CheckPaddings()
}