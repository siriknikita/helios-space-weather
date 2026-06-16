package com.helios.spaceweather.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Parsed NOAA Kp "product" — a flat list of rows, decoded by [NoaaKpDeserializer] from
 * EITHER the live legacy shape (array-of-arrays with a header row and quoted-string values)
 * OR the numeric-object shape the spec assumes. Keeping the wire-format tolerance in the
 * deserializer means the rest of the app only ever sees this normalized structure.
 */
@Serializable(with = NoaaKpDeserializer::class)
data class NoaaKpProduct(val rows: List<NoaaKpRow>)

/**
 * One normalized row from a NOAA Kp product.
 *
 * @param timeTag the raw NOAA time string, e.g. "2024-05-29 00:00:00" (UTC).
 * @param kp the planetary K index value.
 * @param status NOAA's observation status when present — "observed" / "estimated" /
 *   "predicted" — or null for products that don't carry one (the planetary-index history).
 */
data class NoaaKpRow(
    val timeTag: String,
    val kp: Double,
    val status: String?,
)
