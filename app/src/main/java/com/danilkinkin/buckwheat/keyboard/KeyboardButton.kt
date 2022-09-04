package com.danilkinkin.buckwheat.keyboard

import android.view.MotionEvent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danilkinkin.buckwheat.base.AutoResizeText
import com.danilkinkin.buckwheat.base.FontSizeRange
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.combineColors
import kotlin.math.min

enum class KeyboardButtonType { DEFAULT, PRIMARY, SECONDARY, TERTIARY }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun KeyboardButton(
    modifier: Modifier = Modifier,
    type: KeyboardButtonType,
    text: String? = null,
    icon: Painter? = null,
    onClick: (() -> Unit) = {},
) {
    var minSize by remember { mutableStateOf(0.dp) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val radius = animateDpAsState(targetValue = if (isPressed.value) 20.dp else minSize / 2)

    val color = when (type) {
        KeyboardButtonType.DEFAULT -> combineColors(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant,
            angle = 0.4F,
        )
        KeyboardButtonType.PRIMARY -> MaterialTheme.colorScheme.primaryContainer
        KeyboardButtonType.SECONDARY -> MaterialTheme.colorScheme.secondaryContainer
        KeyboardButtonType.TERTIARY -> MaterialTheme.colorScheme.tertiaryContainer
    }

    Surface(
        tonalElevation = 10.dp,
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned {
                minSize = min(it.size.height, it.size.width).dp
            }
            .clip(RoundedCornerShape(radius.value))
    ) {
        Box(
            modifier = Modifier
                .background(color = color)
                .fillMaxSize()
                .clip(RoundedCornerShape(radius.value))
                .clickable(
                    interactionSource = interactionSource,
                    indication = rememberRipple()
                ) { onClick.invoke() },
            contentAlignment = Alignment.Center
        ) {
            if (text !== null) {
                AutoResizeText(
                    text = text,
                    color = contentColorFor(color),
                    fontSizeRange = FontSizeRange(min = 8.sp, max = 90.sp)
                )
            }
            if (icon !== null) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                )
            }
        }
    }
}

@Preview
@Composable
fun KeyboardButtonPreview() {
    BuckwheatTheme {
        KeyboardButton(
            type = KeyboardButtonType.DEFAULT,
            text = "4"
        )
    }
}