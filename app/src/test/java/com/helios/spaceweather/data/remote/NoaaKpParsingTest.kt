package com.helios.spaceweather.data.remote

import com.helios.spaceweather.data.remote.dto.NoaaKpProduct
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins the tolerant NOAA parser to BOTH wire formats: the live legacy array-of-arrays (quoted
 * strings + header row) and the spec's modern numeric-object shape. If either regresses, the
 * app silently stops parsing real data — so these are the most load-bearing tests in the repo.
 */
class NoaaKpParsingTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Test
    fun `parses legacy array-of-arrays with quoted string values`() {
        val payload = """
            [["time_tag","Kp","observed","noaa_scale"],
             ["2024-05-29 00:00:00","3.00","observed",null],
             ["2024-05-29 03:00:00","6.67","observed",null],
             ["2024-05-29 06:00:00","5.00","predicted",null]]
        """.trimIndent()

        val product = json.decodeFromString<NoaaKpProduct>(payload)
        assertEquals(3, product.rows.size)
        assertEquals(6.67, product.rows[1].kp, 0.0001)
        assertEquals("predicted", product.rows[2].status)

        val readings = product.toReadings()
        assertEquals(3, readings.size)
        assertFalse(readings[0].isForecast)
        assertTrue("predicted rows become forecast points", readings[2].isForecast)
    }

    @Test
    fun `parses modern array-of-objects with unquoted numeric kp`() {
        val payload = """
            [{"time_tag":"2024-05-29 00:00:00","kp_index":3.0,"observed":"observed"},
             {"time_tag":"2024-05-29 03:00:00","kp_index":7.33,"observed":"predicted"}]
        """.trimIndent()

        val product = json.decodeFromString<NoaaKpProduct>(payload)
        assertEquals(2, product.rows.size)
        assertEquals(7.33, product.rows[1].kp, 0.0001)

        val readings = product.toReadings()
        assertEquals(2, readings.size)
        assertTrue(readings[1].isForecast)
        // Time tags parse to UTC instants in ascending order.
        assertTrue(readings[0].time.isBefore(readings[1].time))
    }

    @Test
    fun `empty payload yields no rows`() {
        assertEquals(0, json.decodeFromString<NoaaKpProduct>("[]").rows.size)
    }

    @Test
    fun `rows with unparseable values are dropped, not fatal`() {
        val payload = """
            [["time_tag","Kp","observed"],
             ["not-a-date","3.00","observed"],
             ["2024-05-29 03:00:00","oops","observed"],
             ["2024-05-29 06:00:00","4.00","observed"]]
        """.trimIndent()

        // The bad value row survives decoding (kp "oops" -> dropped at decode), and the
        // bad date row is dropped during mapping; one good reading remains.
        val readings = json.decodeFromString<NoaaKpProduct>(payload).toReadings()
        assertEquals(1, readings.size)
        assertEquals(4.0, readings[0].kp, 0.0001)
    }
}
