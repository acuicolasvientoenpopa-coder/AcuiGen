package com.nfctags.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "value_history")
data class ValueHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tagId: String,
    val campo: String,
    val valorAnterior: String,
    val valorNuevo: String,
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)
