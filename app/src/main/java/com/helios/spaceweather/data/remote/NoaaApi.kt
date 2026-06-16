package com.helios.spaceweather.data.remote

import com.helios.spaceweather.data.remote.dto.NoaaKpProduct
import retrofit2.http.GET

/**
 * NOAA Space Weather Prediction Center (SWPC) product endpoints. Both responses decode through
 * the tolerant [NoaaKpProduct] serializer. Base URL is configured in the Hilt network module.
 */
interface NoaaApi {

    /** Recent observed planetary Kp index (3-hour cadence). */
    @GET("products/noaa-planetary-k-index.json")
    suspend fun getPlanetaryKIndex(): NoaaKpProduct

    /** Planetary Kp index forecast (observed/estimated/predicted points). */
    @GET("products/noaa-planetary-k-index-forecast.json")
    suspend fun getPlanetaryKForecast(): NoaaKpProduct

    companion object {
        const val BASE_URL = "https://services.swpc.noaa.gov/"
    }
}
