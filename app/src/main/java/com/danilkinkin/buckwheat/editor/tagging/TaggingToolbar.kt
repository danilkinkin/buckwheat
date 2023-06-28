package com.danilkinkin.buckwheat.editor.tagging

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.editor.FocusController
import com.danilkinkin.buckwheat.editor.calcFontHeight
import com.danilkinkin.buckwheat.util.observeLiveData

@Composable
fun TaggingToolbar(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    editorFocusController: FocusController
) {
    val localDensity = LocalDensity.current

    var showAddComment by remember { mutableStateOf(false) }
    var isEdit by remember { mutableStateOf(false) }

    val height =
        calcFontHeight(style = MaterialTheme.typography.bodyMedium).coerceAtLeast(24.dp) + 12.dp

    observeLiveData(spendsViewModel.stage) {
        showAddComment = it === SpendsViewModel.Stage.EDIT_SPENT
    }

    val tags = arrayOf("Groceries", "Food", "Transport", "Entertainment", "Other")

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val width = maxWidth - 48.dp

        LazyRow(
            Modifier.fillMaxWidth().heightIn(height),
            contentPadding = PaddingValues(horizontal = 24.dp),
            reverseLayout = true,
            state = rememberLazyListState(),
            userScrollEnabled = !isEdit,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            item(key = "custom_tag") {
                AnimatedVisibility(
                    visible = showAddComment,
                    enter = fadeIn(
                        tween(
                            durationMillis = 150,
                            easing = EaseInOutQuad,
                        )
                    ) + slideInHorizontally(
                        tween(
                            durationMillis = 150,
                            easing = EaseInOutQuad,
                        )
                    ) { with(localDensity) { 30.dp.toPx().toInt() } },
                    exit = fadeOut(
                        tween(
                            durationMillis = 150,
                            easing = EaseInOutQuad,
                        )
                    ) + slideOutHorizontally(
                        tween(
                            durationMillis = 150,
                            easing = EaseInOutQuad,
                        )
                    ) { with(localDensity) { 30.dp.toPx().toInt() } },
                ) {
                    CustomTag(
                        editorFocusController = editorFocusController,
                        extendWidth = width,
                        onEdit = { isEdit = it },
                    )
                }
                Spacer(modifier = Modifier.width(24.dp))
            }
            items(tags.size) { index ->
                val item = tags[index]

                AnimatedVisibility(
                    visible = showAddComment,
                    enter = fadeIn(
                        tween(
                            durationMillis = 150,
                            easing = EaseInOutQuad,
                        )
                    ) + slideInHorizontally(
                        tween(
                            durationMillis = 150,
                            easing = EaseInOutQuad,
                        )
                    ) { with(localDensity) { 30.dp.toPx().toInt() } },
                    exit = fadeOut(
                        tween(
                            durationMillis = 150,
                            easing = EaseInOutQuad,
                        )
                    ) + slideOutHorizontally(
                        tween(
                            durationMillis = 150,
                            easing = EaseInOutQuad,
                        )
                    ) { with(localDensity) { 30.dp.toPx().toInt() } },
                ) {
                    Tag(value = item)
                }
            }
        }
    }
}