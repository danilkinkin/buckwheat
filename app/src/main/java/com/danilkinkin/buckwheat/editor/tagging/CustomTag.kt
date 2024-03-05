package com.danilkinkin.buckwheat.editor.tagging

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.LocalWindowInsets
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.balloon.detectTapUnconsumed
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.editor.EditStage
import com.danilkinkin.buckwheat.editor.EditorViewModel
import com.danilkinkin.buckwheat.editor.FocusController
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.observeLiveData

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CustomTag(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    editorViewModel: EditorViewModel = hiltViewModel(),
    editorFocusController: FocusController,
    extendWidth: Dp = 0.dp,
    onlyIcon: Boolean = false,
    onEdit: (Boolean) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    val localDensity = LocalDensity.current

    val tags by spendsViewModel.tags.observeAsState(emptyList())

    var isEdit by remember { mutableStateOf(false) }
    var value by remember {
        mutableStateOf(
            TextFieldValue(
                "",
                TextRange(0),
            )
        )
    }
    var isShowSuggestions by remember { mutableStateOf(false) }
    var renderPopup by remember { mutableStateOf(false) }

    observeLiveData(editorViewModel.stage) {
        if (it === EditStage.CREATING_SPENT) {
            value = TextFieldValue(
                "",
                TextRange(0),
            )
        }
    }

    observeLiveData(editorViewModel.currentComment) {
        value = TextFieldValue(
            it ?: "",
            TextRange((it ?: "").length),
        )
    }

    DisposableEffect(editorViewModel.currentComment) {
        value = TextFieldValue(
            editorViewModel.currentComment.value ?: "",
            TextRange((editorViewModel.currentComment.value ?: "").length),
        )

        onDispose { }
    }

    val close = {
        isEdit = false
        isShowSuggestions = false
        onEdit(false)
        appViewModel.showSystemKeyboard.value = false
        appViewModel.lockDraggable.value = false
        editorViewModel.currentComment.value = value.text.trim()
    }

    ExposedDropdownMenuBox(expanded = isShowSuggestions, onExpandedChange = {}) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .menuAnchor()
                .clip(CircleShape)
                .then(if (isEdit) {
                    Modifier
                } else {
                    Modifier.clickable {
                        editorFocusController.blur()
                        focusManager.clearFocus()
                        isEdit = true
                        onEdit(true)
                        appViewModel.showSystemKeyboard.value = true
                        appViewModel.lockDraggable.value = true
                    }
                })
        ) {
            Row(
                modifier = Modifier
                    .widthIn(0.dp, extendWidth)
                    .padding(start = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AnimatedVisibility(
                    visible = isEdit,
                    enter = scaleIn(tween(durationMillis = 150)),
                    exit = scaleOut(tween(durationMillis = 150)),
                ) {
                    Spacer(Modifier.width(8.dp))
                }
                Icon(
                    modifier = Modifier
                        .width(20.dp)
                        .height(44.dp),
                    painter = painterResource(R.drawable.ic_label),
                    contentDescription = null,
                )

                AnimatedVisibility(
                    visible = !isEdit,
                    enter = scaleIn(tween(durationMillis = 150)),
                    exit = scaleOut(tween(durationMillis = 150)),
                ) {
                    if (onlyIcon) {
                        Spacer(Modifier.width(12.dp))
                    } else {
                        Spacer(Modifier.width(8.dp))
                    }
                }
                AnimatedContent(
                    label = "openCloseTaggingEditor",
                    targetState = isEdit,
                    transitionSpec = {
                        (fadeIn(
                            tween(durationMillis = 250)
                        ) togetherWith fadeOut(
                            tween(durationMillis = 250)
                        )).using(
                            SizeTransform(clip = false)
                        )
                    }
                ) { targetIsEdit ->
                    if (this.transition.currentState == this.transition.targetState && targetIsEdit) {
                        renderPopup = true
                    }

                    if (targetIsEdit) {
                        CommentEditor(
                            value = value,
                            onChange = { value = it },
                            onApply = { close() }
                        )
                    } else if (!onlyIcon || value.text.isNotEmpty()) {
                        Text(
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, end = 16.dp),
                            text = value.text.ifEmpty { stringResource(R.string.add_comment) },
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        if (renderPopup) {
            val filteredItems = tags.filter {
                it.contains(value.text, ignoreCase = true)
            }

            val topBarHeight = LocalWindowInsets.current.calculateTopPadding()

            val height = remember { mutableStateOf(1000.dp) }
            val popupPositionProvider = DropdownMenuPositionProvider(
                DpOffset(0.dp, 8.dp),
                localDensity,
                topBarHeight,
            ) { parentBounds, menuBounds ->
                height.value = with(localDensity) { menuBounds.height.toDp() }
            }

            Popup(
                popupPositionProvider = popupPositionProvider,
                onDismissRequest = {
                },
            ) {
                val dismissEvent = remember {
                    mutableStateOf(false)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height.value)
                        .pointerInput(Unit) {
                            detectTapUnconsumed {
                                if (!dismissEvent.value) close()
                                dismissEvent.value = false
                            }
                        },
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    AnimatedVisibility(
                        visible = isShowSuggestions,
                        enter = expandVertically(tween(150)),
                        exit = shrinkVertically(tween(150)),
                    ) {
                        if (filteredItems.isNotEmpty() && !(filteredItems.size == 1 && filteredItems[0] == value.text)) {
                            Surface(
                                modifier = Modifier
                                    .width(extendWidth)
                                    .pointerInput(Unit) {
                                        detectTapGestures {
                                            dismissEvent.value = true
                                        }
                                    },
                                shape = RoundedCornerShape(16.dp)
                            ) {

                                LazyColumn(
                                    userScrollEnabled = true,
                                    contentPadding = PaddingValues(vertical = 8.dp),
                                ) {
                                    filteredItems.forEach {
                                        itemSuggest(it) {
                                            dismissEvent.value = true
                                            value = TextFieldValue(
                                                it,
                                                TextRange(it.length),
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        DisposableEffect(Unit) {
                            onDispose {
                                renderPopup = false
                            }
                        }
                    }
                }
            }

            LaunchedEffect(Unit) {
                isShowSuggestions = true
            }
        }


    }
}

private fun LazyListScope.itemSuggest(
    name: String,
    onClick: () -> Unit,
) {
    item(name) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .clickable {
                    onClick()
                }
                .fillMaxWidth()
                .heightIn(42.dp)
                .padding(start = 24.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        ) {
            Text(
                text = name,
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

internal data class DropdownMenuPositionProvider(
    val contentOffset: DpOffset,
    val density: Density,
    val topBarHeight: Dp,
    val onPositionCalculated: (IntRect, IntRect) -> Unit = { _, _ -> }
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        // The min margin above and below the menu, relative to the screen.
        val verticalMargin = with(density) { 48.dp.roundToPx() }
        val topBarHeight = with(density) { topBarHeight.roundToPx() }
        // The content offset specified using the dropdown offset parameter.
        val contentOffsetX = with(density) { contentOffset.x.roundToPx() }
        val contentOffsetY = with(density) { contentOffset.y.roundToPx() }

        // Compute horizontal position.
        val toRight = anchorBounds.left + contentOffsetX
        val toLeft = anchorBounds.right - contentOffsetX - popupContentSize.width
        val toDisplayRight = windowSize.width - popupContentSize.width
        val toDisplayLeft = 0
        val x = if (layoutDirection == LayoutDirection.Ltr) {
            sequenceOf(
                toRight,
                toLeft,
                // If the anchor gets outside of the window on the left, we want to position
                // toDisplayLeft for proximity to the anchor. Otherwise, toDisplayRight.
                if (anchorBounds.left >= 0) toDisplayRight else toDisplayLeft
            )
        } else {
            sequenceOf(
                toLeft,
                toRight,
                // If the anchor gets outside of the window on the right, we want to position
                // toDisplayRight for proximity to the anchor. Otherwise, toDisplayLeft.
                if (anchorBounds.right <= windowSize.width) toDisplayLeft else toDisplayRight
            )
        }.firstOrNull {
            it >= 0 && it + popupContentSize.width <= windowSize.width
        } ?: toLeft

        // Compute vertical position.
        val yBottom = anchorBounds.top - contentOffsetY

        onPositionCalculated(
            anchorBounds,
            IntRect(x, topBarHeight, x + popupContentSize.width, yBottom)
        )
        return IntOffset(x, topBarHeight)
    }
}

@Composable
fun CommentEditor(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    onChange: (comment: TextFieldValue) -> Unit,
    onApply: () -> Unit,
) {
    var focusIsTracking by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }


    TextField(
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                if (!focusState.hasFocus && focusIsTracking) {
                    onApply()
                }
            },
        value = value,
        onValueChange = {
            onChange(it)
        },
        trailingIcon = {
            FilledIconButton(
                modifier = Modifier.padding(end = 4.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                onClick = { onApply() },
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_apply),
                    contentDescription = null,
                )
            }
        },
        textStyle = MaterialTheme.typography.bodyLarge,
        singleLine = true,
        shape = RectangleShape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
        ),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = { onApply() }
        ),
    )


    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        focusIsTracking = true
    }
}

@Preview
@Composable
private fun Preview() {
    BuckwheatTheme {
        CustomTag(editorFocusController = FocusController())
    }
}
