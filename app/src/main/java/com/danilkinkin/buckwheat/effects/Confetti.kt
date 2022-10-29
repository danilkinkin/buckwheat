package com.danilkinkin.buckwheat.effects

import android.graphics.PointF
import android.util.Log
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
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
    val path: List<PointF>,
    val hitBox: Size,
    var position: PointF,
    var vectorAcceleration: PointF,
    val maxSpeed: Float,
    var mustDelete: Boolean = false,
)

fun DrawScope.drawConfetti(particle: Particle) {
    val x = particle.position.x - particle.hitBox.width / 2
    val y = particle.position.y - particle.hitBox.height / 2

    drawPath(
        color = particle.color,
        path = Path().apply {
            moveTo(x, y)

            particle.path.forEach {
                lineTo(x + it.x, y + it.y)
            }

            close()
        },
    )
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
                color = colors.random(),
                hitBox = hitBox,
                path = listOf(
                    randomizeShiftPoint(PointF(hitBox.width, 0f), 6f),
                    randomizeShiftPoint(PointF(hitBox.width, hitBox.height), 6f),
                    randomizeShiftPoint(PointF(0f, hitBox.height), 6f),
                ),
                maxSpeed = Random.nextDouble(
                    maxSpeed.first.toDouble(),
                    maxSpeed.second.toDouble(),
                ).toFloat(),
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
                while (particles.size > maxCount) {
                    particles.removeFirst()
                }
            }

            particles.removeIf { it.mustDelete }
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

            it.mustDelete = it.position.x < 0 || it.position.x > width || it.position.y > height

            if (debug) {
                drawDebugArrow(
                    origin = Offset(it.position.x, it.position.y),
                    accelerationVector = it.vectorAcceleration,
                    color = it.color,
                )
            }
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
            Text(text = "lastTimeStamp = $lastTimeStamp")
            Text(text = "diffTimestamp = $diffTimestamp")
            Text(text = "count = ${particles.size}")
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