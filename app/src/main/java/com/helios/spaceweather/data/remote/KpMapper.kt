package com.helios.spaceweather.data.remote

import com.helios.spaceweather.data.remote.dto.NoaaKpProduct
import com.helios.spaceweather.domain.model.KpReading
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Converts the wire-format [NoaaKpProduct] into domain [KpReading]s.
 *
 * NOAA time tags are UTC, space-separated ("2024-05-29 00:00:00"), occasionally with a "T"
 * separator and/or fractional seconds; the parser tolerates those variants and drops any row
 * whose time or value can't be read rather than failing the whole fetch.
 */
private val NOAA_FORMATTERS = listOf(
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
)

/** A NOAA status of "predicted" marks a forecast point; everything else is treated as observed. */
private const val PREDICTED_STATUS = "predicted"

fun NoaaKpProduct.toReadings(): List<KpReading> = rows.mapNotNull { row ->
    val time = parseNoaaTime(row.timeTag) ?: return@mapNotNull null
    KpReading(
        time = time,
        kp = row.kp,
        isForecast = row.status?.trim()?.lowercase() == PREDICTED_STATUS,
    )
}

private fun parseNoaaTime(raw: String): Instant? {
    val normalized = raw.trim().substringBefore('.') // strip any fractional seconds
    for (formatter in NOAA_FORMATTERS) {
        runCatching {
            return LocalDateTime.parse(normalized, formatter).toInstant(ZoneOffset.UTC)
        }
    }
    return null
}
