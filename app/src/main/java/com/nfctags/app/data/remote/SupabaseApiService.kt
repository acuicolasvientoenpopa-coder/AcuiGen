package com.nfctags.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseApiService {

    @POST("nfc_tags")
    suspend fun upsertTags(
        @Body tags: ArrayList<SupabaseTagDto>
    ): Response<Unit>

    @POST("nfc_tags_history")
    suspend fun upsertHistory(
        @Body history: ArrayList<SupabaseHistoryDto>
    ): Response<Unit>

    @GET("nfc_tags")
    suspend fun getTags(
        @Query("select") select: String = "*",
        @Query("order") order: String = "updated_at.desc"
    ): Response<List<SupabaseTagDto>>

    @GET("nfc_tags_history")
    suspend fun getHistory(
        @Query("select") select: String = "*",
        @Query("tag_id") tagId: String? = null,
        @Query("order") order: String = "timestamp.desc"
    ): Response<List<SupabaseHistoryDto>>
}
