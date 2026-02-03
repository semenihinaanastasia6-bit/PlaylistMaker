package com.example.playlistmaker

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// Модель данных для ответа от API
data class ApiResponse(
    val resultCount: Int,
    val results: List<Track>
)


interface ApiService {

    @GET("search")
    fun search(
        @Query("term") text: String,
        @Query("entity") entity: String = "song"
    ): Call<ApiResponse>
}
