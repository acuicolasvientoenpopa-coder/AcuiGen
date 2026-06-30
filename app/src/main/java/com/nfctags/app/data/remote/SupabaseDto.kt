package com.nfctags.app.data.remote

import com.google.gson.annotations.SerializedName

data class SupabaseTagDto(
    @SerializedName("id") val id: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("valor1") val valor1: String,
    @SerializedName("valor2") val valor2: String,
    @SerializedName("valor3") val valor3: String,
    @SerializedName("valor4") val valor4: String,
    @SerializedName("valor5") val valor5: String,
    @SerializedName("valor6") val valor6: String,
    @SerializedName("valor7") val valor7: String,
    @SerializedName("valor8") val valor8: String,
    @SerializedName("valor9") val valor9: String,
    @SerializedName("valor10") val valor10: String,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("updated_at") val updatedAt: Long,
    @SerializedName("synced_at") val syncedAt: Long? = null,
    @SerializedName("deleted") val deleted: Boolean = false
)

data class SupabaseHistoryDto(
    @SerializedName("tag_id") val tagId: String,
    @SerializedName("campo") val campo: String,
    @SerializedName("valor_anterior") val valorAnterior: String,
    @SerializedName("valor_nuevo") val valorNuevo: String,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("synced") val synced: Boolean = false
)

data class SupabaseUpsertResponse(
    @SerializedName("id") val id: String?
)

data class SupabaseError(
    @SerializedName("message") val message: String?,
    @SerializedName("code") val code: String?
)
