package com.helios.spaceweather.data.local

import androidx.room.Entity
import com.helios.spaceweather.domain.model.KpReading
import java.time.Instant

/**
 * Room representation of a [KpReading].
 *
 * The primary key is composite — (timestamp, isForecast) — because the same instant can exist
 * both as an observed point and as a previously-predicted point; keying on both lets an upsert
 * replace like-for-like without one source clobbering the other.
 */
@Entity(tableName = "kp_reading", primaryKeys = ["timestampMillis", "isForecast"])
data class KpReadingEntity(
    val timestampMillis: Long,
    val kp: Double,
    val isForecast: Boolean,
)

fun KpReadingEntity.toDomain(): KpReading = KpReading(
    time = Instant.ofEpochMilli(timestampMillis),
    kp = kp,
    isForecast = isForecast,
)

fun KpReading.toEntity(): KpReadingEntity = KpReadingEntity(
    timestampMillis = time.toEpochMilli(),
    kp = kp,
    isForecast = isForecast,
)
