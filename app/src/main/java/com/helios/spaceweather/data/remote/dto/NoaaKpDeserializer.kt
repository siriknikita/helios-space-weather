package com.helios.spaceweather.data.remote.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

/**
 * Tolerant deserializer for NOAA SWPC Kp products.
 *
 * NOAA's product JSON endpoints currently return a **legacy array-of-arrays**: a header
 * row of column names followed by data rows whose values are quoted strings, e.g.
 *
 * ```
 * [["time_tag","Kp","observed","noaa_scale"],
 *  ["2024-05-29 00:00:00","3.00","observed",null], ...]
 * ```
 *
 * The project spec, however, assumes a **modern numeric-object** shape, e.g.
 *
 * ```
 * [{"time_tag":"2024-05-29 00:00:00","kp_index":3.0,"observed":"observed"}, ...]
 * ```
 *
 * Rather than bet on one, this serializer inspects the first element and parses whichever it
 * finds — so the app works against the live API today and survives a future format change.
 * Column/field names are matched case-insensitively and by intent (the time column is the one
 * containing "time"; the Kp column is the K-index value, not its fraction or running average).
 */
object NoaaKpDeserializer : KSerializer<NoaaKpProduct> {

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("com.helios.spaceweather.NoaaKpProduct")

    override fun deserialize(decoder: Decoder): NoaaKpProduct {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("NoaaKpProduct can only be read from JSON")
        val root = input.decodeJsonElement().jsonArray
        if (root.isEmpty()) return NoaaKpProduct(emptyList())

        return when (val first = root.first()) {
            is JsonArray -> parseLegacy(root)
            is JsonObject -> parseModern(root)
            else -> throw SerializationException(
                "Unexpected NOAA payload element: ${first::class.simpleName}",
            )
        }
    }

    // ---- legacy: header row + array rows of strings ------------------------
    private fun parseLegacy(root: JsonArray): NoaaKpProduct {
        val header = root.first().jsonArray.map { (it.jsonPrimitive.contentOrNull ?: "").lowercase() }
        val timeIdx = header.indexOfFirst { it.contains("time") }.takeIf { it >= 0 } ?: 0
        val kpIdx = header.indexOfKpColumn().takeIf { it >= 0 } ?: 1
        val statusIdx = header.indexOfFirst { it == "observed" || it == "status" }

        val rows = root.drop(1).mapNotNull { element ->
            val cells = (element as? JsonArray) ?: return@mapNotNull null
            val kp = cells.getOrNull(kpIdx)?.asDoubleOrNull() ?: return@mapNotNull null
            val timeTag = cells.getOrNull(timeIdx)?.asContentOrNull() ?: return@mapNotNull null
            val status = statusIdx.takeIf { it >= 0 }?.let { cells.getOrNull(it)?.asContentOrNull() }
            NoaaKpRow(timeTag = timeTag, kp = kp, status = status)
        }
        return NoaaKpProduct(rows)
    }

    // ---- modern: array of objects with named, possibly-numeric fields ------
    private fun parseModern(root: JsonArray): NoaaKpProduct {
        val rows = root.mapNotNull { element ->
            val obj = (element as? JsonObject) ?: return@mapNotNull null
            val kp = obj.firstValue("kp_index", "kp", "estimated_kp", "kp_value")
                ?.asDoubleOrNull() ?: return@mapNotNull null
            val timeTag = obj.firstValue("time_tag", "time", "timestamp")
                ?.asContentOrNull() ?: return@mapNotNull null
            val status = obj.firstValue("observed", "status", "type")?.asContentOrNull()
            NoaaKpRow(timeTag = timeTag, kp = kp, status = status)
        }
        return NoaaKpProduct(rows)
    }

    override fun serialize(encoder: Encoder, value: NoaaKpProduct): Unit =
        throw UnsupportedOperationException("NoaaKpProduct is read-only (response decoding only)")

    // ---- helpers ----------------------------------------------------------
    private fun List<String>.indexOfKpColumn(): Int {
        // Prefer an exact K-index column; fall back to any "kp" that isn't a derived metric.
        indexOf("kp").takeIf { it >= 0 }?.let { return it }
        indexOf("kp_index").takeIf { it >= 0 }?.let { return it }
        indexOf("estimated_kp").takeIf { it >= 0 }?.let { return it }
        return indexOfFirst {
            it.contains("kp") && !it.contains("fraction") && !it.contains("running")
        }
    }

    private fun JsonObject.firstValue(vararg keys: String): JsonElement? {
        for (key in keys) {
            // Exact match first, then a case-insensitive fallback: the live 2026 API uses
            // "Kp" (capitalized) on the observed product but "kp" on the forecast product.
            this[key]?.let { return it }
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) }?.let { return it.value }
        }
        return null
    }

    private fun JsonElement.asDoubleOrNull(): Double? {
        val primitive = this as? JsonPrimitive ?: return null
        // Handles both unquoted numbers (modern) and quoted numeric strings (legacy).
        return primitive.doubleOrNull ?: primitive.contentOrNull?.trim()?.toDoubleOrNull()
    }

    private fun JsonElement.asContentOrNull(): String? =
        (this as? JsonPrimitive)?.contentOrNull
}
