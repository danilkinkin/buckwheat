package com.danilkinkin.buckwheat.effects

import android.graphics.PointF
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.harmonize.hct.Hct
import com.danilkinkin.buckwheat.ui.harmonize.palettes.TonalPalette
import kotlin.math.*

data class Particle(
    val color: Color,
    val bSideColor: Color,
    val path: List<PointF>,
    val hitBox: Size,
    var rotateAngleY: Float = 0f,
    var rotateAngleX: Float = 0f,
    var position: PointF,
    var vectorAcceleration: PointF,
    val windage: Float,
    var mustDestroy: Boolean = false,
    var alpha: Float = 1f,
    var lifetime: Long? = null,
)

fun DrawScope.drawParticle(particle: Particle, debug: Boolean = false) {
    val halfWidth = particle.hitBox.width / 2
    val halfHeight = particle.hitBox.height / 2

    val perspective = 1f
    val radianX = particle.rotateAngleX * (PI / 180f)
    val radianY = particle.rotateAngleY * (PI / 180f)
    val rotateX = cos(radianX).toFloat()
    val rotateY = cos(radianY).toFloat()
    val rotateAltX = sin(radianX).toFloat()
    val rotateAltY = sin(radianY).toFloat()

    drawPath(
        color = (if (rotateY > 0f && rotateX > 0f) particle.color else particle.bSideColor)
            .copy(alpha = particle.alpha),
        path = Path().apply {
            val centerX = particle.position.x
            val centerY = particle.position.y

            particle.path.forEachIndexed { index, point ->
                val xRaw = (point.x - halfWidth)
                val yRaw = (point.y - halfHeight)

                val x = centerX + xRaw * rotateY + yRaw * rotateAltX * rotateAltY
                val y = centerY + yRaw * rotateX

                if (index == 0) moveTo(x, y) else lineTo(x, y)

                if (debug) {
                    drawCircle(
                        color = Color.Black,
                        radius = 6f,
                        center = Offset(centerX + xRaw, centerY)
                    )
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText(
                            "yRaw = $yRaw",
                            x,
                            y + 24.sp.value,
                            textPaint
                        )
                    }
                }
            }

            close()
        },
    )

    if (debug) {
        drawIntoCanvas {
            it.nativeCanvas.drawText(
                "rotateAngleY = ${particle.rotateAngleY}",
                0f,
                24.sp.value,
                textPaint
            )
            it.nativeCanvas.drawText(
                "rotateAngleX = ${particle.rotateAngleX}",
                0f,
                24.sp.value * 2,
                textPaint
            )
            it.nativeCanvas.drawText(
                "rotateX = $rotateX",
                0f,
                24.sp.value * 3,
                textPaint
            )
            it.nativeCanvas.drawText(
                "rotateY = $rotateY",
                0f,
                24.sp.value * 4,
                textPaint
            )
            it.nativeCanvas.drawText(
                "rotateAltX = $rotateAltX",
                0f,
                24.sp.value * 6,
                textPaint
            )
            it.nativeCanvas.drawText(
                "rotateAltY = $rotateAltY",
                0f,
                24.sp.value * 7,
                textPaint
            )
            it.nativeCanvas.drawText(
                "perspective = $perspective",
                0f,
                24.sp.value * 9,
                textPaint
            )
            it.nativeCanvas.drawText(
                "rtr = ${(1f - abs(rotateAltX)) * sign(rotateAltX)}",
                0f,
                24.sp.value * 10,
                textPaint
            )

        }
    }
}

@Preview(name = "rotate confetti")
@Composable
private fun PreviewParticle() {
    BuckwheatTheme {
        val particles by remember {
            val color = Color.Red

            val hct = Hct.fromInt(color.toArgb())
            val hue = hct.hue
            val chroma = hct.chroma
            val tone = hct.tone

            val bSideColor = Color(TonalPalette.fromHueAndChroma(hue, chroma).tone(tone / 1.5))

            mutableStateOf(
                listOf(
                    Particle(
                        color = color,
                        bSideColor = bSideColor,
                        path = listOf(
                            PointF(0f, 0f),
                            PointF(150f, 0f),
                            PointF(150f, 150f),
                            PointF(0f, 150f),
                        ),
                        hitBox = Size(150f, 150f),
                        position = PointF(800f, 400f),
                        vectorAcceleration = PointF(0f, 0f),
                        windage = 0f,
                    ),
                    Particle(
                        color = color,
                        bSideColor = bSideColor,
                        path = listOf(
                            PointF(0f, 0f),
                            PointF(150f, 0f),
                            PointF(150f, 150f),
                            PointF(0f, 150f),
                        ),
                        hitBox = Size(150f, 150f),
                        position = PointF(400f, 400f),
                        vectorAcceleration = PointF(0f, 0f),
                        windage = 0f,
                    ),
                    Particle(
                        color = color,
                        bSideColor = bSideColor,
                        path = listOf(
                            PointF(0f, 0f),
                            PointF(150f, 0f),
                            PointF(150f, 150f),
                            PointF(0f, 150f),
                        ),
                        hitBox = Size(150f, 150f),
                        position = PointF(800f, 800f),
                        vectorAcceleration = PointF(0f, 0f),
                        windage = 0f,
                    ),
                    Particle(
                        color = color,
                        bSideColor = bSideColor,
                        path = listOf(
                            PointF(15f, 20f),
                            PointF(75f, 0f),
                            PointF(60f, 150f),
                            PointF(0f, 100f),
                        ),
                        hitBox = Size(75f, 150f),
                        position = PointF(400f, 1200f),
                        vectorAcceleration = PointF(0f, 0f),
                        windage = 0f,
                    ),
                    Particle(
                        color = color,
                        bSideColor = bSideColor,
                        path = listOf(
                            PointF(0f, 0f),
                            PointF(150f, 0f),
                            PointF(150f, 150f),
                            PointF(0f, 150f),
                        ),
                        hitBox = Size(150f, 150f),
                        position = PointF(400f, 800f),
                        vectorAcceleration = PointF(0f, 0f),
                        windage = 0f,
                    ),
                    Particle(
                        color = color,
                        bSideColor = bSideColor,
                        path = listOf(
                            PointF(0f, 0f),
                            PointF(150f, 0f),
                            PointF(150f, 150f),
                            PointF(0f, 150f),
                        ),
                        hitBox = Size(150f, 150f),
                        position = PointF(400f, 1600f),
                        vectorAcceleration = PointF(0f, 0f),
                        windage = 0f,
                    )
                )
            )
        }

        val infiniteTransition = rememberInfiniteTransition()

        val angle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(14000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        Canvas(Modifier.fillMaxSize()) {
            particles.forEach {
                drawParticle(
                    it.copy(rotateAngleY = angle, rotateAngleX = 45f),
                    debug = true,
                )
            }
        }
    }
}