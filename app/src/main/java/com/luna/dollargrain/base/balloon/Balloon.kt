package com.luna.dollargrain.base.balloon

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luna.dollargrain.ui.BuckwheatTheme

enum class AnchorPosition {
    Start,
    Center,
    End,
}

@Composable
fun Balloon(
    modifier: Modifier = Modifier,
    position: AnchorPosition = AnchorPosition.Center,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = when (position) {
            AnchorPosition.Start -> Alignment.Start
            AnchorPosition.Center -> Alignment.CenterHorizontally
            AnchorPosition.End -> Alignment.End
        }
    ) {
        Anchor(
            modifier = Modifier
                .height(12.dp)
                .width(48.dp),
            position = position,
            tint = MaterialTheme.colorScheme.secondary,
        )
        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.secondary,
                    when (position) {
                        AnchorPosition.Start -> RoundedCornerShape(0.dp, 24.dp, 24.dp, 24.dp)
                        AnchorPosition.Center -> RoundedCornerShape(24.dp)
                        AnchorPosition.End -> RoundedCornerShape(24.dp, 0.dp, 24.dp, 24.dp)
                    }
                )
                .padding(24.dp, 12.dp)
        ) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onSecondary,
            ) {
                content()
            }
        }
    }
}

@Composable
private fun Anchor(
    modifier: Modifier = Modifier,
    position: AnchorPosition = AnchorPosition.Center,
    tint: Color = LocalContentColor.current,
) {
    Canvas(modifier) {
        val width = size.width
        val height = size.height
        val halfWidth = width / 2
        val thirdHeight = height / 3
        val quarterHalfWidth = halfWidth / 4

        val wavyPath = Path().apply {
            moveTo(x = 0f, y = height)

            if (position === AnchorPosition.End) {
                relativeMoveTo(dx = halfWidth, dy = 0f)
            }

            if (position === AnchorPosition.End || position === AnchorPosition.Center) {
                relativeQuadraticBezierTo(
                    dx1 = quarterHalfWidth * 2f,
                    dy1 = 0f,
                    dx2 = quarterHalfWidth * 3f,
                    dy2 = -thirdHeight * 1.4f,
                )
                relativeQuadraticBezierTo(
                    dx1 = quarterHalfWidth * 0.75f,
                    dy1 = -thirdHeight * 1f,
                    dx2 = quarterHalfWidth,
                    dy2 = -thirdHeight * 1.6f,
                )
            }

            if (position === AnchorPosition.End) {
                relativeLineTo(
                    dx = 0f,
                    dy = thirdHeight * 3f,
                )
            }

            if (position === AnchorPosition.Start) {
                relativeLineTo(
                    dx = 0f,
                    dy = -thirdHeight * 3f,
                )
            }

            if (position === AnchorPosition.Start || position === AnchorPosition.Center) {
                relativeQuadraticBezierTo(
                    dx1 = quarterHalfWidth * 0.25f,
                    dy1 = thirdHeight * 0.4f,
                    dx2 = quarterHalfWidth,
                    dy2 = thirdHeight * 1.6f,
                )
                relativeQuadraticBezierTo(
                    dx1 = quarterHalfWidth * 1f,
                    dy1 = thirdHeight * 1.4f,
                    dx2 = quarterHalfWidth * 3f,
                    dy2 = thirdHeight * 1.4f,
                )
            }

            lineTo(0f, height)

            close()
        }

        drawPath(
            path = wavyPath,
            SolidColor(tint),
            style = Fill
        )
    }
}

@Preview
@Composable
private fun Preview() {
    BuckwheatTheme {
        Balloon {
            Text(
                text = "Hello, world!",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewStart() {
    BuckwheatTheme {
        Balloon(position = AnchorPosition.Start) {
            Text(
                text = "Hello, world!",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewEnd() {
    BuckwheatTheme {
        Balloon(position = AnchorPosition.End) {
            Text(
                text = "Hello, world!",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewLong() {
    BuckwheatTheme {
        Balloon {
            Text(
                text = "Minim magna aliquip eiusmod velit sint ipsum laboris anim ex ex cillum cupidatat magna. Do deserunt magna veniam ut tempor dolor quis cillum dolor deserunt irure deserunt. Lorem dolor commodo cupidatat ut magna elit ea reprehenderit tempor esse do. Occaecat esse duis ullamco amet ad dolore voluptate commodo labore culpa dolor dolor pariatur fugiat.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}