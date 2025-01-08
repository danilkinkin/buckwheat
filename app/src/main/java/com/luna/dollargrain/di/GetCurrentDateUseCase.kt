package com.luna.dollargrain.di

import java.util.Date
import javax.inject.Inject

open class GetCurrentDateUseCase @Inject constructor() {
    open operator fun invoke(): Date {
        return Date()
    }
}