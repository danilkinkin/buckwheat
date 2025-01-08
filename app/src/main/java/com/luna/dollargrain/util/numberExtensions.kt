package com.luna.dollargrain.util

import java.math.BigDecimal

fun BigDecimal.isZero(): Boolean = this.signum() == 0

fun BigDecimal.isEquals(second: BigDecimal): Boolean = this.setScale(2) == second.setScale(2)

// clamp(3.5f, 6.7f) > [0.0f, 1.0f]
fun Float.clamp(min: Float, max: Float): Float = (1f - ((this.coerceIn(min, max) - min) / (max - min)))