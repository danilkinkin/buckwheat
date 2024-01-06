package com.danilkinkin.buckwheat.editor.tagging

import androidx.compose.animation.*
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.editor.EditStage
import com.danilkinkin.buckwheat.editor.EditorViewModel
import com.danilkinkin.buckwheat.editor.FocusController
import com.danilkinkin.buckwheat.util.observeLiveData

@Composable
fun CustomTag(
    appViewModel: AppViewModel = hiltViewModel(),
    editorViewModel: EditorViewModel = hiltViewModel(),
    editorFocusController: FocusController,
    extendWidth: Dp = 0.dp,
    onlyIcon: Boolean = false,
    onEdit: (Boolean) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current

    var isEdit by remember { mutableStateOf(false) }
    var value by remember { mutableStateOf("") }

    observeLiveData(editorViewModel.stage) {
        if (it === EditStage.CREATING_SPENT) {
            value = ""
        }
    }

    DisposableEffect(editorViewModel.currentComment) {
        value = editorViewModel.currentComment

        onDispose { }
    }

    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier

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
                }
            })
    ) {
        Row(
            modifier = Modifier.widthIn(0.dp, extendWidth).padding(start = 12.dp),
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
                painter = painterResource(R.drawable.ic_comment),
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
                if (targetIsEdit) {
                    CommentEditor(
                        defaultValue = value,
                        onApply = { comment ->
                            value = comment
                            isEdit = false
                            onEdit(false)
                            appViewModel.showSystemKeyboard.value = false
                            editorViewModel.currentComment = comment
                        }
                    )
                } else if (!onlyIcon) {
                    Text(
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, end = 16.dp),
                        text = value.ifEmpty { stringResource(R.string.add_comment) },
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
fun CommentEditor(
    modifier: Modifier = Modifier,
    defaultValue: String,
    onApply: (comment: String) -> Unit,
) {
    var value by remember {
        mutableStateOf(
            TextFieldValue(
                defaultValue,
                TextRange(defaultValue.length),
            )
        )
    }
    var focusIsTracking by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    TextField(
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                if (!focusState.hasFocus && focusIsTracking) {
                    onApply(value.text)
                }
            },
        value = value,
        onValueChange = { value = it },
        trailingIcon = {
            FilledIconButton(
                modifier = Modifier.padding(end = 4.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                onClick = { onApply(value.text) },
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_apply),
                    contentDescription = null,
                )
            }
        },
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.W600),
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
            onDone = { onApply(value.text) }
        ),
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        focusIsTracking = true

        value = TextFieldValue(value.text, TextRange(value.text.length))
    }
}

@Preview
@Composable
private fun Preview() {
    BuckwheatTheme {
        CustomTag(editorFocusController = FocusController())
    }
}
