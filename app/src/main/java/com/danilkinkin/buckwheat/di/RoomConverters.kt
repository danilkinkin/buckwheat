package com.danilkinkin.buckwheat.di

import androidx.room.TypeConverter
import java.util.*

class RoomConverters {
    @TypeConverter
    fun dateToDateStamp(date: Date): Long = date.time

    @TypeConverter
    fun dateStampToCalendar(value: Long): Date = Date(value)
}