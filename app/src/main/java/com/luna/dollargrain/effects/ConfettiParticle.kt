package com.luna.dollargrain.effects

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
import com.luna.dollargrain.ui.BuckwheatTheme
import com.luna.dollargrain.ui.harmonize.hct.Hct
import com.luna.dollargrain.ui.harmonize.palettes.TonalPalette
import kotlin.math.*

data class Particle(
    val color: Color,
    val bSideColor: Color,
    val path: List<PointF>,
    val hitBox: Size,
    var rotateAngleX: Float = 0f,
    var rotateAngleY: Float = 0f,
    var rotateAngleZ: Float = 0f,
    var rotateAngleGlobalX: Float = 0f,
    var rotateAngleGlobalY: Float = 0f,
    var rotateAngleGlobalZ: Float = 0f,
    var position: PointF,
    var vectorAcceleration: PointF,
    val windage: Float,
    var mustDestroy: Boolean = false,
    var alpha: Float = 1f,
    var lifetime: Long? = null,
    var shiftXCoefficient: Float = 0f,
)

fun DrawScope.drawParticle(particle: Particle, debug: Boolean = false) {
    val halfWidth = particle.hitBox.width / 2
    val halfHeight = particle.hitBox.height / 2
    val centerX = particle.position.x
    val centerY = particle.position.y
    val radianX = particle.rotateAngleX * (PI / 180f)
    val radianY = particle.rotateAngleY * (PI / 180f)
    val radianZ = particle.rotateAngleZ * (PI / 180f)
    val radianGlobalX = particle.rotateAngleGlobalX * (PI / 180f)
    val radianGlobalY = particle.rotateAngleGlobalY * (PI / 180f)
    val radianGlobalZ = particle.rotateAngleGlobalZ * (PI / 180f)
    val rotateX = cos(radianX).toFloat()
    val rotateY = cos(radianY).toFloat()
    val rotateZ = cos(radianZ).toFloat()
    val rotateGlobalX = cos(radianGlobalX).toFloat()
    val rotateGlobalY = cos(radianGlobalY).toFloat()
    val rotateGlobalZ = cos(radianGlobalZ).toFloat()
    val rotateAltX = sin(radianX).toFloat()
    val rotateAltY = sin(radianY).toFloat()
    val rotateAltZ= sin(radianZ).toFloat()
    val rotateAltGlobalX = sin(radianGlobalX).toFloat()
    val rotateAltGlobalY = sin(radianGlobalY).toFloat()
    val rotateAltGlobalZ = sin(radianGlobalZ).toFloat()

    drawPath(
        color = (if (rotateY > 0f && rotateX > 0f && rotateGlobalY > 0f && rotateGlobalX > 0f) particle.color else particle.bSideColor)
            .copy(alpha = particle.alpha),
        path = Path().apply {
            particle.path.forEachIndexed { index, point ->
                val xRaw = (point.x - halfWidth)
                val yRaw = (point.y - halfHeight)

                val xRRaw = xRaw * rotateZ + yRaw * rotateAltZ
                val yRRaw = yRaw * rotateZ - xRaw * rotateAltZ

                val xRel = xRRaw * rotateY * rotateGlobalY + yRRaw * rotateAltX * rotateAltGlobalY
                val yRel = yRRaw * rotateX * rotateGlobalX  + xRRaw * rotateAltY * rotateAltGlobalX

                val x = centerX + xRel * rotateGlobalZ + yRel * rotateAltGlobalZ
                val y = centerY + yRel * rotateGlobalZ - xRel * rotateAltGlobalZ

                if (index == 0) moveTo(x, y) else lineTo(x, y)

                if (debug) {
                    drawCircle(
                        color = Color.Black,
                        radius = 6f,
                        center = Offset(centerX + xRaw, centerY)
                    )
                    drawIntoCanvas { canvas ->
                        listOf(
                            "xRaw = $xRaw",
                            "yRaw = $yRaw",
                        ).forEachIndexed { index, string ->
                            canvas.nativeCanvas.drawText(
                                string,
                                x,
                                y + 24.sp.value * (index + 1),
                                textPaint
                            )
                        }
                    }
                }
            }

            close()
        },
    )

    if (debug) {
        drawIntoCanvas {
            listOf(
                "rotateAngleX = ${particle.rotateAngleX}",
                "rotateAngleY = ${particle.rotateAngleY}",
                "rotateAngleGlobalX = ${particle.rotateAngleGlobalX}",
                "rotateAngleGlobalY = ${particle.rotateAngleGlobalY}",
                "rotateAngleGlobalZ = ${particle.rotateAngleGlobalZ}",
                "rotateX = $rotateX",
                "rotateY = $rotateY",
                "rotateGlobalX = $rotateGlobalX",
                "rotateGlobalY = $rotateGlobalY",
                "rotateGlobalZ = $rotateGlobalZ",
                "rotateAltX = $rotateAltX",
                "rotateAltY = $rotateAltY",
                "rotateAltGlobalX = $rotateAltGlobalX",
                "rotateAltGlobalY = $rotateAltGlobalY",
                "rotateAltGlobalZ = $rotateAltGlobalZ",
            ).forEachIndexed { index, string ->
                it.nativeCanvas.drawText(
                    string,
                    0f,
                    24.sp.value * (index + 1),
                    textPaint
                )
            }
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
                            PointF(20f, 15f),
                            PointF(0f, 75f),
                            PointF(150f, 60f),
                            PointF(100f, 0f),
                        ),
                        hitBox = Size(150f, 75f),
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

        val angleY by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        val angleX by infiniteTransition.animateFloat(
            initialValue = 70f,
            targetValue = 120f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Canvas(Modifier.fillMaxSize()) {
            particles.forEach {
                drawParticle(
                    it.copy(
                        rotateAngleGlobalZ = angleY,
                        rotateAngleX = angleX,
                        rotateAngleZ = angleY,
                    ),
                    debug = true,
                )
            }
        }
    }
}