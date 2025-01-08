package com.luna.dollargrain.di

import androidx.room.TypeConverter
import java.math.BigDecimal
import java.util.Date

class RoomConverters {
    @TypeConverter
    fun dateToDateStamp(input: Date): Long = input.time

    @TypeConverter
    fun dateStampToCalendar(input: Long): Date = Date(input)

    @TypeConverter
    fun bigDecimalToString(input: BigDecimal): String = input.toPlainString()

    @TypeConverter
    fun stringToBigDecimal(input: String): BigDecimal = BigDecimal(input)
}