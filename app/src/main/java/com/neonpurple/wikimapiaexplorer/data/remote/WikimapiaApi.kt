package com.neonpurple.wikimapiaexplorer.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WikimapiaApi {
    @GET("/")
    suspend fun getNearest(
        @Query("function") function: String = "place.getnearest",
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("radius") radius: Int,
        @Query("count") count: Int = 50,
        @Query("format") format: String = "json",
        @Query("key") key: String,
    ): Response<PlacesResponse>

    @GET("/")
    suspend fun getById(
        @Query("function") function: String = "place.getbyid",
        @Query("id") id: Long,
        @Query("data_blocks") dataBlocks: String = "main,geometry",
        @Query("format") format: String = "json",
        @Query("key") key: String,
    ): Response<PlaceDto>
}

@JsonClass(generateAdapter = true)
data class PlacesResponse(
    @Json(name = "places") val places: List<PlaceDto>? = null,
    @Json(name = "count") val count: Int? = null,
    @Json(name = "error") val error: Any? = null,
)

@JsonClass(generateAdapter = true)
data class PlaceDto(
    @Json(name = "id") val id: Long,
    @Json(name = "title") val title: String? = null,
    // Wikimapia responses vary; commonly expose lat/lon at top-level or nested
    @Json(name = "lat") val lat: Double? = null,
    @Json(name = "lon") val lon: Double? = null,
)
