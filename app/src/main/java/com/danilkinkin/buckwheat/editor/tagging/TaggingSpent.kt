package com.danilkinkin.buckwheat.editor.tagging

import androidx.compose.animation.*
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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

enum class CommittingState { EMPTY, EDIT, EXIST }

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun TaggingSpent(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    editorFocusController: FocusController,
) {
    val localDensity = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var showAddComment by remember { mutableStateOf(false) }
    var value by remember { mutableStateOf("") }
    var state by remember { mutableStateOf(CommittingState.EMPTY) }
    val focusRequester = remember { FocusRequester() }
    val height = calcFontHeight(style = MaterialTheme.typography.bodyMedium).coerceAtLeast(24.dp) + 12.dp

    observeLiveData(spendsViewModel.stage) {
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
                    .then(if (state === CommittingState.EDIT) {
                        Modifier
                    } else {
                        Modifier.clickable {
                            editorFocusController.blur()
                            focusManager.clearFocus()
                            state = CommittingState.EDIT
                            appViewModel.showSystemKeyboard.value = true
                        }
                    })
            ) {
                val doneEdit = {
                    state = if (value.isEmpty()) CommittingState.EMPTY else CommittingState.EXIST
                    appViewModel.showSystemKeyboard.value = false
                    spendsViewModel.currentComment = value
                }

                Row(
                    modifier = Modifier.padding(start = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AnimatedVisibility(
                        visible = state == CommittingState.EDIT,
                        enter = scaleIn(
                            tween(
                                durationMillis = 150,
                                easing = EaseInOutQuad,
                            )
                        ),
                        exit = scaleOut(
                            tween(
                                durationMillis = 150,
                                easing = EaseInOutQuad,
                            )
                        ),
                    ) {
                        Spacer(Modifier.width(6.dp))
                    }
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(R.drawable.ic_comment),
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(8.dp))

                    AnimatedContent(
                        targetState = state,
                        transitionSpec = {
                            when(targetState) {
                                CommittingState.EMPTY -> slideInVertically(
                                    tween(durationMillis = 250)
                                ) { height -> height } + fadeIn(
                                    tween(durationMillis = 250)
                                ) with slideOutVertically(
                                    tween(durationMillis = 250)
                                ) { height -> -height } + fadeOut(
                                    tween(durationMillis = 250)
                                )
                                CommittingState.EDIT -> slideInVertically(
                                    tween(durationMillis = 250)
                                ) { height -> height } + fadeIn(
                                    tween(durationMillis = 250)
                                ) with slideOutVertically(
                                    tween(durationMillis = 250)
                                ) { height -> -height } + fadeOut(
                                    tween(durationMillis = 250)
                                )
                                CommittingState.EXIST -> slideInVertically(
                                    tween(durationMillis = 250)
                                ) { height -> height } + fadeIn(
                                    tween(durationMillis = 250)
                                ) with slideOutVertically(
                                    tween(durationMillis = 250)
                                ) { height -> -height } + fadeOut(
                                    tween(durationMillis = 250)
                                )
                            }.using(
                                SizeTransform(clip = false)
                            )
                        }
                    ) { targetState -> when (targetState) {
                        CommittingState.EMPTY -> {
                            Text(
                                modifier = Modifier.padding(top = 6.dp, bottom = 6.dp, end = 16.dp),
                                text = stringResource(R.string.add_comment),
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        CommittingState.EDIT -> {
                            var focusIsTracking by remember { mutableStateOf(false) }

                            TextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester)
                                    .onFocusChanged { focusState ->
                                        if (!focusState.hasFocus && focusIsTracking) {
                                            doneEdit()
                                        }
                                    },
                                value = value,
                                onValueChange = {
                                    value = it
                                },
                                trailingIcon = {
                                    if (value.isEmpty()) return@TextField

                                    IconButton(
                                        modifier = Modifier.padding(end = 6.dp),
                                        onClick = {
                                            doneEdit()
                                        },
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
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        doneEdit()
                                    }
                                ),
                            )

                            LaunchedEffect(Unit) {
                                focusRequester.requestFocus()
                                focusIsTracking = true
                            }
                        }
                        CommittingState.EXIST -> {
                            Row(
                                Modifier.padding(top = 6.dp, bottom = 6.dp, end = 16.dp),
                            ) {
                                Text(
                                    text = value,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    } }
                }

            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    BuckwheatTheme() {
        TaggingSpent(editorFocusController = FocusController())
    }
}
