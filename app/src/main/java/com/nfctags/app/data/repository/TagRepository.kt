package com.nfctags.app.data.repository

import com.nfctags.app.data.dao.TagDao
import com.nfctags.app.data.dao.ValueHistoryDao
import com.nfctags.app.data.entities.TagEntity
import com.nfctags.app.data.entities.ValueHistoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepository @Inject constructor(
    private val tagDao: TagDao,
    private val historyDao: ValueHistoryDao
) {

    fun observeAllTags(): Flow<List<TagEntity>> = tagDao.observeAll()

    fun observeTag(id: String): Flow<TagEntity?> = tagDao.observeById(id)

    suspend fun getTag(id: String): TagEntity? = tagDao.getById(id)

    suspend fun saveTag(tag: TagEntity) {
        tagDao.upsert(tag)
    }

    suspend fun saveTagWithHistory(
        tag: TagEntity,
        cambios: Map<String, Pair<String, String>>
    ) {
        tagDao.upsert(tag)
        cambios.forEach { (campo, values) ->
            historyDao.insert(
                ValueHistoryEntity(
                    tagId = tag.id,
                    campo = campo,
                    valorAnterior = values.first,
                    valorNuevo = values.second
                )
            )
        }
    }

    suspend fun updateTagValues(
        tagId: String,
        nombre: String?,
        nuevosValores: Map<Int, String>
    ) {
        val existing = tagDao.getById(tagId) ?: return
        val cambios = mutableMapOf<String, Pair<String, String>>()

        if (nombre != null && nombre != existing.nombre) {
            cambios["nombre"] = Pair(existing.nombre, nombre)
        }

        val valoresActuales = listOf(
            existing.valor1, existing.valor2, existing.valor3,
            existing.valor4, existing.valor5, existing.valor6,
            existing.valor7, existing.valor8, existing.valor9, existing.valor10
        )

        val nuevos = nuevosValores.toMap()
        val updated = valoresActuales.mapIndexed { idx, current ->
            nuevos[idx + 1]?.let { nuevo ->
                if (nuevo != current) {
                    cambios["valor$idx"] = Pair(current, nuevo)
                }
                nuevo
            } ?: current
        }

        val updatedTag = existing.copy(
            nombre = nombre ?: existing.nombre,
            valor1 = updated[0],
            valor2 = updated[1],
            valor3 = updated[2],
            valor4 = updated[3],
            valor5 = updated[4],
            valor6 = updated[5],
            valor7 = updated[6],
            valor8 = updated[7],
            valor9 = updated[8],
            valor10 = updated[9],
            updatedAt = System.currentTimeMillis(),
            syncedAt = null
        )

        tagDao.upsert(updatedTag)
        cambios.forEach { (campo, values) ->
            historyDao.insert(
                ValueHistoryEntity(
                    tagId = tagId,
                    campo = campo,
                    valorAnterior = values.first,
                    valorNuevo = values.second
                )
            )
        }
    }

    suspend fun deleteTag(id: String) {
        tagDao.softDelete(id)
    }

    fun observeHistory(tagId: String): Flow<List<ValueHistoryEntity>> =
        historyDao.observeByTagId(tagId)

    suspend fun getUnsyncedTags(): List<TagEntity> = tagDao.getUnsynced()

    suspend fun getUnsyncedHistory(): List<ValueHistoryEntity> = historyDao.getUnsynced()

    suspend fun markTagSynced(id: String, timestamp: Long = System.currentTimeMillis()) {
        tagDao.markSynced(id, timestamp)
    }

    suspend fun markHistorySynced(ids: List<Long>) {
        ids.forEach { historyDao.markSynced(it) }
    }
}
