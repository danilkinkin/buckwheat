package com.luna.dollargrain.effects

import android.graphics.PointF
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

fun normalizationVector(vector: PointF): PointF {
    val m = sqrt(vector.x * vector.x + vector.y * vector.y)

    return PointF(
        vector.x / m,
        vector.y / m,
    )
}

fun getVectorAngle(vector: PointF): Float {
    val normalizedVector = normalizationVector(vector)

    val partAngle = (acos(normalizedVector.x) * (180f / PI)).toFloat()

    return if (normalizedVector.y >= 0) {
        partAngle
    } else {
        360 - partAngle
    }
}

fun getVectorLength(vector: PointF): Float {
    return sqrt(vector.x.toDouble().pow(2.0) + vector.y.toDouble().pow(2.0)).toFloat()
}

fun randomizeShiftPoint(point: PointF, level: Float): PointF {
    return PointF(
        point.x + Random.nextDouble(-level.toDouble(), level.toDouble()).toFloat(),
        point.y + Random.nextDouble(-level.toDouble(), level.toDouble()).toFloat(),
    )
}

fun randomizeVectorDirection(vector: PointF, angle: Int): PointF {
    val currAngle = getVectorAngle(vector)
    val vectorLength = getVectorLength(vector)
    val randomizeAngle = Random.nextFloat() * (angle / 2)
    val randomizeDirection = if (Random.nextFloat() > 0.5f) 1 else -1

    val newAngle = currAngle + randomizeAngle * randomizeDirection

    return PointF(
        vectorLength * cos(newAngle * (PI / 180f)).toFloat(),
        vectorLength * sin(newAngle * (PI / 180f)).toFloat(),
    )
}

fun randomizeVectorForce(vector: PointF, coefficient: Float): PointF {
    val force = Random.nextFloat() * coefficient

    return PointF(
        vector.x * force,
        vector.y * force,
    )
}