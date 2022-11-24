package com.danilkinkin.buckwheat.editor.tagging

import androidx.compose.animation.*
import androidx.compose.animation.core.EaseInOutQuad
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.editor.FocusController
import com.danilkinkin.buckwheat.editor.calcFontHeight
import com.danilkinkin.buckwheat.util.observeLiveData

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TaggingSpent(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    editorFocusController: FocusController,
) {
    val localDensity = LocalDensity.current
    val focusManager = LocalFocusManager.current

    var showAddComment by remember { mutableStateOf(false) }
    var isEdit by remember { mutableStateOf(false) }
    var value by remember { mutableStateOf("") }
    val height = calcFontHeight(style = MaterialTheme.typography.bodyMedium).coerceAtLeast(24.dp) + 12.dp

    observeLiveData(spendsViewModel.stage) {
        if (it === SpendsViewModel.Stage.CREATING_SPENT) {
            value = ""
        }

        showAddComment = it === SpendsViewModel.Stage.EDIT_SPENT
    }

    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(height)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))

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
                            appViewModel.showSystemKeyboard.value = true
                        }
                    })
            ) {
                Row(
                    modifier = Modifier.padding(start = 12.dp),
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
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(R.drawable.ic_comment),
                        contentDescription = null,
                    )
                    AnimatedVisibility(
                        visible = !isEdit,
                        enter = scaleIn(tween(durationMillis = 150)),
                        exit = scaleOut(tween(durationMillis = 150)),
                    ) {
                        Spacer(Modifier.width(8.dp))
                    }
                    AnimatedContent(
                        targetState = isEdit,
                        transitionSpec = {
                            (fadeIn(
                                tween(durationMillis = 250)
                            ) with fadeOut(
                                tween(durationMillis = 250)
                            )).using(
                                SizeTransform(clip = false)
                            )
                        }
                    ) { targetIsEdit -> if (targetIsEdit) {
                        CommentEditor(
                            defaultValue = value,
                            onApply = { comment ->
                                value = comment
                                isEdit = false
                                appViewModel.showSystemKeyboard.value = false
                                spendsViewModel.currentComment = comment
                            }
                        )
                    } else {
                        Text(
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, end = 16.dp),
                            text = value.ifEmpty { stringResource(R.string.add_comment) },
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                        )
                    } }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentEditor(defaultValue: String, onApply: (comment: String) -> Unit) {
    var value by remember { mutableStateOf(TextFieldValue(
        defaultValue,
        TextRange(defaultValue.length),
    )) }
    var focusIsTracking by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    TextField(
        modifier = Modifier
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
        colors = TextFieldDefaults.textFieldColors(
            containerColor = MaterialTheme.colorScheme.surface,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
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
        TaggingSpent(editorFocusController = FocusController())
    }
}
