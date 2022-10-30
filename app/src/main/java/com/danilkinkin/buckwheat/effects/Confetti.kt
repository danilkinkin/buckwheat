package com.danilkinkin.buckwheat.effects

import android.graphics.PointF
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
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
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.harmonize.hct.Hct
import com.danilkinkin.buckwheat.ui.harmonize.palettes.TonalPalette
import com.danilkinkin.buckwheat.util.animationTimeMillis
import com.danilkinkin.buckwheat.util.harmonize
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

@Composable
fun generateColors() = listOf(
    harmonize(Color.Red),
    harmonize(Color.Green),
    harmonize(Color.Blue),
    harmonize(Color.Yellow),
)

val textPaint = Paint().asFrameworkPaint().apply {
    isAntiAlias = true
    textSize = 24.sp.value
    color = android.graphics.Color.BLUE
    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
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
    particleSize: Pair<Int, Int>,
    ejectPoint: PointF,
    ejectAngle: Int,
    ejectVector: PointF,
    colors: List<Color>,
    windage: Pair<Float, Float>,
    ejectForceCoefficient: Float,
    lifetime: Pair<Long, Long>,
): List<Particle> {
    val spawnCount = Random.nextInt(count.first, count.second)

    val quarterSize = (particleSize.second - particleSize.first) / 4

    val list = emptyList<Particle>().toMutableList()

    for (i in 0..spawnCount) {
        val hitBox = Size(
            Random.nextInt(particleSize.first, particleSize.second - quarterSize).toFloat() / 2,
            Random.nextInt(particleSize.first + quarterSize, particleSize.second).toFloat(),
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
                    randomizeVectorDirection(ejectVector, ejectAngle),
                    ejectForceCoefficient,
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
                windage = Random.nextDouble(
                    windage.first.toDouble(),
                    windage.second.toDouble(),
                ).toFloat(),
                rotateAngleY = Random.nextDouble(0.0, 360.0).toFloat(),
                rotateAngleX = Random.nextDouble(20.0, 85.0).toFloat(),
                lifetime = Random.nextLong(lifetime.first, lifetime.second),
            )
        )
    }

    return list
}

class ConfettiController {
    var onSpawn: MutableState<((
        count: Pair<Int, Int>,
        ejectVector: PointF,
        ejectPoint: PointF,
        ejectAngle: Int,
        ejectForceCoefficient: Float,
        colors: List<Color>,
        particleSize: Pair<Int, Int>,
        windage: Pair<Float, Float>,
        lifetime: Pair<Long, Long>,
    ) -> Unit)?> = mutableStateOf(null)

    fun spawn(
        count: Pair<Int, Int> = 10 to 50,
        ejectVector: PointF = PointF(0f, 0f),
        ejectPoint: PointF = PointF(0f, 0f),
        ejectAngle: Int = 60,
        ejectForceCoefficient: Float = 5f,
        colors: List<Color> = listOf(),
        particleSize: Pair<Int, Int> = 30 to 50,
        windage: Pair<Float, Float> = 0.01f to 15f,
        lifetime: Pair<Long, Long> = 3000L to 8000L,
    ) {
        if (onSpawn.value !== null) {
            onSpawn.value!!(
                count,
                ejectVector,
                ejectPoint,
                ejectAngle,
                ejectForceCoefficient,
                colors,
                particleSize,
                windage,
                lifetime,
            )
        }
    }
}

@Composable
fun Confetti(
    modifier: Modifier = Modifier,
    controller: ConfettiController = remember { ConfettiController() },
    maxCount: Int = 1000,
    gravity: Float = 10f,
    timeSpeed: Float = 0.01f,
    forceDestroyZoneOffset: Float = 50f,
    debug: Boolean = false,
) {
    val particles = remember<MutableList<Particle>> { mutableListOf() }
    val timeStamp by animationTimeMillis()
    var lastTimeStamp by remember { mutableStateOf(timeStamp) }
    val defaultColors = generateColors()

    LaunchedEffect(controller) {
        controller.onSpawn.value = {
                count: Pair<Int, Int>,
                ejectVector: PointF,
                ejectPoint: PointF,
                ejectAngle: Int,
                ejectForceCoefficient: Float,
                colors: List<Color>,
                particleSize: Pair<Int, Int>,
                windage: Pair<Float, Float>,
                lifetime: Pair<Long, Long>,
            ->
            particles.addAll(
                spawn(
                    count = count,
                    ejectPoint = ejectPoint,
                    ejectVector = ejectVector,
                    colors = colors.ifEmpty { defaultColors },
                    particleSize = particleSize,
                    windage = windage,
                    ejectAngle = ejectAngle,
                    ejectForceCoefficient = ejectForceCoefficient,
                    lifetime = lifetime,
                )
            )

            if (particles.size > maxCount) {
                particles.subList(0, particles.size - maxCount).forEach {
                    it.mustDestroy = true
                }
            }
        }

        while (true) {
            delay(1000)
            particles.removeIf { it.mustDestroy && it.alpha <= 0f }
        }
    }

    val diffTimestamp = (timeStamp - lastTimeStamp)
    lastTimeStamp = timeStamp

    Canvas(modifier = modifier) {
        val width = this.size.width
        val height = this.size.height

        particles.forEach {
            drawParticle(it)

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
                (absX - max(
                    absX * 0.4f,
                    absX - it.windage
                ) * diffTimestamp * timeSpeed) * sign(it.vectorAcceleration.x),
                if (getVectorLength(it.vectorAcceleration) > it.windage) {
                    (absY - max(
                        absY * 0.4f,
                        absY - it.windage
                    ) * diffTimestamp * timeSpeed) * sign(
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

            it.lifetime = it.lifetime?.minus(diffTimestamp)

            it.mustDestroy = it.mustDestroy || (it.lifetime !== null && it.lifetime!! <= 0L)

            if (
                it.position.x < -forceDestroyZoneOffset ||
                it.position.x > width + forceDestroyZoneOffset ||
                it.position.y > height + forceDestroyZoneOffset
            ) {
                it.mustDestroy = true
                it.alpha = 0f
            }

            if (it.mustDestroy) {
                it.alpha = (it.alpha - 0.1f * diffTimestamp * timeSpeed).coerceAtLeast(0f)
            }

            if (debug) {
                drawDebugArrow(
                    origin = Offset(it.position.x, it.position.y),
                    accelerationVector = it.vectorAcceleration,
                    color = it.color,
                )
            }
        }

        if (debug) {
            drawIntoCanvas {
                it.nativeCanvas.drawText(
                    "timeStamp = $timeStamp",
                    0f,
                    24.sp.value,
                    textPaint
                )
                it.nativeCanvas.drawText(
                    "diffTimestamp = $diffTimestamp",
                    0f,
                    24.sp.value * 2,
                    textPaint
                )
                it.nativeCanvas.drawText(
                    "timeSpeed = $timeSpeed",
                    0f,
                    24.sp.value * 3,
                    textPaint
                )
                it.nativeCanvas.drawText(
                    "gravity = $gravity",
                    0f,
                    24.sp.value * 4,
                    textPaint
                )
                it.nativeCanvas.drawText(
                    "count = ${particles.size}",
                    0f,
                    24.sp.value * 5,
                    textPaint
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

        val colors = generateColors()

        BoxWithConstraints(Modifier.fillMaxSize()) {
            Confetti(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures {
                            controller.spawn(
                                ejectPoint = PointF(constraints.maxWidth.toFloat(), 500f),
                                ejectVector = PointF(-100f, -100f),
                                ejectAngle = 140,
                                ejectForceCoefficient = 7f,
                                count = 30 to 60,
                                colors = colors,
                            )

                            controller.spawn(
                                ejectPoint = PointF(0f, 500f),
                                ejectVector = PointF(100f, -100f),
                                ejectAngle = 140,
                                ejectForceCoefficient = 7f,
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