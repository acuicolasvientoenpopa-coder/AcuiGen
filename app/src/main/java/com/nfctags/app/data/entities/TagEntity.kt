package com.nfctags.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey
    val id: String,
    val nombre: String,
    val valor1: String,
    val valor2: String,
    val valor3: String,
    val valor4: String,
    val valor5: String,
    val valor6: String,
    val valor7: String,
    val valor8: String,
    val valor9: String,
    val valor10: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null,
    val deleted: Boolean = false
)
