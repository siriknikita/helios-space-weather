package com.helios.spaceweather.di

import android.content.Context
import com.helios.spaceweather.data.remote.NoaaApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Networking graph: a lenient JSON parser, an OkHttp client with an on-disk HTTP cache + a
 * short freshness window, Retrofit wired to NOAA, and the [NoaaApi].
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val cache = Cache(
            directory = File(context.cacheDir, "noaa_http_cache"),
            maxSize = HTTP_CACHE_BYTES,
        )

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        // NOAA serves these products without cache headers; rewrite responses to be cacheable
        // for a few minutes so rapid re-opens are served from disk rather than the network.
        val cacheControlInterceptor = okhttp3.Interceptor { chain ->
            val response = chain.proceed(chain.request())
            response.newBuilder()
                .header("Cache-Control", "public, max-age=$HTTP_CACHE_MAX_AGE_SECONDS")
                .removeHeader("Pragma")
                .build()
        }

        return OkHttpClient.Builder()
            .cache(cache)
            .addNetworkInterceptor(cacheControlInterceptor)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(NoaaApi.BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideNoaaApi(retrofit: Retrofit): NoaaApi = retrofit.create(NoaaApi::class.java)

    private const val HTTP_CACHE_BYTES = 5L * 1024 * 1024 // 5 MB
    private const val HTTP_CACHE_MAX_AGE_SECONDS = 300 // 5 minutes
}
