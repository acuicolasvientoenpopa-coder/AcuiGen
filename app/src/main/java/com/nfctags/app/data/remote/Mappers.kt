package com.nfctags.app.data.remote

import com.nfctags.app.data.entities.TagEntity
import com.nfctags.app.data.entities.ValueHistoryEntity

fun TagEntity.toSupabaseDto() = SupabaseTagDto(
    id = id,
    nombre = nombre,
    valor1 = valor1,
    valor2 = valor2,
    valor3 = valor3,
    valor4 = valor4,
    valor5 = valor5,
    valor6 = valor6,
    valor7 = valor7,
    valor8 = valor8,
    valor9 = valor9,
    valor10 = valor10,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncedAt = syncedAt,
    deleted = deleted
)

fun ValueHistoryEntity.toSupabaseDto() = SupabaseHistoryDto(
    tagId = tagId,
    campo = campo,
    valorAnterior = valorAnterior,
    valorNuevo = valorNuevo,
    timestamp = timestamp,
    synced = synced
)
