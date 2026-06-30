package com.nfctags.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Query

interface SupabaseApiService {

    @POST("nfc_tags")
    suspend fun upsertTag(
        @Header("Prefer") prefer: String = "resolution=merge-duplicates",
        @Body tag: SupabaseTagDto
    ): Response<Unit>

    @POST("nfc_tags")
    suspend fun upsertTags(
        @Header("Prefer") prefer: String = "resolution=merge-duplicates",
        @Body tags: List<SupabaseTagDto>
    ): Response<Unit>

    @POST("nfc_tags_history")
    suspend fun upsertHistory(
        @Header("Prefer") prefer: String = "resolution=merge-duplicates",
        @Body history: List<SupabaseHistoryDto>
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

    @PATCH("nfc_tags")
    suspend fun updateTag(
        @Query("id") id: String,
        @Body tag: SupabaseTagDto
    ): Response<ResponseBody>

    @DELETE("nfc_tags")
    suspend fun deleteTag(
        @Query("id") id: String
    ): Response<ResponseBody>
}
