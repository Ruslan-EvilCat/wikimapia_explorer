package com.neonpurple.wikimapiaexplorer.data

import com.neonpurple.wikimapiaexplorer.BuildConfig
import com.neonpurple.wikimapiaexplorer.data.remote.PlaceDto
import com.neonpurple.wikimapiaexplorer.data.remote.PlacesResponse
import com.neonpurple.wikimapiaexplorer.data.remote.WikimapiaApi
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class WikimapiaRepository(
    private val api: WikimapiaApi = defaultApi()
) {
    companion object {
        private fun defaultApi(): WikimapiaApi {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl("https://wikimapia.org/")
                .addConverterFactory(MoshiConverterFactory.create())
                .client(client)
                .build()
            return retrofit.create(WikimapiaApi::class.java)
        }
    }

    suspend fun getNearby(lat: Double, lon: Double, radius: Int): Result<List<PlaceDto>> = runWithRetry {
        val resp = api.getNearest(lat = lat, lon = lon, radius = radius, key = BuildConfig.WIKIMAPIA_KEY)
        if (resp.isSuccessful) {
            val body: PlacesResponse? = resp.body()
            Result.success(body?.places.orEmpty())
        } else {
            Result.failure(Exception("HTTP ${'$'}{resp.code()}"))
        }
    }

    suspend fun getById(id: Long): Result<PlaceDto> = runWithRetry {
        val resp = api.getById(id = id, key = BuildConfig.WIKIMAPIA_KEY)
        if (resp.isSuccessful) {
            val body = resp.body()
            if (body != null) Result.success(body) else Result.failure(Exception("Empty body"))
        } else {
            Result.failure(Exception("HTTP ${'$'}{resp.code()}"))
        }
    }

    private suspend fun <T> runWithRetry(
        attempts: Int = 3,
        baseDelayMs: Long = 400,
        block: suspend () -> Result<T>
    ): Result<T> {
        var current = 0
        var lastError: Throwable? = null
        while (current < attempts) {
            val res = block()
            if (res.isSuccess) return res
            lastError = res.exceptionOrNull()
            current++
            if (current < attempts) delay(baseDelayMs * current)
        }
        return Result.failure(lastError ?: Exception("Unknown error"))
    }
}
