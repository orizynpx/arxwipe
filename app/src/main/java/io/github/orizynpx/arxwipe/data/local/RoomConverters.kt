package io.github.orizynpx.arxwipe.data.local

import androidx.room.TypeConverter
import kotlin.time.Instant

class RoomConverters {
    @TypeConverter
    fun fromInstant(instant: Instant?): Long? = instant?.toEpochMilliseconds()

    @TypeConverter
    fun toInstant(millis: Long?): Instant? = millis?.let { Instant.Companion.fromEpochMilliseconds(it) }
}