package io.github.orizynpx.arxwipe.data.local.converter

import androidx.room.TypeConverter
import io.github.orizynpx.arxwipe.domain.model.SwipeType
import kotlin.time.Instant
import kotlin.uuid.Uuid

class RoomConverters {
    @TypeConverter
    fun fromInstant(instant: Instant?): Long? = instant?.toEpochMilliseconds()

    @TypeConverter
    fun toInstant(millis: Long?): Instant? = millis?.let { Instant.fromEpochMilliseconds(it) }

    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    @TypeConverter
    fun fromUuid(uuid: Uuid?): String? = uuid?.toString()

    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    @TypeConverter
    fun toUuid(uuidString: String?): Uuid? = uuidString?.let { Uuid.parse(it) }

    @TypeConverter
    fun fromSwipeType(type: SwipeType?): String? = type?.name

    @TypeConverter
    fun toSwipeType(typeName: String?): SwipeType? = typeName?.let { SwipeType.valueOf(it) }
}
