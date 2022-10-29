package com.danilkinkin.buckwheat.effects

import android.graphics.PointF
import android.graphics.Typeface
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColor
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.harmonize.hct.Hct
import com.danilkinkin.buckwheat.ui.harmonize.palettes.TonalPalette
import com.danilkinkin.buckwheat.util.animationTimeMillis
import kotlin.math.*
import kotlin.random.Random


val colors = listOf(
    Color.Red,
    Color.Green,
    Color.Blue,
    Color.Yellow,
)

data class Particle(
    val color: Color,
    val bSideColor: Color,
    val path: List<PointF>,
    val hitBox: Size,
    var rotateAngleY: Float = 0f,
    var rotateAngleX: Float = 0f,
    var position: PointF,
    var perspectiveOrigin: PointF = position,
    var vectorAcceleration: PointF,
    val maxSpeed: Float,
    var mustDelete: Boolean = false,
    var alpha: Float = 1f,
)

val textPaint = Paint().asFrameworkPaint().apply {
    isAntiAlias = true
    textSize = 24.sp.value
    color = android.graphics.Color.BLUE
    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
}

fun DrawScope.drawConfetti(particle: Particle, debug: Boolean = false) {
    val halfWidth = particle.hitBox.width / 2
    val halfHeight = particle.hitBox.height / 2

    val perspective = 1f
    val radianX = particle.rotateAngleX * (PI / 180f)
    val radianY = particle.rotateAngleY * (PI / 180f)
    val rotateX = cos(radianX).toFloat()
    val rotateY = cos(radianY).toFloat()
    val rotateZ = 1//cos(radian).toFloat()
    val rotateAltX = sin(radianX).toFloat()
    val rotateAltY = sin(radianY).toFloat()
    val rotateAltZ = 0//sin(radian).toFloat()

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
        drawLine(
            color = Color.Blue,
            start = Offset(particle.perspectiveOrigin.x, 0f),
            end = Offset(particle.perspectiveOrigin.x, 1000f),
            strokeWidth = 1f
        )

        drawLine(
            color = Color.Green,
            start = Offset(0f, particle.perspectiveOrigin.y),
            end = Offset(1000f, particle.perspectiveOrigin.y),
            strokeWidth = 1f
        )

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
                "rotateZ = $rotateZ",
                0f,
                24.sp.value * 5,
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
                "rotateAltZ = $rotateAltZ",
                0f,
                24.sp.value * 8,
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

fun DrawScope.drawDebugArrow(
    origin: Offset,
    accelerationVector: PointF,
    color: Color = Color.Black,
) {
    val end = Offset(
        origin.x + accelerationVector.x,
        origin.y + accelerationVector.y,
    )

    drawLine(
        color = color,
        start = origin,
        end = end,
        strokeWidth = 2f,
    )

    drawCircle(
        color = color,
        radius = 4f,
        center = origin,
    )
}

fun spawn(
    count: Pair<Int, Int>,
    size: Pair<Int, Int>,
    ejectPoint: PointF = PointF(0f, 0f),
    accelerationVector: PointF = PointF(0f, 0f),
    colors: List<Color>,
    maxSpeed: Pair<Float, Float>,
    angle: Int = 60,
    forceCoefficient: Float = 5f,
): List<Particle> {
    val spawnCount = Random.nextInt(count.first, count.second)

    val quarterSize = (size.second - size.first) / 4

    val list = emptyList<Particle>().toMutableList()

    for (i in 0..spawnCount) {

        val hitBox = Size(
            Random.nextInt(size.first, size.second - quarterSize).toFloat() / 2,
            Random.nextInt(size.first + quarterSize, size.second).toFloat(),
        )

        val color = colors.random()

        val hct = Hct.fromInt(color.toArgb())
        val hue = hct.hue
        val chroma = hct.chroma
        val tone = hct.tone

        val bSideColor = Color(TonalPalette.fromHueAndChroma(hue, chroma).tone(tone / 1.5))

        list.add(
            Particle(
                position = ejectPoint,
                vectorAcceleration = randomizeVectorForce(
                    randomizeVectorDirection(
                        accelerationVector,
                        angle,
                    ),
                    forceCoefficient,
                ),
                color = color,
                bSideColor = bSideColor,
                hitBox = hitBox,
                path = listOf(
                    randomizeShiftPoint(PointF(0f, 0f), 6f),
                    randomizeShiftPoint(PointF(hitBox.width, 0f), 6f),
                    randomizeShiftPoint(PointF(hitBox.width, hitBox.height), 6f),
                    randomizeShiftPoint(PointF(0f, hitBox.height), 6f),
                ),
                maxSpeed = Random.nextDouble(
                    maxSpeed.first.toDouble(),
                    maxSpeed.second.toDouble(),
                ).toFloat(),
                rotateAngleY = Random.nextDouble(0.0, 360.0).toFloat(),
                rotateAngleX = Random.nextDouble(20.0, 85.0).toFloat(),
            )
        )
    }

    return list
}

class ConfettiController {
    var onSpawn: MutableState<(
        count: Pair<Int, Int>,
        accelerationVector: PointF,
        ejectPoint: PointF,
        angle: Int,
        forceCoefficient: Float,
        colors: List<Color>,
    ) -> Unit> = mutableStateOf({
            _, _, _, _, _, _ ->
    })

    fun spawn (
        count: Pair<Int, Int> = 10 to 50,
        accelerationVector: PointF = PointF(0f, 0f),
        ejectPoint: PointF = PointF(0f, 0f),
        angle: Int = 60,
        forceCoefficient: Float = 5f,
        colors: List<Color> = listOf(Color.Red, Color.Blue, Color.Green),
    ) {
        onSpawn.value(
            count,
            accelerationVector,
            ejectPoint,
            angle,
            forceCoefficient,
            colors,
        )
    }
}

@Composable
fun Confetti(
    modifier: Modifier = Modifier,
    controller: ConfettiController = remember { ConfettiController() },
    size: Pair<Int, Int> = 30 to 50,
    maxSpeed: Pair<Float, Float> = 0.01f to 15f,
    maxCount: Int = 1000,
    gravity: Float = 10f,
    timeSpeed: Float = 0.01f,
    debug: Boolean = false,
) {
    val particles = remember<MutableList<Particle>> { mutableListOf() }
    val timeStamp by animationTimeMillis()
    var lastTimeStamp by remember { mutableStateOf(timeStamp) }

    LaunchedEffect(controller) {
        controller.onSpawn.value = {
                count: Pair<Int, Int>,
                accelerationVector: PointF,
                ejectPoint: PointF,
                angle: Int,
                forceCoefficient: Float,
                colors: List<Color> ->
            Log.d("Confetti", "spawn...")
            particles.addAll(
                spawn(
                    count = count,
                    ejectPoint = ejectPoint,
                    accelerationVector = accelerationVector,
                    colors = colors,
                    size = size,
                    maxSpeed = maxSpeed,
                    angle = angle,
                    forceCoefficient = forceCoefficient,
                )
            )

            if (particles.size > maxCount) {
                particles.subList(0, particles.size - maxCount).forEach {
                    it.mustDelete = true
                }
            }

            particles.removeIf { it.mustDelete && it.alpha <= 0f }
        }
    }

    val diffTimestamp = (timeStamp - lastTimeStamp)
    lastTimeStamp = timeStamp

    Canvas(modifier = modifier) {
        val width = this.size.width
        val height = this.size.height

        particles.forEach {
            drawConfetti(it)

            it.position = PointF(
                it.position.x + it.vectorAcceleration.x * diffTimestamp * timeSpeed,
                it.position.y + it.vectorAcceleration.y * diffTimestamp * timeSpeed,
            )


            it.vectorAcceleration = PointF(
                it.vectorAcceleration.x,
                it.vectorAcceleration.y + gravity * diffTimestamp * timeSpeed,
            )

            val absX = abs(it.vectorAcceleration.x)
            val absY = abs(it.vectorAcceleration.y)

            it.vectorAcceleration = PointF(
                (absX - max(absX * 0.4f, absX - it.maxSpeed) * diffTimestamp * timeSpeed) * sign(it.vectorAcceleration.x),
                if (getVectorLength(it.vectorAcceleration) > it.maxSpeed) {
                    (absY - max(absY * 0.4f, absY - it.maxSpeed) * diffTimestamp * timeSpeed) * sign(
                        it.vectorAcceleration.y
                    )
                } else it.vectorAcceleration.y,
            )

            it.rotateAngleY = it.rotateAngleY + 10f * diffTimestamp * timeSpeed

            it.rotateAngleY = if (it.rotateAngleY > 360f) {
                it.rotateAngleY - 360f
            } else {
                it.rotateAngleY
            }

            it.mustDelete = it.mustDelete || it.position.x < 0 || it.position.x > width || it.position.y > height

            if (it.mustDelete) {
                it.alpha = (it.alpha - 0.1f * diffTimestamp * timeSpeed).coerceAtLeast(0f)
            }

            /*if (debug) {
                drawDebugArrow(
                    origin = Offset(it.position.x, it.position.y),
                    accelerationVector = it.vectorAcceleration,
                    color = it.color,
                )
            }*/
        }

        /* if (debug) {
            drawDebugArrow(
                origin = Offset(ejectPoint.x, ejectPoint.y),
                accelerationVector = accelerationVector,
            )
        } */
    }

    if (debug) {
        Column {
            Text(text = "timeStamp = $timeStamp")
            Text(text = "diffTimestamp = $diffTimestamp")
            Text(text = "count = ${particles.size}")
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

            mutableStateOf(listOf(
                Particle(
                    color = color,
                    bSideColor = bSideColor,
                    path = /* listOf(
                    PointF(10f, 30f),
                    PointF(120f, 30f),
                    PointF(110f, 180f),
                    PointF(10f, 130f),
                )*/listOf(
                        PointF(0f, 0f),
                        PointF(150f, 0f),
                        PointF(150f, 150f),
                        PointF(0f, 150f),
                    ),
                    hitBox = Size(150f, 150f),
                    position = PointF(800f, 400f),
                    perspectiveOrigin = PointF(400f, 400f),
                    vectorAcceleration = PointF(0f, 0f),
                    maxSpeed = 0f,
                ),
                Particle(
                    color = color,
                    bSideColor = bSideColor,
                    path = /* listOf(
                    PointF(10f, 30f),
                    PointF(120f, 30f),
                    PointF(110f, 180f),
                    PointF(10f, 130f),
                )*/listOf(
                        PointF(0f, 0f),
                        PointF(150f, 0f),
                        PointF(150f, 150f),
                        PointF(0f, 150f),
                    ),
                    hitBox = Size(150f, 150f),
                    position = PointF(400f, 400f),
                    perspectiveOrigin = PointF(400f, 400f),
                    vectorAcceleration = PointF(0f, 0f),
                    maxSpeed = 0f,
                ),
                Particle(
                    color = color,
                    bSideColor = bSideColor,
                    path = /* listOf(
                    PointF(10f, 30f),
                    PointF(120f, 30f),
                    PointF(110f, 180f),
                    PointF(10f, 130f),
                )*/listOf(
                        PointF(0f, 0f),
                        PointF(150f, 0f),
                        PointF(150f, 150f),
                        PointF(0f, 150f),
                    ),
                    hitBox = Size(150f, 150f),
                    position = PointF(800f, 800f),
                    perspectiveOrigin = PointF(400f, 400f),
                    vectorAcceleration = PointF(0f, 0f),
                    maxSpeed = 0f,
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
                    perspectiveOrigin = PointF(400f, 400f),
                    vectorAcceleration = PointF(0f, 0f),
                    maxSpeed = 0f,
                ),
                Particle(
                    color = color,
                    bSideColor = bSideColor,
                    path = /* listOf(
                    PointF(10f, 30f),
                    PointF(120f, 30f),
                    PointF(110f, 180f),
                    PointF(10f, 130f),
                )*/listOf(
                        PointF(0f, 0f),
                        PointF(150f, 0f),
                        PointF(150f, 150f),
                        PointF(0f, 150f),
                    ),
                    hitBox = Size(150f, 150f),
                    position = PointF(400f, 800f),
                    perspectiveOrigin = PointF(400f, 400f),
                    vectorAcceleration = PointF(0f, 0f),
                    maxSpeed = 0f,
                ),
                Particle(
                    color = color,
                    bSideColor = bSideColor,
                    path = /* listOf(
                    PointF(10f, 30f),
                    PointF(120f, 30f),
                    PointF(110f, 180f),
                    PointF(10f, 130f),
                )*/listOf(
                        PointF(0f, 0f),
                        PointF(150f, 0f),
                        PointF(150f, 150f),
                        PointF(0f, 150f),
                    ),
                    hitBox = Size(150f, 150f),
                    position = PointF(400f, 1600f),
                    perspectiveOrigin = PointF(400f, 400f),
                    vectorAcceleration = PointF(0f, 0f),
                    maxSpeed = 0f,
                )
            ))
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
                drawConfetti(
                    it.copy(rotateAngleY = angle, rotateAngleX = 45f),
                    debug = true,
                )
            }
        }
    }
}

@Preview(name = "confetti")
@Composable
private fun Preview() {
    BuckwheatTheme {
        val controller = remember { ConfettiController() }

        BoxWithConstraints(Modifier.fillMaxSize()) {
            Confetti(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures {
                            controller.spawn(
                                ejectPoint = PointF(constraints.maxWidth.toFloat(), 500f),
                                accelerationVector = PointF(-100f, -100f),
                                angle = 140,
                                forceCoefficient = 7f,
                                count = 30 to 60,
                                colors = colors,
                            )

                            controller.spawn(
                                ejectPoint = PointF(0f, 500f),
                                accelerationVector = PointF(100f, -100f),
                                angle = 140,
                                forceCoefficient = 7f,
                                count = 30 to 60,
                                colors = colors,
                            )
                        }
                    },
                controller = controller,
                debug = false,
            )
        }

        /*LaunchedEffect(Unit) {
            while (true) {
                controller.spawn()
                delay(500L)
            }
        } */
    }
}